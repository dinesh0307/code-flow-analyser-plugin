package com.dinesh.codeflowanalyser.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassStaticImportMapper {
    Map<String, Set<String>> classToStaticImportsMap = new HashMap<>();

    public void populateStaticImportMap(CompilationUnit cu) {
        if (cu != null) {
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(com.github.javaparser.ast.ImportDeclaration importDeclaration, Void arg) {
                    super.visit(importDeclaration, arg);
                    if (importDeclaration.isStatic()) {
                        String className = cu.getPrimaryTypeName().orElse(null);
                        if (className != null) {
                            classToStaticImportsMap.putIfAbsent(className, new HashSet<>());
                            classToStaticImportsMap.get(className).add(importDeclaration.getNameAsString());
                        }
                    }
                }
            }, null);
        }
    }

    public Map<String, Set<String>> getClassToStaticImportsMap() {
        return classToStaticImportsMap;
    }
}
