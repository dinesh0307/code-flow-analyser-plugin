package com.dinesh.codeflowanalyser.service;
import com.dinesh.codeflowanalyser.api.ApiClient;
import com.dinesh.codeflowanalyser.api.ApiFactory;
import com.dinesh.codeflowanalyser.api.ApiType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
@Service(Service.Level.PROJECT)
public final class CustomAgentService {
    private final Project project;
    public CustomAgentService(Project project) {
        this.project = project;
    }
    public String runAnalysis(ApiType apiType, String modelId, String className, String methodName) {
        ApiClient apiClient = ApiFactory.createClient(apiType);
        String apiKey = apiClient.getApiKey();

        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: API key not found for " + apiType;
        }

        StringBuilder result = new StringBuilder();
        result.append("Analysis of ").append(className);
        if (!methodName.isEmpty()) {
            result.append(".").append(methodName);
        }
        result.append("\n\n");

        result.append("Using model: ").append(modelId).append("\n");
        result.append("API: ").append(apiType).append("\n\n");

        // Find the class code
        try {
            String code = getClassCode(className, methodName);

            if (code == null || code.isEmpty()) {
                return result.append("Error: Could not find the specified class or method.").toString();
            }

            result.append("Code to analyze:\n```java\n").append(code).append("\n```\n\n");

            // Now send the code to the selected API for analysis
            String analysisResult = sendToApi(apiType, modelId, apiKey, code, methodName);

            if (analysisResult != null) {
                result.append("=== Analysis Results ===\n\n").append(analysisResult);
            } else {
                result.append("Failed to get analysis results from API.");
            }

        } catch (Exception e) {
            result.append("Error during analysis: ").append(e.getMessage());
        }

        return result.toString();
    }

    private String getClassCode(String className, String methodName) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project)
                .findClass(className, GlobalSearchScope.allScope(project));

        if (psiClass == null) {
            return null;
        }

        if (methodName != null && !methodName.isEmpty()) {
            // If method name is specified, return only that method
            for (PsiMethod method : psiClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getText();
                }
            }
            // Method not found
            return null;
        } else {
            // Return the entire class code
            return psiClass.getText();
        }
    }

    private String sendToApi(ApiType apiType, String modelId, String apiKey, String code, String methodName) {
        try {
            // Create a simple prompt for the API
            String prompt = "Please analyze the following " +
                    (methodName.isEmpty() ? "Java class" : "Java method") +
                    " and provide insights on code quality, potential bugs, and optimization opportunities:\n\n" + code;

            String response = apiClient.sendRequest(apiType, modelId, apiKey, prompt);
            return response;
        } catch (Exception e) {
            return "Error sending to API: " + e.getMessage();
        }
    }

    // Method to fetch available models for the given API type
    public CompletableFuture<String[]> fetchAvailableModels(ApiType apiType, String apiKey) {
        HttpClient client = HttpClient.newHttpClient();
        String endpoint = getModelListEndpoint(apiType);

        if (endpoint == null) {
            CompletableFuture<String[]> future = new CompletableFuture<>();
            future.complete(getDefaultModels(apiType));
            return future;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseModelsFromResponse(apiType, response.body());
                    } else {
                        return getDefaultModels(apiType);
                    }
                })
                .exceptionally(e -> getDefaultModels(apiType));
    }

    private String getModelListEndpoint(ApiType apiType) {
        switch (apiType) {
            case OPENAI:
                return "https://api.openai.com/v1/models";
            case ANTHROPIC:
                return "https://api.anthropic.com/v1/models";
            case OLLAMA:
                return "http://localhost:11434/api/tags"; // Assuming local Ollama server
            default:
                return null;
        }
    }

    private String[] parseModelsFromResponse(ApiType apiType, String responseBody) {
        // This is a simplified implementation
        // In a real application, you'd use a JSON parser

        switch (apiType) {
            case OPENAI:
                return new String[]{"gpt-4", "gpt-3.5-turbo"};
            case ANTHROPIC:
                return new String[]{"claude-3-opus", "claude-3-sonnet", "claude-3-haiku"};
            case OLLAMA:
                return new String[]{"llama2", "mistral", "codellama"};
            default:
                return new String[]{"default-model"};
        }
    }

    private String[] getDefaultModels(ApiType apiType) {
        switch (apiType) {
            case OPENAI:
                return new String[]{"gpt-4", "gpt-3.5-turbo"};
            case ANTHROPIC:
                return new String[]{"claude-3-opus", "claude-3-sonnet", "claude-3-haiku"};
            case OLLAMA:
                return new String[]{"llama2", "mistral", "codellama"};
            default:
                return new String[]{"default-model"};
        }
    }
}