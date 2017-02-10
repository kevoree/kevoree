package org.kevoree.bootstrap.kernel;

import org.jboss.shrinkwrap.resolver.api.maven.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kevoree.*;
import org.kevoree.api.BootstrapService;
import org.kevoree.api.Context;
import org.kevoree.api.ModelService;
import org.kevoree.api.PlatformService;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.bootstrap.reflect.Injector;
import org.kevoree.core.ContextAwareAdapter;
import org.kevoree.core.KevoreeCoreBean;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.KMFContainer;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 19:41
 */
public class KevoreeCLKernel implements BootstrapService {

    private Bootstrap bs;
    private Injector injector;
    private File propFile = null;

    public KevoreeCLKernel(Bootstrap b, Injector injector) {
        this.bs = b;
        this.injector = injector;

        // be sure that the logging of the Maven resolver won't be too verbose
        // but let the user modify the behavior if needed
        String loggingProp = System.getProperty("java.util.logging.config.file");
        if (loggingProp == null) {
            propFile = Paths.get(System.getProperty("user.home"), ".kevoree", "java-logging.properties").toFile();
            if (!propFile.exists()) {
                propFile.getParentFile().mkdirs();
                try {
                    PrintWriter writer = new PrintWriter(propFile);
                    writer.println("# Specify the handlers to create in the root logger");
                    writer.println("handlers= java.util.logging.ConsoleHandler");
                    writer.println("# Set the default logging level for new ConsoleHandler instances");
                    writer.println("java.util.logging.ConsoleHandler.level= SEVERE");
                    writer.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Unable to write " + propFile.getAbsolutePath(), e);
                }
            }
        } else {
            propFile = new File(loggingProp);
        }
    }

    private Boolean offline = false;

    @Override
    public FlexyClassLoader get(DeployUnit deployUnit) {
        return this.get("mvn:"+deployUnit.getUrl());
    }

    @Override
    public FlexyClassLoader get(String key) {
        return this.bs.getKernel().get(key);
    }

    private String getKey(MavenArtifactInfo artifact) {
        return "mvn:" + artifact.getCoordinate().getGroupId() + ":" + artifact.getCoordinate().getArtifactId() +
                ":" + artifact.getCoordinate().getVersion();
    }

    private void installDependencies(ConfigurableMavenResolverSystem resolver, FlexyClassLoader parentCl, MavenArtifactInfo artifact, int depth) {
        String indent = "";
        int count = 0;
        while (count < depth) {
            indent += "  ";
            count++;
        }

        for (MavenArtifactInfo dep : artifact.getDependencies()) {
            String key = getKey(dep);
            if (!dep.getScope().equals(ScopeType.TEST)) {
                long before = System.currentTimeMillis();
                FlexyClassLoader depCl = get(key);
                if (depCl == null) {
                    if (dep.getCoordinate().getType().equals(PackagingType.POM)) {
                        Log.debug("{} + {} ({}ms)", indent, key, (System.currentTimeMillis() - before));
                        installDependencies(resolver, parentCl, dep, depth+1);
                    } else {
                        File depJar;
                        try {
                            depJar = resolver
                                    .resolve(dep.getCoordinate().toCanonicalForm())
                                    .withoutTransitivity()
                                    .asSingleFile();
                        } catch (Exception e) {
                            Log.error(indent + " ! " + key);
                            throw e;
                        }

                        if (depJar != null && depJar.exists()) {
                            if (dep.getScope().equals(ScopeType.RUNTIME)) {
                                try {
                                    parentCl.load(depJar);
                                } catch (IOException e) {
                                    Log.error("Unable to load jar {} in class loader {}", depJar.getAbsolutePath(), parentCl.getKey());
                                }
                            } else {
                                depCl = bs.getKernel().put(key, depJar);
                                parentCl.attachChild(depCl);
                            }
                            Log.debug("{} + {} ({}ms)", indent, key, (System.currentTimeMillis() - before));
                            installDependencies(resolver, depCl, dep, depth+1);
                        } else {
                            Log.error("{} Unable to resolve {}", indent, key);
                        }
                    }
                } else {
                    if (!dep.getScope().equals(ScopeType.RUNTIME)) {
                        parentCl.attachChild(depCl);
                        Log.debug("{} = {} already loaded", indent, key, (System.currentTimeMillis() - before));
                    }
                }
            }
        }
    }

