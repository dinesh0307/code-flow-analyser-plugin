package com.dinesh.codeflowanalyser.genai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TokenHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractAccessToken(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("access_token").asText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String extractApplicationName(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode applicationsNode = rootNode.path("applications").get(0);
            return applicationsNode.path("application_name").asText();
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
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
}
