package com.dinesh.codeflowanalyser.api;

import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.dinesh.codeflowanalyser.util.ApiKeyManager;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;

import java.util.List;

public interface ApiClient {
    List<ModelInfo> fetchAvailableModels() throws GenAIApiException;
    String getApiKey();
    GeneralCommandLine getGeneralCommandLine(Project project, String model);

    default boolean isVirtualEnvEnabled(){
        return ApiKeyManager.isVirtualEnvironmentEnabled();
    }
}