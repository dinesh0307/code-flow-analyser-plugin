package com.dinesh.codeflowanalyser.api;

public enum ApiType {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    OLLAMA("Ollama");

    private final String displayName;

    ApiType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}