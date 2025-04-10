package com.dinesh.codeflowanalyser.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMethodMapper {

    private Map<String, Map<String, List<String>>> classToMethodToCodeBlockMap = new HashMap<>();

    public void populateClassToMethodMap(CompilationUnit cu) {
        if (cu != null) {
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
                    super.visit(cid, arg);

                    String className = cid.getNameAsString();
                    classToMethodToCodeBlockMap.putIfAbsent(className, new HashMap<>());
                    Map<String, List<String>> methodToCodeBlockMap = classToMethodToCodeBlockMap.get(className);

                    for (MethodDeclaration method : cid.getMethods()) {
                        String methodName = method.getNameAsString();
                        if (method.getTokenRange().isPresent()) {
                            methodToCodeBlockMap.putIfAbsent(methodName, new ArrayList<>());
                            methodToCodeBlockMap.get(methodName).add(method.getTokenRange().get().toString());
                        }
                    }

                    //classToMethodToCodeBlockMap.put(className, methodToCodeBlockMap);
                }
            }, null);
        }
    }

    public Map<String, Map<String, List<String>>> getClassToMethodToCodeBlockMap() {
        return classToMethodToCodeBlockMap;
    }
}
