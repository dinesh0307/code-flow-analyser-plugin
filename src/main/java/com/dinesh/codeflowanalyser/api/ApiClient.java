package com.dinesh.codeflowanalyser.api;

import java.util.List;

public interface ApiClient {
    List<ModelInfo> fetchAvailableModels();
    String getApiKey();
}