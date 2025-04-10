package com.dinesh.codeflowanalyser.util;

import com.dinesh.codeflowanalyser.api.ApiType;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ApiKeyManager {
    private static final String PROPERTIES_FILE = System.getProperty("user.home") + "/api_key.properties";
    private static Properties properties;
    private static boolean loaded = false;

    private static void loadProperties() {
        if (loaded) return;

        properties = new Properties();
        Path propertiesPath = Paths.get(PROPERTIES_FILE);

        try (FileInputStream fis = new FileInputStream(propertiesPath.toFile())) {
            properties.load(fis);
            loaded = true;
        } catch (IOException e) {
            System.err.println("Failed to load API keys: " + e.getMessage());
        }
    }

    public static String getApiKey(ApiType apiType) {
        loadProperties();

        String key = "";
        switch (apiType) {
            case OPENAI:
                key = properties.getProperty("openai.api_key", "");
                break;
            case ANTHROPIC:
                key = properties.getProperty("anthropic.api_key", "");
                break;
            case OLLAMA:
                key = properties.getProperty("ollama.api_key", "");
                break;
        }

        return key;
    }
}