    @Override
    public FlexyClassLoader installDeployUnit(DeployUnit deployUnit) {
        FlexyClassLoader fcl = get(deployUnit);
        if (fcl != null) {
            return fcl;
        } else {
            ConfigurableMavenResolverSystem resolver = getResolver(deployUnit);

            long before = System.currentTimeMillis();
            Log.info("Resolving ............. {}", deployUnit.getUrl());
            MavenResolvedArtifact artifact = resolver
                    .resolve(deployUnit.getUrl())
                    .withoutTransitivity()
                    .asSingleResolvedArtifact();

            File duJar = artifact.asFile();
            if (duJar != null && duJar.exists()) {
                String key = getKey(artifact);
                fcl = bs.getKernel().put(key, duJar);
                installDependencies(resolver, fcl, artifact, 0);
                Log.info("Resolved in {}ms", (System.currentTimeMillis() - before));
            } else {
                Log.error("Unable to resolve {}", deployUnit.getUrl());
            }
        }
        return fcl;
    }

    private ConfigurableMavenResolverSystem getResolver(DeployUnit deployUnit) {
        String offlineProp = System.getProperty("offline", "false");
        boolean offline = Boolean.valueOf(offlineProp);
        ConfigurableMavenResolverSystem resolver = Maven
                .configureResolver()
                .useLegacyLocalRepo(true)
                .workOffline(offline);

        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream(propFile));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read " + propFile.getAbsolutePath());
        }

        // hacky way to treat Maven repositories as I don't want to change the Kevoree MM
        for (Value val : deployUnit.getFilters()) {
            if (val.getName().startsWith("repo_")) {
                try {
                    resolver.withRemoteRepo(val.getName().substring(5), new URL(val.getValue()), "default");
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid repository URL: " + val.getValue());
                }
            }
        }

        return resolver;
    }

    @Override
    public void removeDeployUnit(DeployUnit deployUnit) {
        bs.getKernel().drop(deployUnit.getUrl());
    }

    @Nullable
    @Override
    public FlexyClassLoader installTypeDefinition(Instance instance) {
        TypeDefinition td = instance.getTypeDefinition();
        DeployUnit du = validateFilters(instance, td.select("deployUnits[]/filters[name=platform,value=java]"));
        return installDeployUnit(du);
    }

    @Override
    public void setOffline(boolean b) {
        offline = b;
    }

    private String nodeName;

    public void setNodeName(String nName) {
        nodeName = nName;
    }

    public KevoreeCoreBean core;

    public void setCore(KevoreeCoreBean core) {
        this.core = core;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public synchronized Object createInstance(final Instance instance, final FlexyClassLoader classLoader) {
        try {
            final String mainClassName = searchMainClassName(instance);
            final Class clazz = classLoader.loadClass(mainClassName);
            final Object newInstance = clazz.newInstance();

            injector.register(Context.class, new InstanceContext(instance.path(), nodeName, instance.getName()));
            injector.register(ModelService.class, new ContextAwareAdapter(core, instance.path()));
            injector.register(PlatformService.class, core);
            injector.inject(newInstance);

            return newInstance;
        } catch (final Exception e) {
            Log.error("Error while creating instance \"{}\" of type {}", e, instance.getName(),
                    instance.getTypeDefinition().getName());
        }
        return null;
    }

    private String searchMainClassName(final Instance instance) {
        TypeDefinition td = instance.getTypeDefinition();
        DeployUnit du = validateFilters(instance, td.select("deployUnits[]/filters[name=platform,value=java]"));
        String tag = "class:" + td.getName() + ":" + td.getVersion();
        Value tdefClassName = du.findFiltersByID(tag);
        if (tdefClassName != null) {
            return tdefClassName.getValue();
        } else {
            throw new RuntimeException("Cannot find meta-data \"" + tag + "\" in DeployUnit " + du.getHashcode() + "/" + du.getName() + "/" + du.getVersion());
        }
    }

    private DeployUnit validateFilters(Instance instance, List<KMFContainer> filters) {
        if (filters.size() > 1) {
            String filtersStr = "";
            for (int i=0; i < filters.size(); i++) {
                filtersStr += filters.get(i).eContainer().path();
                if (i < filters.size() - 1) {
                    filtersStr += ", ";
                }
            }
            throw new RuntimeException("Instance " + instance.path() + " has " + filters.size() + " deployUnits ("+filtersStr+") that matches platform \"java\" (must only be one)");
        }

        return (DeployUnit) filters.get(0).eContainer();
    }

    @NotNull
    @Override
    public void injectDictionary(Instance instance, Object target, boolean defaultOnly) {
        if (instance.getTypeDefinition() == null || instance.getTypeDefinition().getDictionaryType() == null) {
            return;
        }
        for (DictionaryAttribute att : instance.getTypeDefinition().getDictionaryType().getAttributes()) {
            String defValue = null;
            String value = null;
            if (att.getFragmentDependant()) {
                FragmentDictionary fdico = instance.findFragmentDictionaryByID(nodeName);
                if (fdico != null) {
                    Value tempValue = fdico.findValuesByID(att.getName());
                    if (tempValue != null) {
                        value = tempValue.getValue();
                    }
                }
            }
            if (value == null) {
                if (instance.getDictionary() != null) {
                    Value tempValue = instance.getDictionary().findValuesByID(att.getName());
                    if (tempValue != null) {
                        value = tempValue.getValue();
                    }
                }
            }
            if (att.getDefaultValue() != null && !att.getDefaultValue().equals("")) {
                defValue = att.getDefaultValue();
            }
            if (defaultOnly) {
                if (defValue != null && value == null) {
                    internalInjectField(att.getName(), defValue, target);
                }
            } else {
                if (value == null && defValue != null) {
                    value = defValue;
                }
                if (value != null) {
                    internalInjectField(att.getName(), value, target);
                }
            }
        }
    }

    @NotNull
    @Override
    public void injectDictionaryValue(Value dictionaryValue, Object target) {
        internalInjectField(dictionaryValue.getName(), dictionaryValue.getValue(), target);
    }

    private boolean internalInjectField(String fieldName, String value, Object target) {
        if (target != null && value != null) {
            try {
                boolean isSet = false;
                String setterName = "set";
                setterName = setterName + fieldName.substring(0, 1).toUpperCase();
                if (fieldName.length() > 1) {
                    setterName = setterName + fieldName.substring(1);
                }
                Method setter = lookupSetter(setterName, target.getClass());
                if (setter != null && setter.getParameterTypes().length == 1) {
                    if (!setter.isAccessible()) {
                        setter.setAccessible(true);
                    }
                    Class pClazz = setter.getParameterTypes()[0];
                    if (pClazz.equals(boolean.class)) {
                        setter.invoke(target, Boolean.parseBoolean(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Boolean.class)) {
                        setter.invoke(target, Boolean.parseBoolean(value));
                        isSet = true;
                    }
                    if (pClazz.equals(int.class)) {
                        setter.invoke(target, Integer.parseInt(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Integer.class)) {
                        setter.invoke(target, Integer.parseInt(value));
                        isSet = true;
                    }
                    if (pClazz.equals(long.class)) {
                        setter.invoke(target, Long.parseLong(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Long.class)) {
                        setter.invoke(target, Long.parseLong(value));
                        isSet = true;
                    }
                    if (pClazz.equals(double.class)) {
                        setter.invoke(target, Double.parseDouble(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Double.class)) {
                        setter.invoke(target, Double.parseDouble(value));
                        isSet = true;
                    }
                    if (pClazz.equals(String.class)) {
                        setter.invoke(target, value);
                        isSet = true;
                    }
                    if (pClazz.equals(short.class)) {
                        setter.invoke(target, Short.parseShort(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Short.class)) {
                        setter.invoke(target, Short.parseShort(value));
                        isSet = true;
                    }
                    if (pClazz.equals(float.class)) {
                        setter.invoke(target, Float.parseFloat(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Float.class)) {
                        setter.invoke(target, Float.parseFloat(value));
                        isSet = true;
                    }
                    if (pClazz.equals(byte.class)) {
                        setter.invoke(target, Byte.parseByte(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Byte.class)) {
                        setter.invoke(target, Byte.parseByte(value));
                        isSet = true;
                    }
                    if (value.length() == 1) {
                        if (pClazz.equals(char.class)) {
                            setter.invoke(target, value.charAt(0));
                            isSet = true;
                        }
                    }
                }

                if (!isSet) {
                    Field f = lookup(fieldName, target.getClass());
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    if (f.getType().equals(boolean.class)) {
                        f.setBoolean(target, Boolean.parseBoolean(value));
                    }
                    if (f.getType().equals(Boolean.class)) {
                        f.set(target, Boolean.parseBoolean(value));
                    }
                    if (f.getType().equals(int.class)) {
                        f.setInt(target, Integer.parseInt(value));
                    }
                    if (f.getType().equals(Integer.class)) {
                        f.set(target, Integer.parseInt(value));
                    }
                    if (f.getType().equals(long.class)) {
                        f.setLong(target, Long.parseLong(value));
                    }
                    if (f.getType().equals(Long.class)) {
                        f.set(target, Long.parseLong(value));
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
                    if (f.getType().equals(short.class)) {
                        f.set(target, Short.parseShort(value));
                    }
                    if (f.getType().equals(Short.class)) {
                        f.set(target, Short.parseShort(value));
                    }
                    if (f.getType().equals(float.class)) {
                        f.set(target, Float.parseFloat(value));
                    }
                    if (f.getType().equals(Float.class)) {
                        f.set(target, Float.parseFloat(value));
                    }
                    if (f.getType().equals(byte.class)) {
                        f.set(target, Byte.parseByte(value));
                    }
                    if (f.getType().equals(Byte.class)) {
                        f.set(target, Byte.parseByte(value));
                    }
                    if (value.length() == 1) {
                        if (f.getType().equals(char.class)) {
                            f.set(target, value.charAt(0));
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                Log.error("Unable to inject value \"{}\" in {} parameter \"{}\"", e.getCause(), value, target.getClass().getName(), fieldName);
                return false;
            }
        } else {
            return false;
        }
    }

    public Method lookupSetter(String name, Class clazz) {
        Method f = null;
        for (Method loopMethod : clazz.getDeclaredMethods()) {
            if (name.equals(loopMethod.getName())) {
                f = loopMethod;
            }
        }
        if (f != null) {
            return f;
        } else {
            for (Class loopClazz : clazz.getInterfaces()) {
                f = lookupSetter(name, loopClazz);
                if (f != null) {
                    return f;
                }
            }
            if (clazz.getSuperclass() != null) {
                f = lookupSetter(name, clazz.getSuperclass());
                if (f != null) {
                    return f;
                }
            }
        }
        return f;
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
    public <T> void registerService(Class<T> serviceClass, T serviceImpl) {
        injector.register(serviceClass, serviceImpl);
    }

    @Override
    public <T> void unregisterService(Class<T> serviceClass) {
        injector.unregister(serviceClass);
    }

    @Nullable
    @Override
    public File resolve(String url, Set<String> repos) {
        return bs.getKernel().getResolver().resolve(url, (Set<String>) repos);
    }

}
