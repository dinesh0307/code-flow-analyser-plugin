package com.dinesh.codeflowanalyser.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MermaidDiagramRenderer implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MermaidRendererPanel rendererPanel = new MermaidRendererPanel(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(rendererPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class MermaidRendererPanel extends JPanel {
        private final JTextArea codeArea;
        private final JButton renderButton;
        private final JEditorPane previewPane;
        private final Project project;

        public MermaidRendererPanel(Project project) {
            this.project = project;
            setLayout(new BorderLayout());

            // Mermaid code input area
            codeArea = new JTextArea();
            codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            codeArea.setText("flowchart TD\n" +
                    "    A[Christmas] -->|Get money| B(Go shopping)\n" +
                    "    B --> C{Let me think}\n" +
                    "    C -->|One| D[Laptop]\n" +
                    "    C -->|Two| E[iPhone]\n" +
                    "    C -->|Three| F[fa:fa-car Car]");
            JScrollPane scrollPane = new JScrollPane(codeArea);

            // Preview pane
            previewPane = new JEditorPane();
            previewPane.setEditable(false);
            previewPane.setContentType("text/html");
            previewPane.addHyperlinkListener(e -> {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            JScrollPane previewScrollPane = new JScrollPane(previewPane);

            // Render button
            renderButton = new JButton("Render Diagram");
            renderButton.addActionListener(this::renderAction);

            // Layout setup
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(scrollPane, BorderLayout.CENTER);
            topPanel.add(renderButton, BorderLayout.SOUTH);

            splitPane.setTopComponent(topPanel);
            splitPane.setBottomComponent(previewScrollPane);
            splitPane.setResizeWeight(0.4);

            add(splitPane, BorderLayout.CENTER);

            // Initial setup message
            previewPane.setText("<html><body><h2>Mermaid Diagram Renderer</h2>" +
                    "<p>Click 'Render Diagram' to generate an HTML file with your diagram.</p>" +
                    "<p>The generated HTML file will open in your default browser.</p></body></html>");
        }

        private void renderAction(ActionEvent e) {
            try {
                String mermaidCode = codeArea.getText();
                String html = generateHtml(mermaidCode);

                // Create a temporary file
                Path tempFile = Files.createTempFile("mermaid-diagram-", ".html");
                File htmlFile = tempFile.toFile();
                htmlFile.deleteOnExit();

                // Write HTML to the file
                try (FileWriter writer = new FileWriter(htmlFile)) {
                    writer.write(html);
                }

                // Open the file in browser
                Desktop.getDesktop().browse(htmlFile.toURI());

                // Update preview
                previewPane.setText("<html><body><h2>Diagram Generated</h2>" +
                        "<p>The diagram has been generated and opened in your browser.</p>" +
                        "<p>Location: <a href=\"" + htmlFile.toURI() + "\">" + htmlFile.getAbsolutePath() + "</a></p>" +
                        "</body></html>");

            } catch (IOException ex) {
                previewPane.setText("<html><body><h2>Error</h2><p>" + ex.getMessage() + "</p></body></html>");
                ex.printStackTrace();
            }
        }

        private String generateHtml(String mermaidCode) {
            return "<!DOCTYPE html>\n" +
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
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"mermaid\">\n" +
                    mermaidCode +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";
        }
    }
}