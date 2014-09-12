package org.kevoree.bootstrap.dev.annotator;

import javassist.*;
import jet.runtime.typeinfo.JetValueParameter;
import org.jetbrains.annotations.NotNull;
import org.kevoree.*;
import org.kevoree.Package;
import org.kevoree.annotation.Input;
import org.kevoree.annotation.Output;
import org.kevoree.annotation.Param;
import org.kevoree.api.*;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.util.ModelVisitor;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by duke on 23/01/2014.
 * <p/>
 * ModelBuilder in Pure Java, Need to be tested before going to production
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
                        providedPortRef.setOptional(annotationInput.optional());
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
                    if (method.getReturnType().getName().equals(Float.class.getName())) {
                        checkType = true;
                        dataType = DataType.FLOAT;
                    }
                    if (method.getReturnType().getName().equals(Integer.class.getName())) {
                        checkType = true;
                        dataType = DataType.INT;
                    }
                    if (method.getReturnType().getName().equals(Double.class.getName())) {
                        checkType = true;
                        dataType = DataType.DOUBLE;
                    }
                    if (method.getReturnType().getName().equals(Boolean.class.getName())) {
                        checkType = true;
                        dataType = DataType.BOOLEAN;
                    }
                    if (method.getReturnType().getName().equals(Long.class.getName())) {
                        checkType = true;
                        dataType = DataType.LONG;
                    }
                    if (method.getReturnType().getName().equals(Byte.class.getName())) {
                        checkType = true;
                        dataType = DataType.BYTE;
                    }
                    if (method.getReturnType().getName().equals(char.class.getName())) {
                        checkType = true;
                        dataType = DataType.CHAR;
                    }
                    if (method.getReturnType().getName().equals(Short.class.getName())) {
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
                        currentTypeDefinition.setDictionaryType(factory.createDictionaryType());
                    }
                    dicAtt.setName(cleanedName);
                    dicAtt.setDatatype(dataType);
                    dicAtt.setOptional(annotationParam.optional());
                    dicAtt.setFragmentDependant(annotationParam.fragmentDependent());
                    dicAtt.setDefaultValue(annotationParam.defaultValue());
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
            for (Object annotation : field.getAnnotations()) {
                if (annotation instanceof org.kevoree.annotation.KevoreeInject) {
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
                    if (currentTypeDefinition instanceof ComponentType) {
                        ComponentType currentTypeDefinitionComponentType = (ComponentType) currentTypeDefinition;
                        PortTypeRef requiredPortRef = factory.createPortTypeRef();
                        requiredPortRef.setName(field.getName());
                        requiredPortRef.setOptional(annotationOutput.optional());
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
                    if (field.getType().getName().equals(Float.class.getName())) {
                        checkType = true;
                        dataType = DataType.FLOAT;
                    }
                    if (field.getType().getName().equals(Integer.class.getName())) {
                        checkType = true;
                        dataType = DataType.INT;
                    }
                    if (field.getType().getName().equals(Double.class.getName())) {
                        checkType = true;
                        dataType = DataType.DOUBLE;
                    }
                    if (field.getType().getName().equals(Boolean.class.getName())) {
                        checkType = true;
                        dataType = DataType.BOOLEAN;
                    }
                    if (field.getType().getName().equals(Long.class.getName())) {
                        checkType = true;
                        dataType = DataType.LONG;
                    }
                    if (field.getType().getName().equals(Short.class.getName())) {
                        checkType = true;
                        dataType = DataType.SHORT;
                    }
                    if (field.getType().getName().equals(char.class.getName())) {
                        checkType = true;
                        dataType = DataType.CHAR;
                    }
                    if (field.getType().getName().equals(byte.class.getName())) {
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
                        currentTypeDefinition.setDictionaryType(factory.createDictionaryType());
                    }
                    dicAtt.setName(field.getName());
                    dicAtt.setDatatype(dataType);
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

    private static void checkParent(TypeDefinition current, CtClass clazz, CtClass originClazz, ContainerRoot root, KevoreeFactory factory) throws Exception {
        if (clazz == null) {
            return;
        }
        String name = clazz.getName();
        String version = null;
        String currentTypeName = null;
        try {
            for (Object an : clazz.getAnnotations()) {
                String newMeta = metaClassName(an);
                if (newMeta != null) {
                    if (currentTypeName != null) {
                        throw new Exception("A Java Class can't be mapped to several Kevoree TypeDefinition " + clazz.getName());
                    } else {
                        currentTypeName = newMeta;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (currentTypeName != null) {


            String endPath = clazz.getName().replace(".", File.separator) + ".class";
            String baseURL = clazz.getURL().toString().replace(endPath, "");

            /*
            String manifest = baseURL + "META-INF" + File.separator + "MANIFEST.MF";
            URL u2 = new URL(manifest);
            System.out.println(u2.openStream().available());
            */
            /*
            if (version == null) {
                File metaInf = new File(baseURL + "META-INF" + File.separator + "maven");
                System.out.println(metaInf.exists());
                if (metaInf.exists()) {
                    if (metaInf.listFiles().length > 0) {
                        File groupFile = metaInf.listFiles()[0];
                    }
                }
            } */

            if (version == null) {
                try {
                    String kevManifest = baseURL + "KEV-INF" + File.separator + "lib.json";
                    URL ukevManifest = new URL(kevManifest);
                    InputStream is = ukevManifest.openStream();
                    ContainerRoot libModel = (ContainerRoot) factory.createJSONLoader().loadModelFromStream(is).get(0);
                    factory.root(libModel);
                    final HashMap<DeployUnit, Integer> links = new HashMap<DeployUnit, Integer>();

                    libModel.deepVisitContained(new ModelVisitor() {
                        @Override
                        public void visit(@JetValueParameter(name = "elem") @NotNull KMFContainer kmfContainer, @JetValueParameter(name = "refNameInParent") @NotNull String s, @JetValueParameter(name = "parent") @NotNull KMFContainer kmfContainer2) {
                            if (kmfContainer instanceof DeployUnit) {
                                DeployUnit du = (DeployUnit) kmfContainer;
                                if (!links.containsKey(du)) {
                                    links.put(du, 0);
                                }
                                for (DeployUnit dul : du.getRequiredLibs()) {
                                    if (!links.containsKey(dul)) {
                                        links.put(dul, 0);
                                    }
                                    links.put(dul, links.get(dul) + 1);
                                }
                            }
                        }
                    });
                    //This current deploy unit should be the only one with no external references, tricky part
                    for (DeployUnit d : links.keySet()) {
                        if (links.get(d) == 0) {
                            version = d.getVersion();
                        }
                    }
                    is.close();
                } catch (Exception e) {
                    //
                }

            }
            if (version == null) {
                version = current.getVersion();
            }
            TypeDefinition parent = getOrCreateTypeDefinition(name, version, root, factory, currentTypeName);
            current.addSuperTypes(parent);
        }
    }

    private static void processTypeDefinition(TypeDefinition td, DeployUnit du, CtClass clazz, ContainerRoot root, KevoreeFactory factory) throws Exception {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            td.setAbstract(true);
        } else {
            td.setAbstract(false);
        }
        Value javaClazz = factory.createValue();
        javaClazz.setName("java.class");
        javaClazz.setValue(clazz.getName());
        td.addMetaData(javaClazz);
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

    public static TypeDefinition getOrCreateTypeDefinition(String name, String version, ContainerRoot root, KevoreeFactory factory, String typeName) {
        String[] packages = name.split("\\.");
        if(packages.length <= 1) {
            throw new RuntimeException("Component '"+name+"' must be defined in a Java package");
        }
        org.kevoree.Package pack = null;
        for (int i = 0; i < packages.length - 1; i++) {
            if (pack == null) {
                pack = root.findPackagesByID(packages[i]);
                if (pack == null) {
                    pack = (org.kevoree.Package) factory.createPackage().withName(packages[i]);
                    root.addPackages(pack);
                }
            } else {
                Package packNew = pack.findPackagesByID(packages[i]);
                if (packNew == null) {
                    packNew = (org.kevoree.Package) factory.createPackage().withName(packages[i]);
                    pack.addPackages(packNew);
                }
                pack = packNew;
            }
        }

        String tdName = packages[packages.length - 1];

        TypeDefinition foundTD = pack.findTypeDefinitionsByNameVersion(tdName, version);
        if (foundTD != null) {
            return foundTD;
        } else {
            TypeDefinition td = (TypeDefinition) factory.create(typeName);
            td.setVersion(version);
            td.setName(tdName);
            pack.addTypeDefinitions(td);
            return td;
        }
    }


    public static void process(Object elem, CtClass clazz, KevoreeFactory factory, DeployUnit du, ContainerRoot root) throws Exception {
        if (elem instanceof org.kevoree.annotation.GroupType) {
            TypeDefinition td = getOrCreateTypeDefinition(clazz.getName(), du.getVersion(), root, factory, metaClassName(elem));
            processTypeDefinition(td, du, clazz, root, factory);
            deepFields(clazz, factory, td);
            deepMethods(clazz, factory, td);
        }
        if (elem instanceof org.kevoree.annotation.ChannelType) {
            TypeDefinition td = getOrCreateTypeDefinition(clazz.getName(), du.getVersion(), root, factory, metaClassName(elem));
            processTypeDefinition(td, du, clazz, root, factory);
            deepFields(clazz, factory, td);
            deepMethods(clazz, factory, td);
        }
        if (elem instanceof org.kevoree.annotation.ComponentType) {
            TypeDefinition td = getOrCreateTypeDefinition(clazz.getName(), du.getVersion(), root, factory, metaClassName(elem));
            processTypeDefinition(td, du, clazz, root, factory);
            deepFields(clazz, factory, td);
            deepMethods(clazz, factory, td);
        }
        if (elem instanceof org.kevoree.annotation.NodeType) {
            TypeDefinition td = getOrCreateTypeDefinition(clazz.getName(), du.getVersion(), root, factory, metaClassName(elem));
            processTypeDefinition(td, du, clazz, root, factory);
            deepFields(clazz, factory, td);
            deepMethods(clazz, factory, td);
        }
    }

    public static String metaClassName(Object elem) {
        if (elem instanceof org.kevoree.annotation.GroupType) {
            return GroupType.class.getName();
        }
        if (elem instanceof org.kevoree.annotation.ChannelType) {
            return ChannelType.class.getName();
        }
        if (elem instanceof org.kevoree.annotation.ComponentType) {
            return ComponentType.class.getName();
        }
        if (elem instanceof org.kevoree.annotation.NodeType) {
            return org.kevoree.NodeType.class.getName();
        }
        return null;
    }

}
