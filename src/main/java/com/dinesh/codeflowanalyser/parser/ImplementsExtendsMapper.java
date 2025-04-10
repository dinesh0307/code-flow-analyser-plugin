package com.dinesh.codeflowanalyser.parser;

// START GENA1

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImplementsExtendsMapper {

    private Map<String, List<String>> interfaceToImplementationClassMap = new HashMap<>();
    private Map<String, List<String>> classToExtendsMap = new HashMap<>();

    public void populateInterfaceToImplementMap(CompilationUnit cu) {
        if (cu != null) {
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
                    super.visit(cid, arg);

                    String className = cid.getNameAsString();
                    List<String> implementedInterfaces = cid.getImplementedTypes().stream()
                            .map(type -> type.getNameAsString())
                            .collect(Collectors.toList());

                    for (String interfaceName : implementedInterfaces) {
                        interfaceToImplementationClassMap
                                .computeIfAbsent(interfaceName, k -> new ArrayList<>())
                                .add(className);
                    }

                    cid.getExtendedTypes().forEach(extendedType -> {
                        String extendedTypeName = extendedType.getNameAsString();
                        classToExtendsMap
                                .computeIfAbsent(className, k -> new ArrayList<>())
                                .add(extendedTypeName);
                    });
                    /*// Process methods to find interface dependencies
       for (MethodDeclaration method : cid.getMethods()) {
           for (Parameter param : method.getParameters()) {
               String paramType = param.getTypeAsString();
               if (interfaceToImplementationClassMap.containsKey(paramType)) {
                   interfaceToImplementationClassMap
                       .computeIfAbsent(paramType, k -> new ArrayList<>())
                       .add(className);
               }
           }
       }*/
                }
            }, null);
        }


    }

    public Map<String, List<String>> getInterfaceToImplementationClassMap() {
        return interfaceToImplementationClassMap;
    }

    public Map<String, List<String>> getClassToExtendsMap() {
        return classToExtendsMap;
    }
}