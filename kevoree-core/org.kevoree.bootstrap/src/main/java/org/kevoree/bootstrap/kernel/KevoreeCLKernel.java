package org.kevoree.bootstrap.kernel;

import jet.runtime.typeinfo.JetValueParameter;
import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.Context;
import org.kevoree.bootstrap.reflect.KevoreeInjector;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.log.Log;
import org.kevoree.resolver.MavenResolver;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 19:41
 */
public class KevoreeCLKernel implements KevoreeCLFactory, BootstrapService {

    private KevoreeJarClassLoader system = new KevoreeJarClassLoader();


    public KevoreeCLKernel() {

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

    private HashMap<String, KevoreeJarClassLoader> cache = new HashMap<String, KevoreeJarClassLoader>();

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
        if (deployUnit.getName().equals("org.kevoree.api") || deployUnit.getName().equals("org.kevoree.annotation.api")) {
            return system;
        }
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
        KevoreeJarClassLoader oldKCL = get(deployUnit);
        cache.remove(deployUnit.path());
        if (oldKCL != null) {
            for (KevoreeJarClassLoader kcl : cache.values()) {
                kcl.removeChild(oldKCL);
            }
        }
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
        KevoreeJarClassLoader classLoader = get(instance.getTypeDefinition().getDeployUnit());
        Class clazz = classLoader.loadClass(instance.getTypeDefinition().getBean());
        try {
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
            if(value==null){
                if(!att.getDefaultValue().equals("")){
                    value = att.getDefaultValue();
                }
            }

            try {
                Field f = target.getClass().getDeclaredField(att.getName());
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
