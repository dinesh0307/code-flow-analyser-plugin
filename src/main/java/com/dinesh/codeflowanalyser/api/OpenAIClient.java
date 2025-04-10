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

public class OpenAIClient implements ApiClient {
    private static final String MODELS_ENDPOINT = "https://api.openai.com/v1/models";

    @Override
    public List<ModelInfo> fetchAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            models.add(new ModelInfo("API key not found", ""));
            return models;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODELS_ENDPOINT))
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray data = jsonObject.getAsJsonArray("data");

                for (JsonElement element : data) {
                    JsonObject modelObj = element.getAsJsonObject();
                    String id = modelObj.get("id").getAsString();

                    // Filter to include only relevant models
                    if (id.startsWith("gpt-") || id.startsWith("text-davinci-")) {
                        models.add(new ModelInfo(id, id));
                    }
                }
            } else {
                models.add(new ModelInfo("Error: " + response.statusCode(), ""));
            }
        } catch (IOException | InterruptedException e) {
            models.add(new ModelInfo("Error fetching models: " + e.getMessage(), ""));
        }

        return models;
    }

    @Override
    public String getApiKey() {
        return ApiKeyManager.getApiKey(ApiType.OPENAI);
    }
}