package com.dinesh.codeflowanalyser.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.dinesh.codeflowanalyser.util.CodeParserUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassToMethodCallsMapper {

    private Map<String, Map<String, List<String>>> classToMethodToMethodCallsMap = new HashMap<>();

    public void populateClassToMethodCallsMap(CompilationUnit cu) {
        if(cu == null){
            return;
        }
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration methodDeclaration, Void arg) {
                super.visit(methodDeclaration, arg);
                String className = methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class).map(c -> c.getNameAsString()).orElse(null);

                if(className != null){
                    classToMethodToMethodCallsMap.putIfAbsent(className, new HashMap<>());
                    List<String> methodCalls = new ArrayList<>();
                    methodDeclaration.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(MethodCallExpr methodCall, Void arg) {
                            super.visit(methodCall, arg);
                            methodCalls.add(methodCall.toString());
                        }
                    }, null);
                    String methodName = methodDeclaration.getNameAsString();
                    if(!methodCalls.isEmpty()){
                        Map<String, List<String>> methodToMethodCallsMap = classToMethodToMethodCallsMap.get(className);
                        methodToMethodCallsMap.computeIfAbsent(methodName, k -> new ArrayList<>()).addAll(methodCalls);
                    }
                }
            }
        }, null);
    }

    public Map<String, Map<String, List<String>>> getClassToMethodToMethodCallsMap() {
        return classToMethodToMethodCallsMap;
    }
}