package com.dinesh.codeflowanalyser.api;

import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.dinesh.codeflowanalyser.service.AgentType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.dinesh.codeflowanalyser.util.ApiKeyManager;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OllamaClient implements ApiClient {
    private static final String MODELS_ENDPOINT = ApiKeyManager.getApiModelsURL(ApiType.OLLAMA);

    @Override
    public List<ModelInfo> fetchAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();

        // Ollama typically runs locally and may not require an API key
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_ENDPOINT))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray modelsArray = jsonObject.getAsJsonArray("models");

                for (JsonElement element : modelsArray) {
                    JsonObject modelObj = element.getAsJsonObject();
                    String name = modelObj.get("name").getAsString();
                    models.add(new ModelInfo(name, name));
                }

                if (models.isEmpty()) {
                    models.add(new ModelInfo("No models found - Is Ollama running?", ""));
                }
            } else {
                models.add(new ModelInfo("Error: " + response.statusCode(), ""));
            }
        } catch (IOException | InterruptedException e) {
            models.add(new ModelInfo("Error connecting to Ollama: " + e.getMessage(), ""));
        }

        return models;
    }

    @Override
    public String getApiKey() {
        // For Ollama, check if we have an API endpoint configured in the properties
        return ApiKeyManager.getApiKey(ApiType.OLLAMA);
    }

    @Override
    public GeneralCommandLine getGeneralCommandLine(Project project, String model) {
        GeneralCommandLine commandLine = new GeneralCommandLine("aider");
        commandLine.setWorkDirectory(project.getBasePath());

        // Add model and API key parameters
        commandLine.addParameter("--model");
        commandLine.addParameter("ollama_chat/" + model);
        //commandLine.addParameter("--ollama-api-base");
        String apiBase = ApiKeyManager.getApiBase(ApiType.OLLAMA);
        if( apiBase == null || apiBase.trim().isEmpty()){
            throw new IllegalStateException("OLLAMA_API_BASE not set");
        }
        //commandLine.addParameter(apiBase);
        Map<String, String> env = commandLine.getEnvironment();
        env.put("OLLAMA_API_BASE", apiBase);

        return commandLine;
    }

    @Override
    public String chatWithGenAIApi(String model, List<String> impactedClassesWithPrompt) throws GenAIApiException {

        HttpClient client = HttpClient.newHttpClient();
        JsonObject jsonRequest = new JsonObject();
        JsonArray prompts = new JsonArray();
        for (String prompt : impactedClassesWithPrompt) {
            JsonObject promptObj = new JsonObject();
            promptObj.addProperty("text", prompt);
            prompts.add(promptObj);
        }
        jsonRequest.addProperty("model", model);
        jsonRequest.add("prompts", prompts);
        jsonRequest.addProperty("stream", false);

        // For Ollama, we might be using a custom endpoint from the apiKey field
        String endpoint = ApiKeyManager.getApiBase(ApiType.OLLAMA) ;
        if( endpoint == null || endpoint.trim().isEmpty()){
            throw new IllegalStateException("OPENAI_API_BASE not set");
        }
        endpoint = endpoint +  "/api/generate";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new GenAIApiException("Failed to get response from Ollama API : " + e.getMessage() , e);
        }

        if (response.statusCode() == 200) {
            // Parse the response (simplified)
            int startIdx = response.body().indexOf("\"response\":\"") + 12;
            int endIdx = response.body().indexOf("\"", startIdx);
            return response.body().substring(startIdx, endIdx).replace("\\n", "\n").replace("\\\"", "\"");
        } else {
            return "Error: " + response.statusCode() + " - " + response.body();
        }
    }
}