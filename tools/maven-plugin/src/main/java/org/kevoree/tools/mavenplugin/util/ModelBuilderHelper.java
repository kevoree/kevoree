package org.kevoree.tools.mavenplugin.util;


import javassist.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.kevoree.*;
import org.kevoree.annotation.Input;
import org.kevoree.annotation.Output;
import org.kevoree.annotation.Param;
import org.kevoree.api.*;
import org.kevoree.api.Port;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.service.KevScriptService;
import org.kevoree.service.ModelService;
import org.kevoree.service.RuntimeService;

/**
 * Created by duke on 23/01/2014.
 */

public class ModelBuilderHelper {

    public static void deepMethods(CtClass clazz, KevoreeFactory factory, TypeDefinition currentTypeDefinition) throws Exception {
        for (CtMethod method : clazz.getDeclaredMethods()) {
            for (Object annotation : method.getAnnotations()) {
                if (annotation instanceof Input) {
                    Input annotationInput = (Input) annotation;
                    if (currentTypeDefinition instanceof ComponentType) {
                        ComponentType currentTypeDefinitionCT = (ComponentType) currentTypeDefinition;
                        PortTypeRef providedPortRef = factory.createPortTypeRef();
                        providedPortRef.setName(method.getName());
                        try {
                            providedPortRef.setOptional(annotationInput.optional());
                        } catch (Exception e) {
                            providedPortRef.setOptional(true);
                        }
                        
                        providedPortRef.setNoDependency(true);
                        currentTypeDefinitionCT.addProvided(providedPortRef);
                    }
                }
                if (annotation instanceof Param && (method.getName().startsWith("set") || method.getName().startsWith("get"))) {
                    boolean checkType = false;

                    String cleanedName = method.getName().substring(3);
                    cleanedName = cleanedName.substring(0, 1).toLowerCase() + cleanedName.substring(1);
                    Param annotationParam = (Param) annotation;
                    DataType dataType = null;

                    if (method.getReturnType().getName().equals(String.class.getName())) {
                        checkType = true;
                        dataType = DataType.STRING;
                    }
                    if (method.getReturnType().getName().equals(Float.class.getName()) || method.getReturnType().getName().equals("float")) {
                        checkType = true;
                        dataType = DataType.FLOAT;
                    }
                    if (method.getReturnType().getName().equals(Integer.class.getName()) || method.getReturnType().getName().equals("int")) {
                        checkType = true;
                        dataType = DataType.INT;
                    }
                    if (method.getReturnType().getName().equals(Double.class.getName()) || method.getReturnType().getName().equals("double")) {
                        checkType = true;
                        dataType = DataType.DOUBLE;
                    }
                    if (method.getReturnType().getName().equals(Boolean.class.getName()) || method.getReturnType().getName().equals("boolean")) {
                        checkType = true;
                        dataType = DataType.BOOLEAN;
                    }
                    if (method.getReturnType().getName().equals(Long.class.getName()) || method.getReturnType().getName().equals("long")) {
                        checkType = true;
                        dataType = DataType.LONG;
                    }
                    if (method.getReturnType().getName().equals(Byte.class.getName()) || method.getReturnType().getName().equals(byte.class.getName()) || method.getReturnType().getName().equals("byte")) {
                        checkType = true;
                        dataType = DataType.BYTE;
                    }
                    if (method.getReturnType().getName().equals(char.class.getName()) || method.getReturnType().getName().equals("char")) {
                        checkType = true;
                        dataType = DataType.CHAR;
                    }
                    if (method.getReturnType().getName().equals(Short.class.getName()) || method.getReturnType().getName().equals("short")) {
                        checkType = true;
                        dataType = DataType.SHORT;
                    }
                    if (!checkType) {
                        if (!method.getReturnType().isPrimitive()) {
                            throw new Exception("Param annotation is only applicable on field of type String,Long,Double,Float,Integer, current " + method.getReturnType().getName());
                        }
                    }
                    DictionaryAttribute dicAtt = factory.createDictionaryAttribute();
                    if (currentTypeDefinition.getDictionaryType() == null) {
                        currentTypeDefinition.setDictionaryType(factory.createDictionaryType().withGenerated_KMF_ID("0.0"));
                    }
                    dicAtt.setName(cleanedName);
                    dicAtt.setDatatype(dataType);
                    try {
                        dicAtt.setOptional(annotationParam.optional());
                    } catch (Exception e) {
                        dicAtt.setOptional(true);
                    }
                    try {
                        dicAtt.setFragmentDependant(annotationParam.fragmentDependent());
                    } catch (Exception e) {
                        dicAtt.setFragmentDependant(false);
                    }
                    currentTypeDefinition.getDictionaryType().addAttributes(dicAtt);
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
            try {
                for (Object annotation : field.getAnnotations()) {
                    if (annotation instanceof org.kevoree.annotation.KevoreeInject) {
                        boolean checkType = false;
                        if (field.getType().getName().equals(ModelService.class.getName())) {
                            checkType = true;
                        } else if (field.getType().getName().equals(RuntimeService.class.getName())) {
                            checkType = true;
                        } else if (field.getType().getName().equals(KevScriptService.class.getName())) {
                            checkType = true;
                        } else if (field.getType().getName().equals(Context.class.getName())) {
                            checkType = true;
                        } else if (field.getType().getName().equals(ChannelContext.class.getName())) {
                            checkType = true;
                        }
                        if (!checkType) {
                            throw new Exception("KevoreeInject annotation is only suitable for following types : ModelService,RuntimeService,KevScriptService,Context,ChannelContext : currently found : " + field.getType().getName());
                        }
                    }
                    if (annotation instanceof Output) {
                        Output annotationOutput = (Output) annotation;
                        if (!field.getType().getName().equals(Port.class.getName())) {
                            throw new Exception("Output port field must of type of " + Port.class.getName());
                        }
                        if (currentTypeDefinition instanceof ComponentType) {
                            ComponentType currentTypeDefinitionComponentType = (ComponentType) currentTypeDefinition;
                            PortTypeRef requiredPortRef = factory.createPortTypeRef();
                            requiredPortRef.setName(field.getName());
                            try {
                                requiredPortRef.setOptional(annotationOutput.optional());
                            } catch (Exception e) {
                                requiredPortRef.setOptional(true);
                            }


                            requiredPortRef.setNoDependency(true);
                            currentTypeDefinitionComponentType.addRequired(requiredPortRef);
                        }
                    }
                    if (annotation instanceof Param) {
                        Param annotationParam = (Param) annotation;
                        DataType dataType = null;

                        boolean checkType = false;
                        if (field.getType().getName().equals(String.class.getName())) {
                            checkType = true;
                            dataType = DataType.STRING;
                        }
                        if (field.getType().getName().equals(Float.class.getName()) || field.getType().getName().equals("float")) {
                            checkType = true;
                            dataType = DataType.FLOAT;
                        }
                        if (field.getType().getName().equals(Integer.class.getName()) || field.getType().getName().equals("int")) {
                            checkType = true;
                            dataType = DataType.INT;
                        }
                        if (field.getType().getName().equals(Double.class.getName()) || field.getType().getName().equals("double")) {
                            checkType = true;
                            dataType = DataType.DOUBLE;
                        }
                        if (field.getType().getName().equals(Boolean.class.getName()) || field.getType().getName().equals("boolean")) {
                            checkType = true;
                            dataType = DataType.BOOLEAN;
                        }
                        if (field.getType().getName().equals(Long.class.getName()) || field.getType().getName().equals("long")) {
                            checkType = true;
                            dataType = DataType.LONG;
                        }
                        if (field.getType().getName().equals(Short.class.getName()) || field.getType().getName().equals("short")) {
                            checkType = true;
                            dataType = DataType.SHORT;
                        }
                        if (field.getType().getName().equals(char.class.getName()) || field.getType().getName().equals("char")) {
                            checkType = true;
                            dataType = DataType.CHAR;
                        }
                        if (field.getType().getName().equals(byte.class.getName()) || field.getType().getName().equals("byte") || field.getType().getName().equals(Byte.class.getName())) {
                            checkType = true;
                            dataType = DataType.BYTE;
                        }
                        if (!checkType) {
                            if (!field.getType().isPrimitive()) {
                                throw new Exception("Param annotation is only applicable on field of type String,Long,Double,Float,Integer, current " + field.getType().getName());
                            }
                        }
                        DictionaryAttribute dicAtt = factory.createDictionaryAttribute();
                        if (currentTypeDefinition.getDictionaryType() == null) {
                            currentTypeDefinition.setDictionaryType(factory.createDictionaryType().withGenerated_KMF_ID("0.0"));
                        }
                        dicAtt.setName(field.getName());
                        dicAtt.setDatatype(dataType);
                        try {
                            dicAtt.setOptional(annotationParam.optional());
                        } catch (Exception e) {
                            dicAtt.setOptional(true);
                        }
                        try {
                            dicAtt.setFragmentDependant(annotationParam.fragmentDependent());
                        } catch (Exception e) {
                            dicAtt.setFragmentDependant(false);
                        }
                        currentTypeDefinition.getDictionaryType().setGenerated_KMF_ID("0.0");
                        currentTypeDefinition.getDictionaryType().addAttributes(dicAtt);
                    }
                }
            } catch (ClassNotFoundException ignore) {}
        }
        
        for (CtClass interfaceLoop : clazz.getInterfaces()) {
            deepFields(interfaceLoop, factory, currentTypeDefinition);
        }
        if (clazz.getSuperclass() != null) {
            deepFields(clazz.getSuperclass(), factory, currentTypeDefinition);
        }
    }

    private static void checkParent(TypeDefinition current, CtClass clazz, CtClass originClazz, ContainerRoot root, KevoreeFactory factory) throws Exception {
//        if (clazz == null) {
//            return;
//        }
//        String name = clazz.getName();
//        String version = null;
//        String currentTypeName = null;
//        try {
//            for (Object an : clazz.getAnnotations()) {
//                String newMeta = metaClassName(an);
//                if (newMeta != null) {
//                    if (currentTypeName != null) {
//                        throw new Exception("A Java Class can't be mapped to several Kevoree TypeDefinition " + clazz.getName());
//                    } else {
//                        currentTypeName = newMeta;
//                    }
//                }
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (currentTypeName != null) {
//            final HashMap<DeployUnit, Integer> links = new HashMap<DeployUnit, Integer>();
//
//            root.deepVisitContained(new ModelVisitor() {
//                @Override
//                public void visit(@NotNull KMFContainer kmfContainer, @NotNull String s, @NotNull KMFContainer kmfContainer2) {
//                    if (kmfContainer instanceof DeployUnit) {
//                        DeployUnit du = (DeployUnit) kmfContainer;
//                        if (!links.containsKey(du)) {
//                            links.put(du, 0);
//                        }
//                        for (DeployUnit dul : du.getRequiredLibs()) {
//                            if (!links.containsKey(dul)) {
//                                links.put(dul, 0);
//                            }
//                            links.put(dul, links.get(dul) + 1);
//                        }
//                    }
//                }
//            });
//
//            //This current deploy unit should be the only one with no external references, tricky part
//            for (DeployUnit d : links.keySet()) {
//                if (links.get(d) == 0) {
//                    version = d.getVersion();
//                }
//            }
//            if (version == null) {
//                version = current.getVersion();
//            }
//            TypeDefinition parent = getOrCreateTypeDefinition(name, version, root, factory, (org.kevoree.Package) current.eContainer(), currentTypeName);
//            current.addSuperTypes(parent);
//        }
    }

    private static void processTypeDefinition(TypeDefinition td, DeployUnit du, CtClass clazz, ContainerRoot root, KevoreeFactory factory) throws Exception {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            td.setAbstract(true);
        } else {
            td.setAbstract(false);
        }
        
        final Value javaClazz = factory.createValue();
        javaClazz.setName("class:" + td.getName() + ":" + td.getVersion());
        javaClazz.setValue(clazz.getName());
        
        du.addFilters(javaClazz);
        td.addDeployUnits(du);
        
        try {
            checkParent(td, clazz.getSuperclass(), clazz, root, factory);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        try {
            for (CtClass interf : clazz.getInterfaces()) {
                checkParent(td, interf, clazz, root, factory);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static TypeDefinition getOrCreateTypeDefinition(final String name, final Long version, final ContainerRoot root, final KevoreeFactory factory, final org.kevoree.Package pkg, final String typeName) {
        final TypeDefinition foundTD = pkg.findTypeDefinitionsByNameVersion(name, String.valueOf(version));
        if (foundTD != null) {
            return foundTD;
        } else {
            final TypeDefinition td = (TypeDefinition) factory.create(typeName);
            td.setVersion(String.valueOf(version));
            td.setName(name);
            td.setDictionaryType(factory.createDictionaryType().withGenerated_KMF_ID("0.0"));
            pkg.addTypeDefinitions(td);
            return td;
        }
    }

    public static void process(Object elem, CtClass clazz, KevoreeFactory factory, DeployUnit du, ContainerRoot root) throws Exception {
    	TypeDefinition td = null;
    	String descVal = null;
        if (elem instanceof org.kevoree.annotation.GroupType) {
            td = getOrCreateTypeDefinition(
            		clazz.getSimpleName(),
            		((org.kevoree.annotation.GroupType) elem).version(),
            		root,
            		factory,
            		(org.kevoree.Package) du.eContainer(),
            		metaClassName(elem));
            descVal = ((org.kevoree.annotation.GroupType) elem).description();
        } else if (elem instanceof org.kevoree.annotation.ChannelType) {
            td = getOrCreateTypeDefinition(
            		clazz.getSimpleName(),
            		((org.kevoree.annotation.ChannelType) elem).version(),
            		root,
            		factory,
            		(org.kevoree.Package) du.eContainer(),
            		metaClassName(elem));
            descVal = ((org.kevoree.annotation.ChannelType) elem).description();
        } else if (elem instanceof org.kevoree.annotation.ComponentType) {
            td = getOrCreateTypeDefinition(
            		clazz.getSimpleName(),
            		((org.kevoree.annotation.ComponentType) elem).version(),
            		root,
            		factory,
            		(org.kevoree.Package) du.eContainer(),
            		metaClassName(elem));
            descVal = ((org.kevoree.annotation.ComponentType) elem).description();

        } else if (elem instanceof org.kevoree.annotation.NodeType) {
            td = getOrCreateTypeDefinition(
            		clazz.getSimpleName(),
            		((org.kevoree.annotation.NodeType) elem).version(),
            		root,
            		factory,
            		(org.kevoree.Package) du.eContainer(),
            		metaClassName(elem));
            descVal = ((org.kevoree.annotation.NodeType) elem).description();
        }
        if (td != null) {
        	if (descVal != null && !descVal.isEmpty()) {
                Value desc = factory.createValue();
                desc.setName("description");
                desc.setValue(descVal);
                td.addMetaData(desc);
            }
            processTypeDefinition(td, du, clazz, root, factory);
            deepFields(clazz, factory, td);
            deepMethods(clazz, factory, td);        	
        }
    }

    public static String metaClassName(Object elem) {
        if (elem instanceof org.kevoree.annotation.GroupType) {
            return org.kevoree.GroupType.class.getName();
        }
        if (elem instanceof org.kevoree.annotation.ChannelType) {
            return org.kevoree.ChannelType.class.getName();
        }
        if (elem instanceof org.kevoree.annotation.ComponentType) {
            return org.kevoree.ComponentType.class.getName();
        }
        if (elem instanceof org.kevoree.annotation.NodeType) {
            return org.kevoree.NodeType.class.getName();
        }
        return null;
    }

    public static String createKey(String namespace, String name, String version, String append) {
    	String data = namespace + "/" + name + "/" + version;
    	if (append != null && !append.isEmpty()) {
    		data += "/" + append;
    	}
    	return DigestUtils.md5Hex(data);
    }
}
