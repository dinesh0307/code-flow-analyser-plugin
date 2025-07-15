package com.dinesh.codeflowanalyser.actions;



import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MermaidDiagramRendererTest {

    private static JEditorPane previewPane;

    public static void main(String args[]){
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
        renderAction();
    }
    private static void renderAction() {
        try {
            String mermaidCode = "flowchart TD\n" +
                    "    A[Christmas] -->|Get money| B(Go shopping)\n" +
                    "    B --> C{Let me think}\n" +
                    "    C -->|One| D[Laptop]\n" +
                    "    C -->|Two| E[iPhone]\n" +
                    "    C -->|Three| F[fa:fa-car Car]";
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

    private static String generateHtml(String mermaidCode) {
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