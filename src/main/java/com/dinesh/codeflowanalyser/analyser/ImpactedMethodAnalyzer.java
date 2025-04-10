package com.dinesh.codeflowanalyser.analyser;

// START GENAI

// START GENAI2

import com.dinesh.codeflowanalyser.parser.*;
import java.util.*;

public class ImpactedMethodAnalyzer {


    private final ClassAssociationMapper classAssociationMapper;
    private final ClassHeaderMapper classHeaderMapper;
    private final ClassMethodMapper classMethodMapper;
    private final ClassStaticImportMapper classStaticImportMapper;
    private final ClassToMethodCallsMapper classToMethodCallsMapper;
    private final ClassVariableMapper classVariableMapper;
    private final ImplementsExtendsMapper implementsExtendsMapper;


    private Map<String, List<String>> classToClassHeaderAndMethodsMap = new LinkedHashMap<>();

    public ImpactedMethodAnalyzer(ClassAssociationMapper classAssociationMapper, ClassHeaderMapper classHeaderMapper,
                                  ClassMethodMapper classMethodMapper, ClassStaticImportMapper classStaticImportMapper,
                                  ClassToMethodCallsMapper classToMethodCallsMapper, ClassVariableMapper classVariableMapper,
                                  ImplementsExtendsMapper implementsExtendsMapper) {

        this.classAssociationMapper = classAssociationMapper;
        this.classHeaderMapper = classHeaderMapper;
        this.classMethodMapper = classMethodMapper;
        this.classStaticImportMapper = classStaticImportMapper;
        this.classToMethodCallsMapper = classToMethodCallsMapper;
        this.classVariableMapper = classVariableMapper;
        this.implementsExtendsMapper = implementsExtendsMapper;
    }

    public void prepareImpactedMethods(String className, String methodName) {
        classToClassHeaderAndMethodsMap.clear();
        List<String> impactedMethods = new ArrayList<>();
        Set<String> visitedMethods = new HashSet<>();
        getImpactedMethodsRecursive(className, methodName, impactedMethods, visitedMethods);
    }

    public List<String> getImpactedCodeBlocksForGPTRequest(String mainPrompt, String subPrompt){
        List<String> contents = new LinkedList<>();
        contents.add(mainPrompt);
        for (Map.Entry<String, List<String>> entrySet : classToClassHeaderAndMethodsMap.entrySet()) {
            StringBuilder builder = new StringBuilder();
            String key = entrySet.getKey();
            builder.append(subPrompt + key + ".java \n");
            String header = classHeaderMapper.getClassToHeaderMap().get(key);
            builder.append(header + "\n");
            for (String methodBlock : entrySet.getValue()) {
                builder.append(methodBlock + "\n");
            }
            builder.append("}" + "\n");
            String content = builder.toString();
            content = content.replace("\"", "\\\"").replace("\n", "\\n");
            contents.add(content);
        }
        return contents;
    }

    public List<String> getImpactedFilesList(Map<String, String> classNameToAbsPathMap){
        LinkedList<String> impactedFiles = new LinkedList<>();
        for (String className : classToClassHeaderAndMethodsMap.keySet()) {
            impactedFiles.add(classNameToAbsPathMap.get(className));
        }
        return impactedFiles;
    }


    private void getImpactedMethodsRecursive(String className, String methodName, List<String> impactedMethods, Set<String> visitedMethods) {
        if(visitedMethods.contains(className + "." + methodName)){
            return;
        }
        visitedMethods.add(className + "." + methodName);

        if(!classMethodMapper.getClassToMethodToCodeBlockMap().containsKey(className) || !classMethodMapper.getClassToMethodToCodeBlockMap().get(className).containsKey(methodName)){
            return;
        }

        impactedMethods.add("Class: " + className + ", Method: " + methodName);
        for (String methodCodeBlock : classMethodMapper.getClassToMethodToCodeBlockMap().get(className).get(methodName)) {
            classToClassHeaderAndMethodsMap.putIfAbsent(className, new ArrayList<>());
            classToClassHeaderAndMethodsMap.get(className).add(methodCodeBlock);
        }
        Map<String, Map<String, List<String>>> classToMethodToMethodCallsMap = classToMethodCallsMapper.getClassToMethodToMethodCallsMap();
        if(classToMethodToMethodCallsMap != null && !classToMethodToMethodCallsMap.isEmpty()){
            Map<String, List<String>> methodToMethodCallsMap = classToMethodToMethodCallsMap.get(className);
            if(methodToMethodCallsMap != null && methodToMethodCallsMap.containsKey(methodName)){
                for (String methodCall : methodToMethodCallsMap.get(methodName)) {
                    methodCall = methodCall.substring(0, methodCall.indexOf("("));
                    String[] methodCallParts = methodCall.split("\\.");
                    if(methodCallParts.length == 2){
                        List<String> classNameFromObjectRef = getClassNameFromObjectReference(methodCallParts[0], className, methodName, methodCallParts[1]);
                        for (String refClass : classNameFromObjectRef) {
                            getImpactedMethodsRecursive(refClass, methodCallParts[1], impactedMethods, visitedMethods);
                        }
                    } else if (methodCallParts.length == 1) {
                        String refClass = getClassNameFromCalleeMethodName(methodCallParts[0], className);
                        if(refClass != null){
                            getImpactedMethodsRecursive(getClassNameFromCalleeMethodName(methodCallParts[0], className), methodCallParts[0], impactedMethods, visitedMethods);
                        }
                    }
                }
            }
        }
    }


