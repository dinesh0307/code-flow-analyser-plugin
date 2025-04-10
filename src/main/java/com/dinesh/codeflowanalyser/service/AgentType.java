package com.dinesh.codeflowanalyser.service;

public enum AgentType {
    AIDER("Aider"),
    CUSTOM_AGENT("Custom Agent");

    private final String displayName;

    AgentType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}