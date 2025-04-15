package com.dinesh.codeflowanalyser.actions;

import com.dinesh.codeflowanalyser.service.AiderOutputHandler;
import com.dinesh.codeflowanalyser.service.AiderService;
import com.dinesh.codeflowanalyser.service.JavaParserService;
import com.dinesh.codeflowanalyser.ui.ClassMethodInputDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class AiderAnalyzeAction extends AnAction {
    // In AiderAnalyzeAction class
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Show dialog to get class and method names
        ClassMethodInputDialog dialog = new ClassMethodInputDialog(project);
        if (!dialog.showAndGet()) {
            return; // User canceled
        }

        String className = dialog.getClassName();
        String methodName = dialog.getMethodName();

        // Get the impacted classes using your Java parser
        JavaParserService javaParserService = project.getService(JavaParserService.class);
        List<String> impactedClasses = javaParserService.getImpactedClasses(className, methodName, null, null);

        if (impactedClasses.isEmpty()) {
            return; // No classes found
        }

        // Get the Aider tool window
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Aider");
        if (toolWindow != null) {
            toolWindow.show(() -> {
                // Get the Aider service
                AiderService aiderService = project.getService(AiderService.class);

                // Create initial prompt
                String initialPrompt = "Analyze the flow of execution from " + className + "." + methodName;

                // Access the tool window content differently
                // Get the content component, which will be our content panel
                Content content = toolWindow.getContentManager().getContent(0);
                if (content != null) {
                    // Find the AiderToolWindowContent instance using the content panel
                    JComponent component = content.getComponent();
                    if (component instanceof JPanel) {
                        // Find our output handler - we need to refactor the AiderToolWindowFactory
                        // to provide access to the output handler
                        AiderOutputHandler outputHandler = project.getService(AiderOutputHandler.class);
                        if (outputHandler != null) {
                            outputHandler.clearOutput();
                            outputHandler.appendOutput("Starting Aider session with classes:\n");
                            for (String cls : impactedClasses) {
                                outputHandler.appendOutput("- " + cls + "\n");
                            }
                            outputHandler.appendOutput("\nInitial prompt: " + initialPrompt + "\n\n");

                            // Start the Aider session
                           /* aiderService.startAiderSession(impactedClasses, initialPrompt, outputHandler::appendOutput)
                                    .thenRun(() -> outputHandler.setInputEnabled(true));*/
                        }
                    }
                }
            });
        }
    }}
