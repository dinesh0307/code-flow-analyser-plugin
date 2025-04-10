package com.dinesh.codeflowanalyser.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.dinesh.codeflowanalyser.util.ApiKeyManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class OllamaClient implements ApiClient {
    private static final String MODELS_ENDPOINT = "http://localhost:11434/api/tags";

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
}