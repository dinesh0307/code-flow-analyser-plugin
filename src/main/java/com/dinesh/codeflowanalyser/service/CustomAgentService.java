package com.dinesh.codeflowanalyser.service;

import com.dinesh.codeflowanalyser.api.ApiClient;
import com.dinesh.codeflowanalyser.api.ApiFactory;
import com.dinesh.codeflowanalyser.api.ApiType;
import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.google.gson.JsonObject;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class CustomAgentService {
    private final Project project;

    public CustomAgentService(Project project) {
        this.project = project;
    }

    /**
     * Analyze the provided code using the specified AI service
     *
     * @param apiType The type of API to use (OpenAI, Anthropic, etc.)
     * @param modelId The specific model to use
     * @param codeToAnalyze The actual code to be analyzed
     * @param contextInfo Additional context about the code (e.g., class name, method name)
     * @return Analysis result as a string
     */
    public String runAnalysis(ApiType apiType, String modelId, String codeToAnalyze, String contextInfo) {
        ApiClient apiClient = ApiFactory.createClient(apiType);
        String apiKey = apiClient.getApiKey();

        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: API key not found for " + apiType;
        }

        StringBuilder result = new StringBuilder();
        result.append("Analysis of ").append(contextInfo).append("\n\n");
        result.append("Using model: ").append(modelId).append("\n");
        result.append("API: ").append(apiType).append("\n\n");

        try {
            // Display the code to be analyzed
            result.append("Code to analyze:\n```java\n").append(codeToAnalyze).append("\n```\n\n");

            // Send the code to the selected API for analysis
            String analysisResult = sendAnalysisRequest(apiType, modelId, apiKey, codeToAnalyze, contextInfo);

            if (analysisResult != null && !analysisResult.isEmpty()) {
                result.append("=== Analysis Results ===\n\n").append(analysisResult);
            } else {
                result.append("Failed to get analysis results from API.");
            }

        } catch (Exception e) {
            result.append("Error during analysis: ").append(e.getMessage());
        }

        return result.toString();
    }

    private String sendAnalysisRequest(ApiType apiType, String modelId, String apiKey, String code, String contextInfo)
            throws IOException, InterruptedException {
        // Create a prompt for the API based on the provided code and context
        String prompt = "Please analyze the following Java code" +
                (contextInfo != null && !contextInfo.isEmpty() ? " (" + contextInfo + ")" : "") +
                " and provide insights on code quality, potential bugs, and optimization opportunities:\n\n" + code;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;

        switch (apiType) {
            case OPENAI:
                return sendOpenAIRequest(client, modelId, apiKey, prompt);
            case ANTHROPIC:
                return sendAnthropicRequest(client, modelId, apiKey, prompt);
            case OLLAMA:
                return sendOllamaRequest(client, modelId, apiKey, prompt);
            default:
                return "Unsupported API type: " + apiType;
        }
    }

    private String sendOpenAIRequest(HttpClient client, String modelId, String apiKey, String prompt)
            throws IOException, InterruptedException {
        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("model", modelId);
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        JsonObject messages = new JsonObject();
        jsonRequest.add("messages", message);
        jsonRequest.addProperty("temperature", 0.7);
        jsonRequest.addProperty("max_tokens", 2048);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse the response (simplified)
            // In a real implementation, use proper JSON parsing
            int startIdx = response.body().indexOf("\"content\":\"") + 11;
            int endIdx = response.body().indexOf("\"", startIdx);
            return response.body().substring(startIdx, endIdx).replace("\\n", "\n").replace("\\\"", "\"");
        } else {
            return "Error: " + response.statusCode() + " - " + response.body();
        }
    }

    private String sendAnthropicRequest(HttpClient client, String modelId, String apiKey, String prompt)
            throws IOException, InterruptedException {
        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("model", modelId);
        jsonRequest.addProperty("prompt", prompt);
        jsonRequest.addProperty("max_tokens_to_sample", 2048);
        jsonRequest.addProperty("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/complete"))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse the response (simplified)
            int startIdx = response.body().indexOf("\"completion\":\"") + 14;
            int endIdx = response.body().indexOf("\"", startIdx);
            return response.body().substring(startIdx, endIdx).replace("\\n", "\n").replace("\\\"", "\"");
        } else {
            return "Error: " + response.statusCode() + " - " + response.body();
        }
    }

    private String sendOllamaRequest(HttpClient client, String modelId, String apiKey, String prompt)
            throws IOException, InterruptedException {
        JsonObject jsonRequest = new JsonObject();
        jsonRequest.addProperty("model", modelId);
        jsonRequest.addProperty("prompt", prompt);
        jsonRequest.addProperty("stream", false);

        // For Ollama, we might be using a custom endpoint from the apiKey field
        String endpoint = apiKey.startsWith("http") ? apiKey : "http://localhost:11434/api/generate";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse the response (simplified)
            int startIdx = response.body().indexOf("\"response\":\"") + 12;
            int endIdx = response.body().indexOf("\"", startIdx);
            return response.body().substring(startIdx, endIdx).replace("\\n", "\n").replace("\\\"", "\"");
        } else {
            return "Error: " + response.statusCode() + " - " + response.body();
        }
    }

    /**
     * Fetch available models for the given API type
     */
    public List<ModelInfo> fetchAvailableModels(ApiType apiType) throws GenAIApiException {
        ApiClient apiClient = ApiFactory.createClient(apiType);
        return apiClient.fetchAvailableModels();
    }
}