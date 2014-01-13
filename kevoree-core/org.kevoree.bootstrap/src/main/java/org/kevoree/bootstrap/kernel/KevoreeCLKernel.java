package org.kevoree.bootstrap.kernel;

import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.Context;
import org.kevoree.bootstrap.reflect.KevoreeInjector;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.kcl.api.FlexyClassLoaderFactory;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 19:41
 */
public class KevoreeCLKernel implements KevoreeCLFactory, BootstrapService {

    private FlexyClassLoader system = FlexyClassLoaderFactory.INSTANCE.create();

    public KevoreeCLKernel() {
        system.setKey("KevoreeBootstrapCL");

        try {
            DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
            ContainerRoot root = factory.createContainerRoot();
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("bootinfo")));
            String line = reader.readLine();
            while (line != null) {
                String[] elem = line.split(":");
                DeployUnit du = factory.createDeployUnit();
                du.setGroupName(elem[0]);
                du.setName(elem[1]);
                du.setVersion(elem[2]);
                root.addDeployUnits(du);
                line = reader.readLine();
                cache.put(du.path(), system);
            }
        } catch (IOException e) {
            Log.error("Error while read boot info");
        }
    }

    private ConcurrentHashMap<String, FlexyClassLoader> cache = new ConcurrentHashMap<String, FlexyClassLoader>();

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
    public FlexyClassLoader get(DeployUnit deployUnit) {
        if (deployUnit.getName().equals("org.kevoree.api") || deployUnit.getName().equals("org.kevoree.annotation.api") || deployUnit.getName().equals("org.kevoree.model") || deployUnit.getName().equals("org.kevoree.modeling.microframework")) {
            return system;
        }
        return cache.get(deployUnit.path());
    }

    public FlexyClassLoader installDeployUnit(DeployUnit deployUnit) {
        String path = deployUnit.path();
        FlexyClassLoader resolvedKCL = get(deployUnit);
        if (resolvedKCL != null) {
            return resolvedKCL;
        } else {
            List<String> urls = new ArrayList<String>();
            if (!offline) {
                ContainerRoot root = (ContainerRoot) deployUnit.eContainer();
                File resolved;
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
                if (deployUnit.getUrl() == null || "".equals(deployUnit.getUrl())) {
                    resolved = resolver.resolve(deployUnit.getGroupName(), deployUnit.getName(), deployUnit.getVersion(), deployUnit.getType(), urls);
                } else {
                    resolved = resolver.resolve(deployUnit.getUrl(), urls);
                    if (resolved == null && new File(deployUnit.getUrl()).exists()) {
                        resolved = new File(deployUnit.getUrl());
                    }
                }
                Log.info("Resolved in {}ms", (System.currentTimeMillis() - before));
                if (resolved != null) {
                    FlexyClassLoader kcl = createClassLoader(deployUnit, resolved);
                    cache.put(path, kcl);
                    return kcl;
                } else {
                    Log.error("Unable to resolve {}", deployUnit.path());
                }
            }

        }
        return null;
    }

    @Override
    public void removeDeployUnit(DeployUnit deployUnit) {
        FlexyClassLoader oldKCL = get(deployUnit);
        cache.remove(deployUnit.path());
        if (oldKCL != null) {
            for (FlexyClassLoader kcl : cache.values()) {
                kcl.detachChild(oldKCL);
            }
        }
    }

    @Override
    public void manualAttach(DeployUnit deployUnit, FlexyClassLoader kevoreeJarClassLoader) {
        cache.put(deployUnit.path(), kevoreeJarClassLoader);
    }

    public FlexyClassLoader recursiveInstallDeployUnit(DeployUnit deployUnit) {
        String path = deployUnit.path();
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        FlexyClassLoader kcl = installDeployUnit(deployUnit);
        if (kcl == null) {
            Log.error("Can't install {}", deployUnit.path());
        } else {
            for (DeployUnit child : deployUnit.getRequiredLibs()) {
                kcl.attachChild(recursiveInstallDeployUnit(child));
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
        try {
            FlexyClassLoader classLoader = get(instance.getTypeDefinition().getDeployUnit());
            Class clazz = classLoader.loadClass(instance.getTypeDefinition().getBean());
            Object newInstance = clazz.newInstance();
            KevoreeInjector selfInjector = injector.clone();
            selfInjector.addService(Context.class, new InstanceContext(instance.path(), nodeName, instance.getName()));
            selfInjector.process(newInstance);
            injectDictionary(instance, newInstance);
            return newInstance;
        } catch (Exception e) {
            Log.error("Error while creating instance ", e);
        }
        return null;
    }

    public void injectDictionary(Instance instance, Object target) {
        if (instance.getTypeDefinition() == null || instance.getTypeDefinition().getDictionaryType() == null) {
            return;
        }
        for (DictionaryAttribute att : instance.getTypeDefinition().getDictionaryType().getAttributes()) {
            String value = null;
            if (att.getFragmentDependant()) {
                FragmentDictionary fdico = instance.findFragmentDictionaryByID(nodeName);
                if (fdico != null) {
                    DictionaryValue tempValue = fdico.findValuesByID(att.getName());
                    if (tempValue != null) {
                        value = tempValue.getValue();
                    }
                }
            }
            if (value == null) {
                if (instance.getDictionary() != null) {
                    DictionaryValue tempValue = instance.getDictionary().findValuesByID(att.getName());
                    if (tempValue != null) {
                        value = tempValue.getValue();
                    }
                }
            }
            if (value == null) {
                if (!att.getDefaultValue().equals("")) {
                    value = att.getDefaultValue();
                }
            }
            if (value != null) {
                try {
                    Field f = lookup(att.getName(), target.getClass());
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    if (f.getType().equals(boolean.class)) {
                        f.setBoolean(target, Boolean.parseBoolean(value));
                    }
                    if (f.getType().equals(Boolean.class)) {
                        f.set(target, new Boolean(Boolean.parseBoolean(value)));
                    }
                    if (f.getType().equals(int.class)) {
                        f.setInt(target, Integer.parseInt(value));
                    }
                    if (f.getType().equals(Integer.class)) {
                        f.set(target, new Integer(Integer.parseInt(value)));
                    }
                    if (f.getType().equals(long.class)) {
                        f.setLong(target, Long.parseLong(value));
                    }
                    if (f.getType().equals(Long.class)) {
                        f.set(target, new Long(Long.parseLong(value)));
                    }
                    if (f.getType().equals(double.class)) {
                        f.setDouble(target, Double.parseDouble(value));
                    }
                    if (f.getType().equals(Double.class)) {
                        f.set(target, Double.parseDouble(value));
                    }
                    if (f.getType().equals(String.class)) {
                        f.set(target, value);
                    }
                } catch (Exception e) {
                    Log.error("No field corresponding to annotation, consistency error {} on {}", att.getName(), target.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    public Field lookup(String name, Class clazz) {
        Field f = null;
        for (Field loopf : clazz.getDeclaredFields()) {
            if (name.equals(loopf.getName())) {
                f = loopf;
            }
        }
        if (f != null) {
            return f;
        } else {
            for (Class loopClazz : clazz.getInterfaces()) {
                f = lookup(name, loopClazz);
                if (f != null) {
                    return f;
                }
            }
            if (clazz.getSuperclass() != null) {
                f = lookup(name, clazz.getSuperclass());
                if (f != null) {
                    return f;
                }
            }
        }
        return f;
    }


    @Override
    public void injectService(Class<? extends Object> aClass, Object o, Object o2) {
        KevoreeInjector injector = new KevoreeInjector();
        injector.addService(aClass, o);
        injector.process(o2);
    }

    @Override
    public FlexyClassLoader createClassLoader(DeployUnit du, File file) {
        FlexyClassLoader classLoader = FlexyClassLoaderFactory.INSTANCE.create();
        classLoader.setKey(du.path());
        try {
            classLoader.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            Log.error("Error while opening JAR {} : ", file.getAbsolutePath());
        } finally {
            return classLoader;
        }
    }
}
