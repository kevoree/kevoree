package org.kevoree.bootstrap.kernel;

import jet.runtime.typeinfo.JetValueParameter;
import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.Context;
import org.kevoree.bootstrap.reflect.KevoreeInjector;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 19:41
 */
public class KevoreeCLKernel implements KevoreeCLFactory, BootstrapService {

    private ConcurrentHashMap<String, KevoreeJarClassLoader> cache = new ConcurrentHashMap<String, KevoreeJarClassLoader>();

    private MavenResolver resolver = new MavenResolver();

    private Boolean offline = false;

    public void setKevoreeCLFactory(KevoreeCLFactory kevoreeCLFactory) {
        this.kevoreeCLFactory = kevoreeCLFactory;
    }

    public void setResolver(MavenResolver resolver) {
        this.resolver = resolver;
    }

    private KevoreeCLFactory kevoreeCLFactory = this;

    public void setInjector(KevoreeInjector injector) {
        this.injector = injector;
    }

    private KevoreeInjector injector = null;

    @Override
    public KevoreeJarClassLoader get(DeployUnit deployUnit) {
        return cache.get(deployUnit.path());
    }

    public KevoreeJarClassLoader installDeployUnit(DeployUnit deployUnit) {
        String path = deployUnit.path();
        if (cache.containsKey(path)) {
            return cache.get(path);
        } else {
            List<String> urls = new ArrayList<String>();
            if (!offline) {
                ContainerRoot root = (ContainerRoot) deployUnit.eContainer();
                for (Repository repo : root.getRepositories()) {
                    urls.add(repo.getUrl());
                }
                if (deployUnit.getVersion().contains("SNAPSHOT") || deployUnit.getVersion().contains("LATEST")) {
                    urls.add("http://oss.sonatype.org/content/groups/public/");
                } else {
                    urls.add("http://repo1.maven.org/maven2");
                }
                Log.info("Resolving ............. " + deployUnit.path());
                long before = System.currentTimeMillis();
                File resolved = resolver.resolve(deployUnit.getGroupName(), deployUnit.getName(), deployUnit.getVersion(), deployUnit.getType(), urls);
                Log.info("Resolved in {}ms", (System.currentTimeMillis() - before));
                if (resolved != null) {
                    KevoreeJarClassLoader kcl = createClassLoader(deployUnit, resolved);
                    cache.put(path, kcl);
                    return kcl;
                }
            }

        }
        return null;
    }

    @Override
    public void removeDeployUnit(DeployUnit deployUnit) {
        cache.remove(deployUnit.path());
    }

    @Override
    public void manualAttach(DeployUnit deployUnit, KevoreeJarClassLoader kevoreeJarClassLoader) {
        cache.put(deployUnit.path(), kevoreeJarClassLoader);
    }

    public KevoreeJarClassLoader recursiveInstallDeployUnit(DeployUnit deployUnit) {
        String path = deployUnit.path();
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        KevoreeJarClassLoader kcl = installDeployUnit(deployUnit);
        if (kcl == null) {
            Log.error("Can install {}", deployUnit.path());
        } else {
            for (DeployUnit child : deployUnit.getRequiredLibs()) {
                kcl.addSubClassLoader(recursiveInstallDeployUnit(child));
            }
        }

        return kcl;
    }

    @Override
    public void setOffline(boolean b) {
        offline = b;
    }

    private String nodeName;

    public void setNodeName(String nName) {
        nodeName = nName;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Object createInstance(final Instance instance) {
        KevoreeJarClassLoader classLoader = recursiveInstallDeployUnit(instance.getTypeDefinition().getDeployUnit());
        Class clazz = classLoader.loadClass(instance.getTypeDefinition().getBean());
        try {
            Object newInstance = clazz.newInstance();
            injector.addService(Context.class, new Context() {
                @Override
                public String getPath() {
                    return instance.path();
                }

                @Override
                public String getNodeName() {
                    return nodeName;
                }

                @Override
                public String getInstanceName() {
                    return instance.getName();
                }
            });

            injector.process(newInstance);
            injectDictionary(instance, newInstance);
            return newInstance;
        } catch (Exception e) {
            Log.error("Error while creating instance ", e);
        }
        return null;
    }

    public void injectDictionary(Instance instance, Object target) {
        if (instance.getDictionary() == null) {
            return;
        }
        for (DictionaryValue dicVal : instance.getDictionary().getValues()) {

            try {
                Field f = target.getClass().getDeclaredField(dicVal.getAttribute().getName());
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                if (f.getType().equals(boolean.class)) {
                    f.setBoolean(target, Boolean.parseBoolean(dicVal.getValue()));
                }
                if (f.getType().equals(Boolean.class)) {
                    f.set(target, new Boolean(Boolean.parseBoolean(dicVal.getValue())));
                }
                if (f.getType().equals(Integer.class)) {
                    f.setInt(target, Integer.parseInt(dicVal.getValue()));
                }
                if (f.getType().equals(Integer.class)) {
                    f.set(target, new Integer(Integer.parseInt(dicVal.getValue())));
                }
                if (f.getType().equals(long.class)) {
                    f.setLong(target, Long.parseLong(dicVal.getValue()));
                }
                if (f.getType().equals(Long.class)) {
                    f.set(target, new Long(Long.parseLong(dicVal.getValue())));
                }
                if (f.getType().equals(double.class)) {
                    f.setDouble(target, Double.parseDouble(dicVal.getValue()));
                }
                if (f.getType().equals(Double.class)) {
                    f.set(target, Double.parseDouble(dicVal.getValue()));
                }
                if (f.getType().equals(String.class)) {
                    f.set(target, dicVal.getValue());
                }
            } catch (Exception e) {
                Log.error("No field corresponding to annotation, consistency error {} on {}", dicVal.getAttribute().getName(), target.toString());
                e.printStackTrace();


            }
        }
    }

    @Override
    public void injectService(@JetValueParameter(name = "api") Class<? extends Object> aClass, @JetValueParameter(name = "impl") Object o, @JetValueParameter(name = "target") Object o2) {
        KevoreeInjector injector = new KevoreeInjector();
        injector.addService(aClass, o);
        injector.process(o2);
    }

    @Override
    public KevoreeJarClassLoader createClassLoader(DeployUnit du, File file) {
        KevoreeJarClassLoader classLoader = new KevoreeJarClassLoader();
        try {
            classLoader.add(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            Log.error("Error while opening JAR {} : ", file.getAbsolutePath());
        } finally {
            return classLoader;
        }
    }
}
