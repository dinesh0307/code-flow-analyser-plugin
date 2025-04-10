package com.dinesh.codeflowanalyser.analyser;


import com.dinesh.codeflowanalyser.genai.LLMManager;
import com.dinesh.codeflowanalyser.parser.CodeParser;

import java.io.IOException;
import java.util.List;

public class CodeAnalyzer {
    public static void main(String[] args) {
        String rootDirectory = "<Code base root path>";
        CodeParser codeParser = new CodeParser();
        try {
            // 1. Initial setup to set code mappings
            codeParser.parse(rootDirectory);

            //On demand code analysis
            ImpactedMethodAnalyzer impactedMethodAnalyzer = codeParser.getImpactedMethodAnalyzer();

            String className = "SrcCheckoutController";
            String method = "enrollAndCheckout";
            impactedMethodAnalyzer.prepareImpactedMethods(className, method);

            //String mainPrompt = "Explain me the functionality of given code starting with class name " + className + " and method name " + method + " and cover the service layer, utility classes if any and dao layer as well. All the code blocks are given in subsequent prompts. Don't jump into explanation before reading all the below subsequent prompts.. And give me two lists at the end, one list for list of classes you considered for analysis and another list for list of classes you didn't consider for analysis and provide me the reason why you didn't consider";
            String mainPrompt = "Generate the sequence diagram for the code in dot format compatible with mermaid starting from class name " + className + " and method name " + method + " and cover the service layer, utility classes if any and dao layer as well. All the code blocks are given in subsequent prompts. Don't jump into generating sequence diagram before reading all the below subsequent prompts.. And give me two lists at the end, one list for list of classes you considered for analysis and another list for list of classes you didn't consider for analysis and provide me the reason why you didn't consider";
            String subPrompt = "Include the below code snippet in your analysis from class ";

            // For GPT API prompt preparation
            List<String> impactedCodeBlocks = impactedMethodAnalyzer.getImpactedCodeBlocksForGPTRequest(mainPrompt, subPrompt);

            // After LLM response
            //List<String> impactedFileList = impactedMethodAnalyzer.getImpactedFileList(codeParser.getClassNameToAbsolutePathMap());

            LLMManager.invokeLLM(impactedCodeBlocks, className, method);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
