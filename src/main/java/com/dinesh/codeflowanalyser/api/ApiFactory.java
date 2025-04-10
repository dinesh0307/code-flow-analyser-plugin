package com.dinesh.codeflowanalyser.api;

public class ApiFactory {
    public static ApiClient createClient(ApiType apiType) {
        switch (apiType) {
            case OPENAI:
                return new OpenAIClient();
            case ANTHROPIC:
                return new AnthropicClient();
            case OLLAMA:
                return new OllamaClient();
            default:
                throw new IllegalArgumentException("Unsupported API type: " + apiType);
        }
    }
}