package com.dinesh.codeflowanalyser.service;


import com.dinesh.codeflowanalyser.analyser.ImpactedMethodAnalyzer;
import com.dinesh.codeflowanalyser.parser.CodeParser;
import com.dinesh.codeflowanalyser.ui.AnalysisType;
import com.dinesh.codeflowanalyser.util.PromptUtil;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.util.List;

/**
 * Interface to your existing Java Parser code.
 * Implement this to connect with your existing code.
 */
@Service(Service.Level.PROJECT)
public final class JavaParserService {
    private final Project project;

    public JavaParserService(Project project) {
        this.project = project;
    }

    /**
     * Call your existing Java parser to get impacted classes
     *
     * @param className    The name of the class to analyze
     * @param methodName   The name of the method to analyze
     * @param agentType
     * @param analysisType
     * @return List of fully qualified class names that are impacted
     */
    public List<String> getImpactedClasses(String className, String methodName, AgentType agentType, AnalysisType analysisType) {
        String rootDirectory = project.getBasePath();
        CodeParser codeParser = new CodeParser();
        List<String> impactedList = null;
        try {
            // 1. Initial setup to set code mappings
            codeParser.parse(rootDirectory);

            //On demand code analysis
            ImpactedMethodAnalyzer impactedMethodAnalyzer = codeParser.getImpactedMethodAnalyzer();

            impactedMethodAnalyzer.prepareImpactedMethods(className, methodName);

            if(agentType == AgentType.CUSTOM_AGENT){

                String mainPrompt = PromptUtil.generatePromptBasedOnAnalysisTypeForCustomAgent(analysisType, className, methodName);
                String subPrompt = "Include the below code snippet in your analysis from class ";

                // For GPT API prompt preparation
                impactedList = impactedMethodAnalyzer.getImpactedCodeBlocksForGPTRequest(mainPrompt, subPrompt);
            } else if (agentType == AgentType.AIDER) {
                impactedList = impactedMethodAnalyzer.getImpactedFilesList(codeParser.getClassNameToAbsolutePathMap());
            }

            // After LLM response

            //LLMManager.invokeLLM(impactedCodeBlocks, className, method);
        } catch (IOException e) {
            throw new RuntimeException("Java parser failed : " + e.getMessage(), e);
        }
        return impactedList;
    }
}
