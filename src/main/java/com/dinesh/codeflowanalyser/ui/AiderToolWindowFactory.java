package com.dinesh.codeflowanalyser.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.dinesh.codeflowanalyser.service.AiderOutputHandler;
import com.dinesh.codeflowanalyser.service.AiderService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class AiderToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel contentPanel = createToolWindowPanel(project);
        Content content = ContentFactory.getInstance().createContent(
                contentPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private JPanel createToolWindowPanel(Project project) {
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Output area for Aider responses
        JTextPane outputArea = new JTextPane();
        outputArea.setEditable(false);
        JBScrollPane scrollPane = new JBScrollPane(outputArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton terminateButton = new JButton("Stop");

        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        buttonPanel.add(terminateButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        // Get the output handler service and initialize it
        AiderOutputHandler outputHandler = project.getService(AiderOutputHandler.class);
        outputHandler.initialize(outputArea, inputField, sendButton, terminateButton);

        // Add action listeners
        sendButton.addActionListener(e -> {
            String input = inputField.getText().trim();
            if (!input.isEmpty()) {
                AiderService aiderService = project.getService(AiderService.class);
                if (aiderService.isSessionActive()) {
                    outputHandler.appendOutput("\n> " + input + "\n");
                    aiderService.sendInput(input);
                    inputField.setText("");
                }
            }
        });

        terminateButton.addActionListener(e -> {
            AiderService aiderService = project.getService(AiderService.class);
            aiderService.terminateSession();
            outputHandler.setInputEnabled(false);
            outputHandler.appendOutput("\n[Session terminated]\n");
        });

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        // Initially disable input until a session is started
        outputHandler.setInputEnabled(false);

        return contentPanel;
    }
}