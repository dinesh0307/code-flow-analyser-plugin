package com.dinesh.codeflowanalyser.util;

import com.dinesh.codeflowanalyser.api.ApiType;
import com.dinesh.codeflowanalyser.dto.Credential;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ApiKeyManager {
    private static final String PROPERTIES_FILE = System.getProperty("user.home") + "/api_key.properties";
    private static Properties properties;
    //private static boolean loaded = false;

    static {
        properties = new Properties();
        Path propertiesPath = Paths.get(PROPERTIES_FILE);

        try (FileInputStream fis = new FileInputStream(propertiesPath.toFile())) {
            properties.load(fis);
            //loaded = true;
        } catch (IOException e) {
            System.err.println("Failed to load API keys: " + e.getMessage());
        }
    }


    public static String getApiKey(ApiType apiType) {
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

    public static String getApiBase(ApiType apiType) {
        String apiBase = "";
        switch (apiType) {
            case OPENAI:
                apiBase = properties.getProperty("OPENAI_API_BASE", "");
                break;
            case ANTHROPIC:
                apiBase = properties.getProperty("ANTHROPIC_API_BASE", "");
                break;
            case OLLAMA:
                apiBase = properties.getProperty("OLLAMA_API_BASE", "");
                break;
        }

        return apiBase;
    }

    public static String getApiModelsURL(ApiType apiType) {
        String modelsUrl = "";
        switch (apiType) {
            case OPENAI:
                modelsUrl = properties.getProperty("OPENAI_MODELS_URL", "");
                break;
            case ANTHROPIC:
                modelsUrl = properties.getProperty("ANTHROPIC_MODELS_URL", "");
                break;
            case OLLAMA:
                modelsUrl = properties.getProperty("OLLAMA_MODELS_URL", "");
                break;
        }

        return modelsUrl;
    }

    public static Credential getApiCredentials(ApiType apiType) {
        String userName = "";
        String password = "";
        switch (apiType) {
            case OPENAI:
                userName = properties.getProperty("OPENAI_USER", "");
                password = properties.getProperty("OPENAI_PASSWORD", "");
                break;
            case ANTHROPIC:
                userName = properties.getProperty("ANTHROPIC_USER", "");
                password = properties.getProperty("ANTHROPIC_PASSWORD", "");
                break;
            case OLLAMA:
                break;
        }

        return new Credential(userName, password);
    }

    public static boolean isVirtualEnvironmentEnabled(){
        return Boolean.parseBoolean(properties.getProperty("ENABLE_VIRTUAL_ENV"));
    }

    public static String getAiderVirtualEnvPath(){
        return properties.getProperty("AIDER_VIRTUAL_ENV_PATH");
    }
}