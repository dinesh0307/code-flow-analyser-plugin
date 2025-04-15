package com.dinesh.codeflowanalyser.dto;

public class ModelInfo {
    private final String displayName;
    private final String id;

    public ModelInfo(String displayName, String id) {
        this.displayName = displayName;
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return displayName;
    }
}