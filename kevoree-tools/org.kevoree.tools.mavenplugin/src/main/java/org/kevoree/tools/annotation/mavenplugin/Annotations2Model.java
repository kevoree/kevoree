package org.kevoree.tools.annotation.mavenplugin;

import javassist.ClassPool;
import javassist.CtClass;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 24/11/2013
 * Time: 14:40
 */
public class Annotations2Model {

    private static final KevoreeFactory factory = new DefaultKevoreeFactory();

    private void recursiveBuild(File current, ClassPool pool, String root, DeployUnit du, ContainerRoot modelRoot,Map<String, Set<String>> collectedClasses) throws Exception {
        if (current.isDirectory()) {
            File[] childs = current.listFiles();
            for (int i = 0; i < childs.length; i++) {
                File child = childs[i];
                if (child.getName().endsWith(".class")) {
                    FileInputStream fis = new FileInputStream(child);
                    CtClass clazz = pool.makeClass(fis);//
                    fis.close();
                    Object[] annotations = clazz.getAvailableAnnotations();
                    for (Object annotation : annotations) {
                        ModelBuilderHelper.process(annotation, clazz, factory, du, modelRoot);
                    }


                    //add to collected
                    Set<String> sources = collectedClasses.get(child.getName());
                    if(sources==null){
                        sources = new HashSet<String>();
                        collectedClasses.put(child.getName(),sources);
                    }
                    sources.add(du.getName()+":"+ du.getVersion());

                } else {
                    if (child.isDirectory()) {
                        String nextPath = root;
                        if (!root.isEmpty()) {
                            nextPath = nextPath + "." + child.getName();
                        } else {
                            nextPath = child.getName();
                        }
                        recursiveBuild(child, pool, nextPath, du, modelRoot,collectedClasses);
                    }
                }
            }
        }
    }

    public void fillModel(File targetDir, ContainerRoot model, DeployUnit deployUnit, List<String> additionalClassPathElems, Map<String, Set<String>> collectedClasses) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        for (String classPath : additionalClassPathElems) {
            pool.appendClassPath(classPath);
        }
        recursiveBuild(targetDir, pool, "", deployUnit, model,collectedClasses);
    }

}
