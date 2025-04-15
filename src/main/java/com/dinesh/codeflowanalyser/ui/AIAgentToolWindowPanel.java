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
import java.util.List;

public class AIAgentToolWindowPanel extends JBPanel<AIAgentToolWindowPanel> {
    private final Project project;
    private JComboBox<AgentType> agentComboBox;
    private JComboBox<ApiType> apiComboBox;
    private JComboBox<ModelInfo> modelComboBox;
    private JTextField classNameTextField;
    private JTextField methodNameTextField;
    private JButton analyzeButton;

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

        // Add listeners
        agentComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateUIBasedOnAgent((AgentType) e.getItem());
            }
        });

        apiComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadModelsForSelectedApi();
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
            return;
        }

        if (agentType == AgentType.AIDER) {
            // Use existing Aider service
            AiderService aiderService = project.getService(AiderService.class);
            String prompt = PromptUtil.generatePromptBasedOnAnalysisTypeForAiderAgent(analysisType, className, methodName);

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
                    } catch (Exception e) {
                        customAgentOutputArea.setText("Error during analysis: " + e.getMessage());
                    } finally {
                        SwingUtilities.invokeLater(() -> setComponentsEnabled(true));
                    }
                }
            }.execute();
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