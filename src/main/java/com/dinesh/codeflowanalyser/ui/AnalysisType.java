package com.dinesh.codeflowanalyser.ui;

public enum AnalysisType {
    EXPLAIN_CODE("Explain code"),
    GENERATE_FLOW_DIAGRAM("Generate Flow Diagram"),
    GENERATE_SEQUENCE_DIAGRAM("Generate sequence diagram"),
    ADD_CLASSES_ONLY("Add Classes to the context");
    private final String displayName;
    AnalysisType(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }
}