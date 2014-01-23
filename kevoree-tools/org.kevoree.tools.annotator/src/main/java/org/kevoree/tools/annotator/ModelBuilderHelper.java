package org.kevoree.tools.annotator;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.kevoree.*;
import org.kevoree.ChannelType;
import org.kevoree.ComponentType;
import org.kevoree.GroupType;
import org.kevoree.NodeType;
import org.kevoree.annotation.*;
import org.kevoree.api.*;

/**
 * Created by duke on 23/01/2014.
 *
 * ModelBuilder in Pure Java, Need to be tested before going to production
 *
 */
public class ModelBuilderHelper {

    public static void addLibrary(String libName, TypeDefinition typeDef, ContainerRoot root, KevoreeFactory factory) {
        TypeLibrary lib = root.findLibrariesByID(libName);
        if (lib == null) {
            lib = factory.createTypeLibrary();
            lib.setName(libName);
            root.addLibraries(lib);
        }
        lib.addSubTypes(typeDef);
    }

    public static void deepMethods(CtClass clazz, KevoreeFactory factory, TypeDefinition currentTypeDefinition) throws ClassNotFoundException, NotFoundException {
        for (CtMethod method : clazz.getDeclaredMethods()) {
            for (Object annotation : method.getAnnotations()) {
                if (annotation instanceof Input) {
                    Input annotationInput = (Input) annotation;
                    if (currentTypeDefinition instanceof org.kevoree.ComponentType) {
                        org.kevoree.ComponentType currentTypeDefinitionCT = (ComponentType) currentTypeDefinition;
                        PortTypeRef providedPortRef = factory.createPortTypeRef();
                        providedPortRef.setName(method.getName());
                        providedPortRef.setOptional(annotationInput.optional());
                        currentTypeDefinitionCT.addProvided(providedPortRef);
                    }
                }
            }
        }
        for (CtClass interfaceLoop : clazz.getInterfaces()) {
            deepMethods(interfaceLoop, factory, currentTypeDefinition);
        }
        if (clazz.getSuperclass() != null) {
            deepMethods(clazz.getSuperclass(), factory, currentTypeDefinition);
        }
    }

