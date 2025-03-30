package com.dinesh.codeflowanalyser.service;


import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.Arrays;
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
     * @param className The name of the class to analyze
     * @param methodName The name of the method to analyze
     * @return List of fully qualified class names that are impacted
     */
    public List<String> getImpactedClasses(String className, String methodName) {
        // TODO: Replace this with your actual implementation that calls your Java parser
        // This is just a placeholder
        return Arrays.asList("/Users/dineshs/Documents/stock-source-codes/ArbitrageStrategy/src/main/java/ArbitrageTradingNotifier", "/Users/dineshs/Documents/stock-source-codes/ArbitrageStrategy/src/main/java/ArbitrageTradingNotifier","/Users/dineshs/Documents/stock-source-codes/ArbitrageStrategy/src/main/java/TelegramNotifier","/Users/dineshs/Documents/stock-source-codes/ArbitrageStrategy/src/main/java/ArbitrageAlertCreator");
    }
}
