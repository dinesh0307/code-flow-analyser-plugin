package com.dinesh.codeflowanalyser.parser;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.dinesh.codeflowanalyser.analyser.ImpactedMethodAnalyzer;
import com.dinesh.codeflowanalyser.genai.LLMManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeParser {

    private FileReader fileReader;
    private ClassMethodMapper classMethodMapper;
    private ClassAssociationMapper classAssociationMapper;
    private ImplementsExtendsMapper implementsExtendsMapper;
    //private ConfigPropertiesMapper configPropertiesMapper;
    private ClassVariableMapper classVariableMapper;
    private ClassStaticImportMapper classStaticImportMapper;
    private ClassToMethodCallsMapper classToMethodCallsMapper;
    private ClassHeaderMapper classHeaderMapper;

    private Map<String, String> classNameToAbsolutePathMap = new HashMap<>();

    public CodeParser() {
        this.fileReader = new FileReader();
        this.classMethodMapper = new ClassMethodMapper();
        this.classAssociationMapper = new ClassAssociationMapper();
        this.implementsExtendsMapper = new ImplementsExtendsMapper();
        //this.configPropertiesMapper = new ConfigPropertiesMapper();
        this.classVariableMapper = new ClassVariableMapper();
        this.classStaticImportMapper = new ClassStaticImportMapper();
        this.classToMethodCallsMapper = new ClassToMethodCallsMapper();
        this.classHeaderMapper = new ClassHeaderMapper();
    }

    public void parse(String rootDirectory) throws IOException {
        List<Path> javaFiles = fileReader.readJavaFiles(rootDirectory);
        //List<Path> configFiles = fileReader.readConfigFiles(rootDirectory);

        JavaParser parser = new JavaParser();

        for (Path javaFile : javaFiles) {
            try {
                classNameToAbsolutePathMap.put(javaFile.getFileName().toString().split("\\.")[0], javaFile.toString());
                CompilationUnit cu = parser.parse(javaFile).getResult().orElse(null);
                classMethodMapper.populateClassToMethodMap(cu);
                classAssociationMapper.populateClassToAssociatedClassesMap(cu);
                implementsExtendsMapper.populateInterfaceToImplementMap(cu);
                classVariableMapper.populateClassToVariableMap(cu);
                classStaticImportMapper.populateStaticImportMap(cu);
                classToMethodCallsMapper.populateClassToMethodCallsMap(cu);
                classHeaderMapper.populateClassHeaderMap(cu);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, String> getClassNameToAbsolutePathMap() {
        return classNameToAbsolutePathMap;
    }

    public ImpactedMethodAnalyzer getImpactedMethodAnalyzer() {
        return new ImpactedMethodAnalyzer(classAssociationMapper, classHeaderMapper, classMethodMapper, classStaticImportMapper, classToMethodCallsMapper, classVariableMapper, implementsExtendsMapper);
    }

    public void displayResults() {
        System.out.println("Class to Method to Code Block Map: " + classMethodMapper.getClassToMethodToCodeBlockMap());
        System.out.println("Class to Associated Classes Map: " + classAssociationMapper.getClassToAssociatedClassesMap());
        System.out.println("Interface to Implementation Class Map: " + implementsExtendsMapper.getInterfaceToImplementationClassMap());
        System.out.println("Class to Variables Map: " + classVariableMapper.getClassToVariableMap());
        System.out.println("Class to Static Import Map: " + classStaticImportMapper.getClassToStaticImportsMap());
        //System.out.println("Configs and Properties Map: " + configPropertiesMapper.getConfigsAndPropertiesMap());
    }

    public ClassMethodMapper getClassMethodMapper(){
        return classMethodMapper;
    }
}