    public static void deepFields(CtClass clazz, KevoreeFactory factory, TypeDefinition currentTypeDefinition) throws Exception {
        for (CtField field : clazz.getDeclaredFields()) {
            for (Object annotation : field.getAnnotations()) {
                if (annotation instanceof KevoreeInject) {
                    boolean checkType = false;
                    if (field.getType().getName().equals(ModelService.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(BootstrapService.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(KevScriptService.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(Context.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(ChannelContext.class.getName())) {
                        checkType = true;
                    }
                    if (!checkType) {
                        throw new Exception("KevoreeInject annotation is only suitable for following types : ModelService,BootstrapService,KevScriptService,Context,ChannelContext : currently found : " + field.getType().getName());
                    }
                }
                if (annotation instanceof Output) {
                    Output annotationOutput = (Output) annotation;
                    if (!field.getType().getName().equals(org.kevoree.api.Port.class.getName())) {
                        throw new Exception("Output port field must of type of " + org.kevoree.api.Port.class.getName());
                    }
                    if (currentTypeDefinition instanceof org.kevoree.ComponentType) {
                        org.kevoree.ComponentType currentTypeDefinitionComponentType = (ComponentType) currentTypeDefinition;
                        PortTypeRef requiredPortRef = factory.createPortTypeRef();
                        requiredPortRef.setName(field.getName());
                        requiredPortRef.setOptional(annotationOutput.optional());
                        currentTypeDefinitionComponentType.addRequired(requiredPortRef);
                    }
                }
                if (annotation instanceof Param) {
                    Param annotationParam = (Param) annotation;
                    boolean checkType = false;
                    if (field.getType().getName().equals(String.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(Float.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(Integer.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(Double.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(Boolean.class.getName())) {
                        checkType = true;
                    }
                    if (field.getType().getName().equals(Long.class.getName())) {
                        checkType = true;
                    }
                    if (!checkType) {
                        if (!field.getType().isPrimitive()) {
                            throw new Exception("Param annotation is only applicable on field of type String,Long,Double,Float,Integer, current " + field.getType().getName());
                        }
                    }
                    DictionaryAttribute dicAtt = factory.createDictionaryAttribute();
                    if (currentTypeDefinition.getDictionaryType() == null) {
                        currentTypeDefinition.setDictionaryType(factory.createDictionaryType());
                    }
                    dicAtt.setName(field.getName());
                    dicAtt.setDatatype(field.getType().getName());
                    dicAtt.setOptional(annotationParam.optional());
                    dicAtt.setFragmentDependant(annotationParam.fragmentDependent());
                    dicAtt.setDefaultValue(annotationParam.defaultValue());
                    currentTypeDefinition.getDictionaryType().addAttributes(dicAtt);

                }
            }
        }
        for (CtClass interfaceLoop : clazz.getInterfaces()) {
            deepFields(interfaceLoop, factory, currentTypeDefinition);
        }
        if (clazz.getSuperclass() != null) {
            deepFields(clazz.getSuperclass(), factory, currentTypeDefinition);
        }
    }


    public static void process(Object elem, CtClass clazz, KevoreeFactory factory, DeployUnit du, ContainerRoot root) throws Exception {

        if (elem instanceof GroupType) {
            org.kevoree.GroupType groupType = factory.createGroupType();
            groupType.setVersion(du.getVersion());
            groupType.setName(clazz.getSimpleName());
            groupType.setBean(clazz.getName());
            root.addTypeDefinitions(groupType);
            groupType.setDeployUnit(du);
            deepFields(clazz, factory, groupType);
        }
        if (elem instanceof ChannelType) {
            ChannelType channelType = factory.createChannelType();
            channelType.setVersion(du.getVersion());
            channelType.setName(clazz.getSimpleName());
            channelType.setBean(clazz.getName());
            root.addTypeDefinitions(channelType);
            channelType.setDeployUnit(du);
            deepFields(clazz, factory, channelType);
        }
        if (elem instanceof ComponentType) {
            ComponentType componentType = factory.createComponentType();
            componentType.setVersion(du.getVersion());
            componentType.setName(clazz.getSimpleName());
            componentType.setBean(clazz.getName());
            root.addTypeDefinitions(componentType);
            componentType.setDeployUnit(du);
            deepFields(clazz, factory, componentType);
            deepMethods(clazz, factory, componentType);
        }
        if (elem instanceof NodeType) {
            NodeType nodeType = factory.createNodeType();
            nodeType.setVersion(du.getVersion());
            nodeType.setName(clazz.getSimpleName());
            nodeType.setBean(clazz.getName());
            root.addTypeDefinitions(nodeType);
            nodeType.setDeployUnit(du);
            deepFields(clazz, factory, nodeType);
        }
        if (elem instanceof Library) {
            libraryCache = (Library) elem;
        }

    }

    static Library libraryCache = null;

    public static void postProcess(CtClass clazz, KevoreeFactory factory, DeployUnit du, ContainerRoot root) {
        if (libraryCache != null) {
            for (TypeDefinition typeDef : root.getTypeDefinitions()) {
                if (typeDef.getName().equals(clazz.getSimpleName()) && typeDef.getVersion().equals(du.getVersion())) {
                    if (libraryCache.name() != null) {
                        addLibrary(libraryCache.name(), typeDef, root, factory);
                    }
                    String[] libs = libraryCache.names();
                    if (libs != null) {
                        for (int i = 0; i < libs.length; i++) {
                            addLibrary(libs[i], typeDef, root, factory);
                        }
                    }
                }
            }
        }
        libraryCache = null;
    }


}
