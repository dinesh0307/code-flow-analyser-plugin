package com.dinesh.codeflowanalyser.genai;

import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TokenHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractAccessToken(String jsonResponse) throws GenAIApiException {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("access_token").asText();
        } catch (IOException e) {
            throw new GenAIApiException("Failed to extract access token : " + e.getMessage(), e);
        }
    }

    public static String extractApplicationName(String jsonResponse) throws GenAIApiException {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode applicationsNode = rootNode.path("applications").get(0);
            return applicationsNode.path("application_name").asText();
        } catch (IOException | IndexOutOfBoundsException e) {
            throw new GenAIApiException("Failed to extract application name : " + e.getMessage(), e);
        }
    }

    public static String[] parseModelNames(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode modelsNode = rootNode.path("models");
            String[] modelNames = new String[modelsNode.size()];

            for (int i = 0; i < modelsNode.size(); i++) {
                modelNames[i] = modelsNode.get(i).path("model_name").asText();
            }
            return modelNames;
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static List<ModelInfo> parseModelNamesWithId(String jsonResponse) throws GenAIApiException {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode modelsNode = rootNode.path("models");
            List<ModelInfo> modelInfos = new LinkedList<>();

            for (int i = 0; i < modelsNode.size(); i++) {
                String modelName = modelsNode.get(i).path("model_name").asText();
                String id = modelsNode.get(i).path("id").asText();
                modelInfos.add(new ModelInfo(modelName, id));
            }
            return modelInfos;
        } catch (IOException e) {
            throw new GenAIApiException("Failed to retrieve models. " + e.getMessage(), e);
        }
    }
}
