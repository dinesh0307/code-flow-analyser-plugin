package com.dinesh.codeflowanalyser.util;

import com.dinesh.codeflowanalyser.ui.AnalysisType;

public final class PromptUtil {
    public static String generatePromptBasedOnAnalysisTypeForAiderAgent(AnalysisType analysisType, String className, String methodName) {
        switch (analysisType) {
            case EXPLAIN_CODE:
                return "Explain the code functionality and logic flow starting from class " + className +
                        " and method " + methodName + ". Provide details about what the code does, " +
                        "important classes and methods it interacts with and go iteratively on all the impacted classes till the last class in the flow, and any key algorithms or patterns used." +
                        "Include service layer, utility classes if any, and dao layer as appropriate." +
                        "And give me two lists at the end, one list for list of classes you considered for analysis and another list for " +
                        "list of classes you didn't consider for analysis and provide me the reason why you didn't consider";

            case GENERATE_FLOW_DIAGRAM:
                return "Generate a flow diagram for the code in dot format compatible with mermaid starting from class name" +
                        className + " and method name " + methodName + ". Show the logical flow of execution, decision points, " +
                        "and important control structures. Include service layer, utility classes if any, and dao layer as appropriate." +
                        "And give me two lists at the end, one list for list of classes you considered for analysis and another list for " +
                        "list of classes you didn't consider for analysis and provide me the reason why you didn't consider";

            case GENERATE_SEQUENCE_DIAGRAM:
            default:
                return "Generate the sequence diagram for the code in dot format compatible with mermaid starting from class name " +
                        className + " and method name " + methodName + " and cover the service layer, utility classes if any and dao layer as well. " +
                        "And give me two lists at the end, one list for list of classes you considered for analysis and another list for " +
                        "list of classes you didn't consider for analysis and provide me the reason why you didn't consider";

        }
    }

    public static String generatePromptBasedOnAnalysisTypeForCustomAgent(AnalysisType analysisType, String className, String methodName) {
        switch (analysisType) {
            case EXPLAIN_CODE:
                return "Explain the code functionality and logic flow starting from class " + className +
                        " and method " + methodName + ". Provide details about what the code does, " +
                        "important classes and methods it interacts with and go iteratively on all the impacted classes till the last class in the flow, and any key algorithms or patterns used." +
                        "Include service layer, utility classes if any, and dao layer as appropriate." +
                        " All the code blocks are given in subsequent prompts. Don't jump into generating sequence diagram before reading all the below subsequent prompts. " +
                        "And give me two lists at the end, one list for list of classes you considered for analysis and another list for " +
                        "list of classes you didn't consider for analysis and provide me the reason why you didn't consider";


            case GENERATE_FLOW_DIAGRAM:
                return "Generate a flow diagram for the code in dot format compatible with mermaid starting from class name" +
                        className + " and method name " + methodName + ". Show the logical flow of execution, decision points, " +
                        "and important control structures. Include service layer, utility classes if any, and dao layer as appropriate." +
                        " All the code blocks are given in subsequent prompts. Don't jump into generating sequence diagram before reading all the below subsequent prompts. " +
                        "And give me two lists at the end, one list for list of classes you considered for analysis and another list for " +
                        "list of classes you didn't consider for analysis and provide me the reason why you didn't consider";

            case GENERATE_SEQUENCE_DIAGRAM:
            default:
                return "Generate the sequence diagram for the code in dot format compatible with mermaid starting from class name " +
                        className + " and method name " + methodName + " and cover the service layer, utility classes if any and dao layer as well." +
                        " All the code blocks are given in subsequent prompts. Don't jump into generating sequence diagram before reading all the below subsequent prompts. " +
                        "And give me two lists at the end, one list for list of classes you considered for analysis and another list for list of classes you didn't consider " +
                        "for analysis and provide me the reason why you didn't consider";

        }
    }
}