    private String getClassNameFromCalleeMethodName(String calleeMethod, String className) {
        if (classMethodMapper.getClassToMethodToCodeBlockMap().containsKey(className)){
            if(classMethodMapper.getClassToMethodToCodeBlockMap().get(className).containsKey(calleeMethod)) {
                return className;
            }
            String parentClass = className;
            while (implementsExtendsMapper.getClassToExtendsMap().containsKey(parentClass)){
                List<String> extendedClasses = implementsExtendsMapper.getClassToExtendsMap().get(parentClass);
                if(extendedClasses != null && !extendedClasses.isEmpty()){
                    parentClass = extendedClasses.get(0);
                    if(classMethodMapper.getClassToMethodToCodeBlockMap().get(parentClass) != null && classMethodMapper.getClassToMethodToCodeBlockMap().get(parentClass).containsKey(calleeMethod)){
                        return parentClass;
                    }
                }
            }
        }

        return null;
    }
    private List<String> getClassNameFromObjectReference(String objectRef, String className, String methodName, String calleeMethod) {
        if("this".equals(objectRef)){
            String refClassName = getClassNameFromCalleeMethodName(calleeMethod, className);
            return refClassName == null ? Collections.emptyList() : Collections.singletonList(refClassName);
        }
        if(classVariableMapper.getClassToVariableMap().containsKey(objectRef)){
            String refClassName = getClassNameFromCalleeMethodName(calleeMethod, objectRef);
            return refClassName == null ? Collections.emptyList() : Collections.singletonList(refClassName);
        }
        if(classVariableMapper.getClassToMethodToVariableMap().containsKey(className) && classVariableMapper.getClassToMethodToVariableMap().get(className).containsKey(methodName)){
            Map<String, String> variableToClassNameMap = classVariableMapper.getClassToMethodToVariableMap().get(className).get(methodName);
            if(variableToClassNameMap.containsKey(objectRef)){
                String refClassName = variableToClassNameMap.get(objectRef);
                if(implementsExtendsMapper.getInterfaceToImplementationClassMap().containsKey(refClassName)){
                    List<String> refClasses = new ArrayList<>();
                    List<String> implementClasses = implementsExtendsMapper.getInterfaceToImplementationClassMap().get(refClassName);
                    for (String implementClass : implementClasses) {
                        String classNameFromCalleeMethodName = getClassNameFromCalleeMethodName(calleeMethod, implementClass);
                        if(classNameFromCalleeMethodName != null){
                            refClasses.add(classNameFromCalleeMethodName);
                        }
                    }
                    return refClasses;
                }
                refClassName = getClassNameFromCalleeMethodName(calleeMethod, refClassName);
                return refClassName == null ? Collections.emptyList() : Collections.singletonList(refClassName);
            }
        }
        if(classVariableMapper.getClassToVariableMap().containsKey(className)){
            Map<String, String> variableToClassNameMap = classVariableMapper.getClassToVariableMap().get(className);
            if(variableToClassNameMap.containsKey(objectRef)){
                String refClassName = variableToClassNameMap.get(objectRef);
                if(implementsExtendsMapper.getInterfaceToImplementationClassMap().containsKey(refClassName)){
                    List<String> refClasses = new ArrayList<>();
                    List<String> implementClasses = implementsExtendsMapper.getInterfaceToImplementationClassMap().get(refClassName);
                    for (String implementClass : implementClasses) {
                        String classNameFromCalleeMethodName = getClassNameFromCalleeMethodName(calleeMethod, implementClass);
                        if(classNameFromCalleeMethodName != null){
                            refClasses.add(classNameFromCalleeMethodName);
                        }
                    }
                    return refClasses;
                }
                refClassName = getClassNameFromCalleeMethodName(calleeMethod, refClassName);
                return refClassName == null ? Collections.emptyList() : Collections.singletonList(refClassName);
            }
        }
        return Collections.emptyList();
    }


}

