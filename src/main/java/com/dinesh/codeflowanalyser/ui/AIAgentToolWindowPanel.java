package com.dinesh.codeflowanalyser.ui;

import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.dinesh.codeflowanalyser.service.*;
import com.dinesh.codeflowanalyser.api.ApiType;
import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.api.ApiClient;
import com.dinesh.codeflowanalyser.api.ApiFactory;
import com.dinesh.codeflowanalyser.util.PromptUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;

public class AIAgentToolWindowPanel extends JBPanel<AIAgentToolWindowPanel> {
    private final Project project;
    private JComboBox<AgentType> agentComboBox;
    private JComboBox<ApiType> apiComboBox;
    private JComboBox<ModelInfo> modelComboBox;
    private JTextField classNameTextField;
    private JTextField methodNameTextField;
    private JButton analyzeButton;
    private JButton displayDiagramButton; // New button for displaying diagrams

    // Panel for custom agent output
    private JPanel customAgentPanel;
    private JTextArea customAgentOutputArea;

    // Panel for Aider (will use AiderToolWindowFactory's UI)
    private JPanel aiderContainerPanel;
    private AiderToolWindowContent aiderContent;
    private JComboBox<AnalysisType> analysisTypeComboBox;

    public AIAgentToolWindowPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));

        // Create the top configuration panel
        JPanel configPanel = createConfigPanel();

        // Create a splitter for the main area
        JBSplitter splitter = new JBSplitter(true, 0.3f);
        splitter.setFirstComponent(configPanel);

        // Create a card layout for switching between agent UIs
        JPanel agentUIPanel = new JPanel(new CardLayout());

        // Add Custom Agent panel
        customAgentPanel = createCustomAgentPanel();
        agentUIPanel.add(customAgentPanel, AgentType.CUSTOM_AGENT.toString());

        // Add Aider panel
        aiderContainerPanel = new JPanel(new BorderLayout());
        aiderContent = new AiderToolWindowContent(project);
        aiderContainerPanel.add(aiderContent.getContent(), BorderLayout.CENTER);
        agentUIPanel.add(aiderContainerPanel, AgentType.AIDER.toString());

        splitter.setSecondComponent(agentUIPanel);

        add(splitter, BorderLayout.CENTER);

        // Set initial state
        updateUIBasedOnAgent((AgentType) agentComboBox.getSelectedItem());
        updateDisplayDiagramButtonVisibility();
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5);

        // Agent selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JBLabel("Agent:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        agentComboBox = new ComboBox<>(AgentType.values());
        panel.add(agentComboBox, gbc);

        // API selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JBLabel("API:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        apiComboBox = new ComboBox<>(ApiType.values());
        panel.add(apiComboBox, gbc);

        // Model selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JBLabel("Model:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        modelComboBox = new ComboBox<>();
        panel.add(modelComboBox, gbc);

        // Analysis Type selection (new dropdown)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JBLabel("Analysis Type:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        analysisTypeComboBox = new ComboBox<>(AnalysisType.values());
        panel.add(analysisTypeComboBox, gbc);

        // Class name input
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        panel.add(new JBLabel("Class Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        classNameTextField = new JBTextField();
        panel.add(classNameTextField, gbc);

        // Method name input
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        panel.add(new JBLabel("Method Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        methodNameTextField = new JBTextField();
        panel.add(methodNameTextField, gbc);

        // Analyze button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        analyzeButton = new JButton("Analyze");
        panel.add(analyzeButton, gbc);

        // Display Diagram button (initially invisible)
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        displayDiagramButton = new JButton("Display Diagram");
        displayDiagramButton.setVisible(false);
        displayDiagramButton.setEnabled(false);
        displayDiagramButton.addActionListener(e -> displayMermaidDiagram());
        panel.add(displayDiagramButton, gbc);

        // Add listeners
        agentComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateUIBasedOnAgent((AgentType) e.getItem());
            }
            if(AgentType.CUSTOM_AGENT == (AgentType)agentComboBox.getSelectedItem()){
                analysisTypeComboBox.removeItem(AnalysisType.ADD_CLASSES_ONLY);
            }else {
                AnalysisType addClass = analysisTypeComboBox.getItemAt(3);
                if(addClass == null){
                    analysisTypeComboBox.addItem(AnalysisType.ADD_CLASSES_ONLY);
                }
            }
            updateDisplayDiagramButtonVisibility();
        });

        apiComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadModelsForSelectedApi();
            }
        });

        analysisTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateDisplayDiagramButtonVisibility();
            }
        });

        analyzeButton.addActionListener(e -> performAnalysis());

        // Initialize with defaults
        loadModelsForSelectedApi();

        return panel;
    }

    private JPanel createCustomAgentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Analysis Results"));

        customAgentOutputArea = new JTextArea();
        customAgentOutputArea.setEditable(false);
        customAgentOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(customAgentOutputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateUIBasedOnAgent(AgentType agentType) {
        // Switch the visible panel based on agent type
        CardLayout cardLayout = (CardLayout) ((JPanel) aiderContainerPanel.getParent()).getLayout();
        cardLayout.show((Container) aiderContainerPanel.getParent(), agentType.toString());
    }

    private void updateDisplayDiagramButtonVisibility() {
        AnalysisType analysisType = (AnalysisType) analysisTypeComboBox.getSelectedItem();
        AgentType agentType = (AgentType) agentComboBox.getSelectedItem();

        // Show button only for CUSTOM_AGENT with flow or sequence diagram analysis types
        boolean shouldShow = agentType == AgentType.CUSTOM_AGENT &&
                (analysisType == AnalysisType.GENERATE_FLOW_DIAGRAM ||
                        analysisType == AnalysisType.GENERATE_SEQUENCE_DIAGRAM);

        displayDiagramButton.setVisible(shouldShow);
        // Button will be enabled after analysis completes
        displayDiagramButton.setEnabled(false);
    }

    private void loadModelsForSelectedApi() {
        ApiType selectedApiType = (ApiType) apiComboBox.getSelectedItem();
        if (selectedApiType == null) return;

        modelComboBox.removeAllItems();

        // Show loading indicator
        SwingUtilities.invokeLater(() -> {
            modelComboBox.addItem(new ModelInfo("Loading models...", ""));
            modelComboBox.setEnabled(false);
        });

        // Load models asynchronously
        new SwingWorker<List<ModelInfo>, Void>() {
            @Override
            protected List<ModelInfo> doInBackground() throws GenAIApiException {
                ApiClient apiClient = ApiFactory.createClient(selectedApiType);
                return apiClient.fetchAvailableModels();
            }

            @Override
            protected void done() {
                try {
                    List<ModelInfo> models = get();
                    SwingUtilities.invokeLater(() -> {
                        modelComboBox.removeAllItems();
                        for (ModelInfo model : models) {
                            modelComboBox.addItem(model);
                        }
                        modelComboBox.setEnabled(true);
                    });
                } catch (Exception e) {
                    // Handle error
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                AIAgentToolWindowPanel.this, // Replace with the appropriate parent component
                                e.getMessage(),
                                "Loading models error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        modelComboBox.removeAllItems();
                        modelComboBox.addItem(new ModelInfo("Error loading models", ""));
                        modelComboBox.setEnabled(false);
                    });
                }
            }
        }.execute();
    }

    private void performAnalysis() {
        AgentType agentType = (AgentType) agentComboBox.getSelectedItem();
        ApiType apiType = (ApiType) apiComboBox.getSelectedItem();
        ModelInfo model = (ModelInfo) modelComboBox.getSelectedItem();
        AnalysisType analysisType = (AnalysisType) analysisTypeComboBox.getSelectedItem();
        String className = classNameTextField.getText().trim();
        String methodName = methodNameTextField.getText().trim();

        if (className.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a class name.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (methodName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a method name.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (model == null || model.getDisplayName().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a valid model.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Reset the Display Diagram button
        if (displayDiagramButton.isVisible()) {
            displayDiagramButton.setEnabled(false);
        }

        // Disable UI while processing
        setComponentsEnabled(false);

        final List<String> impactedClasses;
        try {
            JavaParserService javaParserService = project.getService(JavaParserService.class);
            impactedClasses = javaParserService.getImpactedClasses(className, methodName, agentType, analysisType);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Java Parser Exception",
                    JOptionPane.ERROR_MESSAGE);
            setComponentsEnabled(true);
            return;
        }

        if (agentType == AgentType.AIDER) {
            // Use existing Aider service
            AiderService aiderService = project.getService(AiderService.class);
            String prompt = null;
            if(analysisType != AnalysisType.ADD_CLASSES_ONLY){
                prompt = PromptUtil.generatePromptBasedOnAnalysisTypeForAiderAgent(analysisType, className, methodName);
            }

            aiderService.startAiderSession(
                    apiType, model.getDisplayName(),
                    impactedClasses,
                    prompt,
                    output -> {
                        AiderOutputHandler outputHandler = project.getService(AiderOutputHandler.class);
                        outputHandler.appendOutput(output);
                    } // The AiderOutputHandler already handles the output
            ).whenComplete((result, ex) -> {
                SwingUtilities.invokeLater(() -> setComponentsEnabled(true));
                if (ex != null) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this,
                                    "Error starting Aider session: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE)
                    );
                } else {
                    updateUIBasedOnAgent(AgentType.AIDER);
                    AiderOutputHandler outputHandler = project.getService(AiderOutputHandler.class);
                    outputHandler.setInputEnabled(true);
                }
            });
        } else {
            // Run custom agent analysis
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    CustomAgentService customAgentService = project.getService(CustomAgentService.class);
                    return customAgentService.runAnalysis(
                            apiType,
                            model.getDisplayName(),
                            className,
                            methodName,
                            impactedClasses
                    );
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        customAgentOutputArea.setText(result);

                        // Enable diagram button if we have diagram code in the result
                        if (isDiagramAnalysisType() && containsMermaidDiagram(result)) {
                            displayDiagramButton.setEnabled(true);
                        }
                    } catch (Exception e) {
                        customAgentOutputArea.setText("Error during analysis: " + e.getMessage());
                    } finally {
                        SwingUtilities.invokeLater(() -> setComponentsEnabled(true));
                    }
                }
            }.execute();
        }
    }

    private boolean isDiagramAnalysisType() {
        AnalysisType analysisType = (AnalysisType) analysisTypeComboBox.getSelectedItem();
        return analysisType == AnalysisType.GENERATE_FLOW_DIAGRAM ||
                analysisType == AnalysisType.GENERATE_SEQUENCE_DIAGRAM;
    }

    private boolean containsMermaidDiagram(String text) {
        // Look for code blocks that might contain a mermaid diagram
        return text.contains("```") &&
                (text.contains("flowchart") || text.contains("sequenceDiagram") ||
                        text.contains("graph") || text.contains("classDiagram"));
    }

    private void displayMermaidDiagram() {
        String outputText = customAgentOutputArea.getText();
        String mermaidCode = extractMermaidDiagram(outputText);

        if (mermaidCode != null && !mermaidCode.isEmpty()) {
            showMermaidDiagramDialog(mermaidCode);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No valid Mermaid diagram found in the output.",
                    "Diagram Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractMermaidDiagram(String text) {
        // Pattern to match content between triple backticks
        Pattern pattern = Pattern.compile("```(?:mermaid)?\\s*([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);

        // Find first match that looks like a mermaid diagram
        while (matcher.find()) {
            String potentialDiagram = matcher.group(1).trim();
            if (potentialDiagram.contains("flowchart") || potentialDiagram.contains("sequenceDiagram") ||
                    potentialDiagram.contains("graph") || potentialDiagram.contains("classDiagram")) {
                return potentialDiagram;
            }
        }
        return null;
    }

    /*private void showMermaidDiagramDialog(String mermaidCode) {
        try {
            // Create a temporary HTML file with the Mermaid diagram
            Path tempFile = Files.createTempFile("mermaid-diagram-", ".html");
            File htmlFile = tempFile.toFile();
            htmlFile.deleteOnExit();

            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Mermaid Diagram</title>\n" +
                    "    <script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>\n" +
                    "    <script>\n" +
                    "        mermaid.initialize({startOnLoad: true, theme: 'default'});\n" +
                    "    </script>\n" +
                    "    <style>\n" +
                    "        body { font-family: sans-serif; margin: 20px; }\n" +
                    "        .diagram-container { width: 100%; overflow: auto; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"diagram-container\">\n" +
                    "        <div class=\"mermaid\">\n" +
                    mermaidCode +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

            // Write the HTML to the file
            try (FileWriter writer = new FileWriter(htmlFile)) {
                writer.write(html);
            }

            // Create and setup the dialog
            JDialog dialog = new JDialog();
            dialog.setTitle("Mermaid Diagram Viewer");
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);

            // Create web viewer inside the dialog
            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.setContentType("text/html");

            // Load the HTML file
            editorPane.setPage(htmlFile.toURI().toURL());

            JScrollPane scrollPane = new JScrollPane(editorPane);
            dialog.add(scrollPane);

            // Close button at the bottom
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);

            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Show the dialog
            dialog.setVisible(true);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error displaying diagram: " + e.getMessage(),
                    "Diagram Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }*/

    private void showMermaidDiagramDialog(String mermaidCode) {
        try {
            // Create a temporary HTML file with the Mermaid diagram
            Path tempFile = Files.createTempFile("mermaid-diagram-", ".html");
            File htmlFile = tempFile.toFile();
            htmlFile.deleteOnExit();

            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Mermaid Diagram</title>\n" +
                    "    <script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>\n" +
                    "    <script>\n" +
                    "        mermaid.initialize({startOnLoad: true, theme: 'default'});\n" +
                    "    </script>\n" +
                    "    <style>\n" +
                    "        body { font-family: sans-serif; margin: 20px; }\n" +
                    "        .diagram-container { width: 100%; overflow: auto; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"diagram-container\">\n" +
                    "        <div class=\"mermaid\">\n" +
                    mermaidCode +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

            // Write the HTML to the file
            try (FileWriter writer = new FileWriter(htmlFile)) {
                writer.write(html);
            }

            // Check if JCEF is supported
            if (!JBCefApp.isSupported()) {
                throw new RuntimeException("JCEF is not supported in this environment");
            }

            // Create and setup the dialog
            JDialog dialog = new JDialog();
            dialog.setTitle("Mermaid Diagram Viewer");
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);

            // Use JCEF browser
            JBCefBrowser browser = JBCefBrowser.createBuilder()
                    .setUrl(htmlFile.toURI().toString())
                    .build();

            dialog.add(browser.getComponent(), BorderLayout.CENTER);

            // Close button at the bottom
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Show the dialog
            dialog.setVisible(true);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error displaying diagram: " + e.getMessage(),
                    "Diagram Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setComponentsEnabled(boolean enabled) {
        agentComboBox.setEnabled(enabled);
        apiComboBox.setEnabled(enabled);
        modelComboBox.setEnabled(enabled);
        analysisTypeComboBox.setEnabled(enabled);
        classNameTextField.setEnabled(enabled);
        methodNameTextField.setEnabled(enabled);
        analyzeButton.setEnabled(enabled);
    }
}