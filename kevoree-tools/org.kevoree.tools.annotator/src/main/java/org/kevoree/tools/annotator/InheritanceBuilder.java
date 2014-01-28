package org.kevoree.tools.annotator;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.annotation.ChannelType;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.NodeType;

import java.io.File;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/01/14
 * Time: 17:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class InheritanceBuilder {

    public void fillModel(File targetDir, ContainerRoot model, DeployUnit deployUnit, List<String> additionalClassPathElems) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(Annotations2Model.class));
        for (String classPath : additionalClassPathElems) {
            pool.appendClassPath(classPath);
        }
        processInheritance(targetDir, pool, "", deployUnit, model);
    }

    public void processInheritance(File current, ClassPool pool, String root, DeployUnit du, ContainerRoot modelRoot) throws NotFoundException, ClassNotFoundException {
        if (current.isDirectory()) {
            File[] childs = current.listFiles();
            for (int i = 0; i < childs.length; i++) {
                File child = childs[i];
                if (child.getName().endsWith(".class")) {
                    String className = root + "." + child.getName().replace(".class", "");
                    CtClass clazz = pool.get(className);
                    // FIXME the "/" in the following line may cause some trouble if KMF change its separator for id from multi keys
                    TypeDefinition currentTypeDefinition = modelRoot.findTypeDefinitionsByID(clazz.getSimpleName() + "/" + du.getVersion());
                    if (currentTypeDefinition != null) {
                        deepInheritance(clazz, currentTypeDefinition, modelRoot);
                    }
                } else {
                    if (child.isDirectory()) {
                        String nextPath = root;
                        if (!root.isEmpty()) {
                            nextPath = nextPath + "." + child.getName();
                        } else {
                            nextPath = child.getName();
                        }
                        processInheritance(child, pool, nextPath, du, modelRoot);
                    }
                }
            }
        }
    }

    public void deepInheritance(CtClass clazz, TypeDefinition currentTypeDefinition, ContainerRoot model) throws NotFoundException, ClassNotFoundException {
        if (clazz.getInterfaces() != null) {
            for (CtClass interf : clazz.getInterfaces()) {
                if (currentTypeDefinition instanceof org.kevoree.GroupType) {
                    doDeepInheritance(currentTypeDefinition, interf, model, GroupType.class);
                } else if (currentTypeDefinition instanceof org.kevoree.ChannelType) {
                    doDeepInheritance(currentTypeDefinition, interf, model, ChannelType.class);
                } else if (currentTypeDefinition instanceof org.kevoree.ComponentType) {
                    doDeepInheritance(currentTypeDefinition, interf, model, ComponentType.class);
                } else if (currentTypeDefinition instanceof org.kevoree.NodeType) {
                    doDeepInheritance(currentTypeDefinition, interf, model, NodeType.class);
                }
            }
        }
        if (clazz.getSuperclass() != null) {
            if (currentTypeDefinition instanceof org.kevoree.GroupType) {
                doDeepInheritance(currentTypeDefinition, clazz.getSuperclass(), model, GroupType.class);
            } else if (currentTypeDefinition instanceof org.kevoree.ChannelType) {
                doDeepInheritance(currentTypeDefinition, clazz.getSuperclass(), model, ChannelType.class);
            } else if (currentTypeDefinition instanceof org.kevoree.ComponentType) {
                doDeepInheritance(currentTypeDefinition, clazz.getSuperclass(), model, ComponentType.class);
            } else if (currentTypeDefinition instanceof org.kevoree.NodeType) {
                doDeepInheritance(currentTypeDefinition, clazz.getSuperclass(), model, NodeType.class);
            }
        }
    }

    public void doDeepInheritance(TypeDefinition currentTypeDefinition, CtClass superClazz, ContainerRoot model, Class annotationClass) throws NotFoundException, ClassNotFoundException {
        Object annotation = superClazz.getAnnotation(annotationClass);
        if (annotation != null) {
            for (TypeDefinition superType : model.getTypeDefinitions()) {
                if (superClazz.getSimpleName().equals(superType.getName())) {
                    currentTypeDefinition.addSuperTypes(superType);
                }
            }
        }
    }
}
