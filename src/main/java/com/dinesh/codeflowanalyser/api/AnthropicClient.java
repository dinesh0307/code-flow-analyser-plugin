package com.dinesh.codeflowanalyser.api;

import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.dinesh.codeflowanalyser.util.ApiKeyManager;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnthropicClient implements ApiClient {
    // Anthropic doesn't have a specific endpoint to list models, so we'll hardcode the available models

    @Override
    public List<ModelInfo> fetchAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            models.add(new ModelInfo("API key not found", ""));
            return models;
        }

        // Since Anthropic doesn't have a straightforward model listing endpoint,
        // we'll use a predefined list of models
        List<String> modelIds = Arrays.asList(
                "claude-3-7-sonnet-20250219",
                "claude-3-5-haiku-20250501",
                "claude-3-5-sonnet-20250501",
                "claude-3-opus-20240229",
                "claude-3-sonnet-20240229",
                "claude-3-haiku-20240307"
        );

        for (String id : modelIds) {
            models.add(new ModelInfo(id, id));
        }

        return models;
    }

    @Override
    public String getApiKey() {
        return ApiKeyManager.getApiKey(ApiType.ANTHROPIC);
    }

    @Override
    public GeneralCommandLine getGeneralCommandLine(Project project, String model) {
        return null;
    }

    @Override
    public String chatWithGenAIApi(String model, List<String> impactedClassesWithPrompt) throws GenAIApiException {
        return "";
    }
}