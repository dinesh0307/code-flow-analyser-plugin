package com.dinesh.codeflowanalyser.util;


import com.github.javaparser.ast.body.MethodDeclaration;

public class CodeParserUtil {

    public static String getMethodName(MethodDeclaration method) {
        StringBuilder methodIdentifier = new StringBuilder();
        methodIdentifier.append(method.getNameAsString());
        methodIdentifier.append("(");
        method.getParameters().forEach(param -> {
            methodIdentifier.append(param.getTypeAsString()).append(",");
        });
        if (method.getParameters().size() > 0) {
            methodIdentifier.setLength(methodIdentifier.length() - 1); // Remove the last comma
        }
        methodIdentifier.append(")");
        return methodIdentifier.toString();
    }
}
