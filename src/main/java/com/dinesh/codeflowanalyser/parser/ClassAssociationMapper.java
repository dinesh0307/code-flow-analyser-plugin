package com.dinesh.codeflowanalyser.parser;

// START GENAI
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.metamodel.ClassOrInterfaceDeclarationMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;

import java.util.*;

public class ClassAssociationMapper {

    private Map<String, List<String>> classToAssociatedClassesMap = new HashMap<>();

    public void populateClassToAssociatedClassesMap(CompilationUnit cu) {
        if (cu != null) {
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
                    super.visit(cid, arg);

                    String className = cid.getNameAsString();

                    Set<String> associatedClasses = new HashSet<>();

                    // Collect from constructor parameters
                    cid.getConstructors().forEach(constructor ->
                            constructor.getParameters().forEach(param ->
                                    associatedClasses.add(param.getType().asString())
                            )
                    );

                    // Collect from method parameters
                    cid.getMethods().forEach(method ->
                            method.getParameters().forEach(param ->
                                    associatedClasses.add(param.getType().asString())
                            )
                    );

                    // Collect from field declarations
                    cid.getFields().forEach(field ->
                            field.getVariables().forEach(variable ->
                                    associatedClasses.add(field.getElementType().asString())
                            )
                    );

                    // Add the associated classes to the map
                    classToAssociatedClassesMap.put(className, new ArrayList<>(associatedClasses));
                }
            }, null);
        }
    }

    public Map<String, List<String>> getClassToAssociatedClassesMap() {
        return classToAssociatedClassesMap;
    }
}
