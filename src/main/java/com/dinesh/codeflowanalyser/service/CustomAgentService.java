package com.dinesh.codeflowanalyser.service;

import com.dinesh.codeflowanalyser.api.ApiClient;
import com.dinesh.codeflowanalyser.api.ApiFactory;
import com.dinesh.codeflowanalyser.api.ApiType;
import com.dinesh.codeflowanalyser.dto.ModelInfo;
import com.dinesh.codeflowanalyser.exception.GenAIApiException;
import com.google.gson.JsonObject;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class CustomAgentService {
    private final Project project;

    public CustomAgentService(Project project) {
        this.project = project;
    }

    /**
     *
     * @param apiType
     * @param model
     * @param className
     * @param methodName
     * @param impactedClassesWithPrompt
     * @return
     */
    public String runAnalysis(ApiType apiType, String model, String className, String methodName, List<String> impactedClassesWithPrompt) {


        StringBuilder result = new StringBuilder();
        result.append("Analysis of class ").append(className).append(" and method ").append(methodName).append("\n\n");
        result.append("Using model: ").append(model).append("\n");
        result.append("API: ").append(apiType).append("\n\n");

        try {
            // Send the code to the selected API for analysis
            String analysisResult = sendAnalysisRequest(apiType, model, impactedClassesWithPrompt);

            if (analysisResult != null && !analysisResult.isEmpty()) {
                result.append("=== Analysis Results ===\n\n").append(analysisResult);
            } else {
                result.append("Failed to get analysis results from API.");
            }

        } catch (Exception e) {
            result.append("Error during analysis: ").append(e.getMessage());
        }

        return result.toString();
    }

    private String sendAnalysisRequest(ApiType apiType, String modelId, List<String> impactedClassesWithPrompt)
            throws GenAIApiException {

        ApiClient apiClient = ApiFactory.createClient(apiType);
        return apiClient.chatWithGenAIApi(modelId, impactedClassesWithPrompt);

    }

}