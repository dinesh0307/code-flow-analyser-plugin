package com.dinesh.codeflowanalyser.service;

import com.dinesh.codeflowanalyser.api.ApiClient;
import com.dinesh.codeflowanalyser.api.ApiFactory;
import com.dinesh.codeflowanalyser.api.ApiType;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service(Service.Level.PROJECT)
public final class AiderService {
    private static final Logger LOG = Logger.getInstance(AiderService.class);

    private final Project project;
    private ProcessHandler currentProcessHandler;
    private Consumer<String> outputConsumer;
    private boolean sessionInitialized = false;
    private List<String> pendingClassNames;
    private String pendingPrompt;

    public AiderService(Project project) {
        this.project = project;
    }

    /**
     * Starts a new Aider session with the specified model and prepares to add files
     *
     * @param classNames List of class names to include in the session
     * @param prompt Initial prompt to send to Aider
     * @param onOutput Callback to receive output from Aider
     * @return CompletableFuture that completes when the process is started
     */
    public CompletableFuture<Void> startAiderSession(ApiType apiType, String model, List<String> classNames, String prompt, Consumer<String> onOutput) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Stop any existing process
            if (currentProcessHandler != null && !currentProcessHandler.isProcessTerminated()) {
                currentProcessHandler.destroyProcess();
            }

            // Reset session state
            sessionInitialized = false;
            pendingClassNames = classNames;
            pendingPrompt = prompt;

            ApiClient apiClient = ApiFactory.createClient(apiType);
            GeneralCommandLine generalCommandLine = null;
            try {
                generalCommandLine = apiClient.getGeneralCommandLine(project, model);
            }catch (IllegalStateException e){
                onOutput.accept("Error: " + e.getMessage() + "\n");
                future.completeExceptionally(e);
                return future;
            }


            generalCommandLine.setRedirectErrorStream(true);
            // Start the process
            currentProcessHandler = new OSProcessHandler(generalCommandLine);
            this.outputConsumer = onOutput;

            // Add listener to capture output and detect when session is ready
            currentProcessHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    String text = event.getText();
                    if (outputConsumer != null) {
                        outputConsumer.accept(text);
                    }

                    System.out.println(text);
                    // Detect when the aider session is initialized and ready for commands
                    // This is a heuristic - we're looking for the prompt pattern
                    if (!sessionInitialized && text.contains(">") && text.trim().endsWith(">")) {
                        sessionInitialized = true;
                        // Add the files one by one
                        addClassFilesAndSendPrompt();
                    }
                    /*if(text.contains("(Y)es/(N)o")){
                        System.out.println("Prompting yes or no");
                        sendInput("Yes");
                    }*/
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    if (outputConsumer != null) {
                        outputConsumer.accept("\n[Process terminated with exit code " + event.getExitCode() + "]\n");
                    }
                }
            });

            currentProcessHandler.startNotify();
            future.complete(null);

        } catch (ExecutionException e) {
            LOG.error("Failed to start Aider process", e);
            if (outputConsumer != null) {
                outputConsumer.accept("Error starting Aider: " + e.getMessage() + "\n");
            }
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Adds all class files to the session using the /add command
     * and then sends the initial prompt
     */
    private void addClassFilesAndSendPrompt() {
        if (pendingClassNames == null || pendingClassNames.isEmpty()) {
            // If no classes to add, just send the prompt
            if (pendingPrompt != null && !pendingPrompt.isEmpty()) {
                sendInput(pendingPrompt);
            }
            return;
        }

        // Schedule a task to add files with a small delay between commands
        CompletableFuture.runAsync(() -> {
            try {
                for (String className : pendingClassNames) {
                    // Convert class name to file path (assuming standard Java package structure)
                    //String filePath = className.replace('.', '/') + ".java";
                    String addCommand = "/add " + className;

                    // Send the /add command
                    sendInput(addCommand);

                    // Wait a bit to let Aider process the command
                    Thread.sleep(500);
                }

                // After adding all files, send the initial prompt
                if (pendingPrompt != null && !pendingPrompt.isEmpty()) {
                    Thread.sleep(1000); // Give a bit more time before sending the prompt
                    sendInput(pendingPrompt);
                }
            } catch (InterruptedException e) {
                LOG.error("Interrupted while adding files", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Sends input to the running Aider process
     *
     * @param input The text to send to Aider
     * @return true if input was sent successfully, false otherwise
     */
    public boolean sendInput(String input) {
        if (currentProcessHandler == null || currentProcessHandler.isProcessTerminated()) {
            return false;
        }

        try {
            OutputStream outputStream = currentProcessHandler.getProcessInput();
            if (outputStream != null) {
                outputStream.write((input + "\n").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                return true;
            }
        } catch (Exception e) {
            LOG.error("Failed to send input to Aider process", e);
        }

        return false;
    }

    /**
     * Terminates the current Aider session
     */
    public void terminateSession() {
        if (currentProcessHandler != null && !currentProcessHandler.isProcessTerminated()) {
            currentProcessHandler.destroyProcess();
        }
        sessionInitialized = false;
        pendingClassNames = null;
        pendingPrompt = null;
    }

    /**
     * Checks if an Aider session is currently running
     *
     * @return true if a session is active, false otherwise
     */
    public boolean isSessionActive() {
        return currentProcessHandler != null && !currentProcessHandler.isProcessTerminated();
    }
}