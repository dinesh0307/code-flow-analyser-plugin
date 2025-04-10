package com.dinesh.codeflowanalyser.ui;

import com.dinesh.codeflowanalyser.service.AiderOutputHandler;
import com.dinesh.codeflowanalyser.service.AiderService;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A wrapper class for the Aider UI components that can be embedded in our tool window
 */
public class AiderToolWindowContent {
    private final Project project;
    private final JPanel contentPanel;

    public AiderToolWindowContent(Project project) {
        this.project = project;
        this.contentPanel = createAiderPanel();
    }

    private JPanel createAiderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Output area for Aider responses
        JTextPane outputArea = new JTextPane();
        outputArea.setEditable(false);
        JBScrollPane scrollPane = new JBScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

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

        panel.add(inputPanel, BorderLayout.SOUTH);

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

        return panel;
    }

    public JPanel getContent() {
        return contentPanel;
    }
}