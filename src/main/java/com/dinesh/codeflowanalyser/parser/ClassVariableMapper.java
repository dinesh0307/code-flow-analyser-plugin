package com.dinesh.codeflowanalyser.parser;



import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.dinesh.codeflowanalyser.util.CodeParserUtil;

import java.util.*;

public class ClassVariableMapper {

    private Map<String, Map<String, String>> classToVariableMap = new HashMap<>();

    private Map<String, Map<String, Map<String, String>>> classToMethodToVariableMap = new HashMap<>();

    public void populateClassToVariableMap(CompilationUnit cu) {
        if (cu != null) {
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(FieldDeclaration fieldDeclaration, Void arg) {
                    super.visit(fieldDeclaration, arg);
                    String className = fieldDeclaration.findAncestor(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class)
                            .map(c -> c.getNameAsString()).orElse(null);
                    if (className != null) {
                        classToVariableMap.putIfAbsent(className, new HashMap<>());
                        for (VariableDeclarator variable : fieldDeclaration.getVariables()) {
                            classToVariableMap.get(className).put(variable.getNameAsString(), variable.getTypeAsString());
                        }
                    }
                }
            }, null);

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
                    super.visit(cid, arg);

                    String className = cid.getNameAsString();
                    if (className != null) {
                        classToMethodToVariableMap.putIfAbsent(className, new HashMap<>());
                        Map<String, Map<String, String>> methodToVariableMap = classToMethodToVariableMap.get(className);
                        cid.getMethods().forEach(method -> {
                            String methodName = method.getNameAsString();
                            methodToVariableMap.putIfAbsent(methodName, new HashMap<>());
                            Map<String, String> variableToClassNameMap = methodToVariableMap.get(methodName);
                            method.getParameters().forEach(param ->
                                    variableToClassNameMap.put(param.getNameAsString(), param.getTypeAsString()));
                        });
                    }
                }
            }, null);
        }
    }

    public Map<String, Map<String, String>> getClassToVariableMap() {
        return classToVariableMap;
    }

    public Map<String, Map<String, Map<String, String>>> getClassToMethodToVariableMap() {
        return classToMethodToVariableMap;
    }
}