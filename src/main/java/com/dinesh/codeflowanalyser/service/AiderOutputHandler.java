package com.dinesh.codeflowanalyser.service;


import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

@Service(Service.Level.PROJECT)
public final class AiderOutputHandler {
    private JTextPane outputArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton terminateButton;

    // Initialize with references from the UI
    public void initialize(JTextPane outputArea, JTextField inputField,
                           JButton sendButton, JButton terminateButton) {
        this.outputArea = outputArea;
        this.inputField = inputField;
        this.sendButton = sendButton;
        this.terminateButton = terminateButton;
    }

    public void setInputEnabled(boolean enabled) {
        if (inputField != null && sendButton != null && terminateButton != null) {
            inputField.setEnabled(enabled);
            sendButton.setEnabled(enabled);
            terminateButton.setEnabled(enabled);
        }
    }

    public void appendOutput(String text) {
        if (outputArea != null) {
            SwingUtilities.invokeLater(() -> {
                Document doc = outputArea.getDocument();
                try {
                    doc.insertString(doc.getLength(), text, null);
                    // Auto-scroll to bottom
                    outputArea.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void clearOutput() {
        if (outputArea != null) {
            SwingUtilities.invokeLater(() -> {
                outputArea.setText("");
            });
        }
    }

    public String getInput() {
        return inputField != null ? inputField.getText() : "";
    }

    public void clearInput() {
        if (inputField != null) {
            inputField.setText("");
        }
    }
}