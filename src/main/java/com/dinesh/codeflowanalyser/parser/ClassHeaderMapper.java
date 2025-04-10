package com.dinesh.codeflowanalyser.parser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

public class ClassHeaderMapper {

    private Map<String, String> classToHeaderMap = new HashMap<>();

    public void populateClassHeaderMap(CompilationUnit cu) {
        if (cu != null) {
            StringBuilder classHeader = new StringBuilder();

            cu.getImports().forEach(importDeclaration -> {
                classHeader.append(importDeclaration.toString()).append(System.lineSeparator());
            });

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration classDeclaration, Void arg) {
                    super.visit(classDeclaration, arg);
                    // Append annotations
                    classDeclaration.getAnnotations().forEach(annotation -> {
                        classHeader.append(annotation.toString()).append(System.lineSeparator());
                    });

                    // Append modifiers (public, abstract, etc.)
                    classDeclaration.getModifiers().forEach(modifier -> {
                        classHeader.append(modifier.toString()).append(" ");
                    });

                    // Append class or interface keyword
                    if (classDeclaration.isInterface()) {
                        classHeader.append("interface ");
                    } else {
                        classHeader.append("class ");
                    }

                    // Append class name
                    classHeader.append(classDeclaration.getNameAsString());

                    // Append extends clause if present
                    classDeclaration.getExtendedTypes().forEach(extendedType -> {
                        classHeader.append(" extends ").append(extendedType);
                    });

                    // Append implements clause if present
                    classDeclaration.getImplementedTypes().forEach(implementedType -> {
                        classHeader.append(" implements ").append(implementedType);
                    });

                    // Append opening brace
                    classHeader.append(" {");
                    classToHeaderMap.put(classDeclaration.getNameAsString(), classHeader.toString());

                    // Append closing brace
                    //classHeader.append(System.lineSeparator()).append("}");

                    // Only visit the first class or interface declaration
                    // Remove this line if you want to visit all class or interface declarations
                    return;
                }
            }, null);
        }
    }

    public Map<String, String> getClassToHeaderMap() {
        return classToHeaderMap;
    }
}

