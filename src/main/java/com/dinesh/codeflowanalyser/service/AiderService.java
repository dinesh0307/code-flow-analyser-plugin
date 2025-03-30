package com.dinesh.codeflowanalyser.service;

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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service(Service.Level.PROJECT)
public final class AiderService {
    private static final Logger LOG = Logger.getInstance(AiderService.class);

    private final Project project;
    private ProcessHandler currentProcessHandler;
    private Consumer<String> outputConsumer;

    public AiderService(Project project) {
        this.project = project;
    }

    /**
     * Starts a new Aider session with the specified files
     *
     * @param classNames List of class names to include in the session
     * @param prompt Initial prompt to send to Aider
     * @param onOutput Callback to receive output from Aider
     * @return CompletableFuture that completes when the process is started
     */
    public CompletableFuture<Void> startAiderSession(List<String> classNames, String prompt, Consumer<String> onOutput) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Stop any existing process
            if (currentProcessHandler != null && !currentProcessHandler.isProcessTerminated()) {
                currentProcessHandler.destroyProcess();
            }

            // Build command to start aider with the specified files
            GeneralCommandLine commandLine = new GeneralCommandLine("aider");
            commandLine.setWorkDirectory(project.getBasePath());

            // Add all class files to the command
            for (String className : classNames) {
                // Convert class name to file path (assuming standard Java package structure)
                String filePath = className.replace('.', '/') + ".java";
                commandLine.addParameter(filePath);
            }

            // Add initial prompt if provided
            if (prompt != null && !prompt.isEmpty()) {
                commandLine.addParameter("--message");
                commandLine.addParameter(prompt);
            }

            // Start the process
            currentProcessHandler = new OSProcessHandler(commandLine);
            this.outputConsumer = onOutput;

            // Add listener to capture output
            currentProcessHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    if (outputConsumer != null) {
                        outputConsumer.accept(event.getText());
                    }
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    if (outputConsumer != null) {
                        outputConsumer.accept("[Process terminated with exit code " + event.getExitCode() + "]");
                    }
                }
            });

            currentProcessHandler.startNotify();
            future.complete(null);

        } catch (ExecutionException e) {
            LOG.error("Failed to start Aider process", e);
            future.completeExceptionally(e);
        }

        return future;
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
