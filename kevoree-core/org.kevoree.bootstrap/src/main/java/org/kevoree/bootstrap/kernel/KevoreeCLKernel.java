package org.kevoree.bootstrap.kernel;

import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
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
import org.kevoree.kcl.api.FlexyClassLoaderFactory;
import org.kevoree.kcl.api.ResolutionPriority;
import org.kevoree.log.Log;
import org.kevoree.pmodeling.api.KMFContainer;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.LogManager;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 19:41
 */
public class KevoreeCLKernel implements BootstrapService {

    private File propFile = null;
    private Bootstrap bs;
    private Injector injector;

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
                    writer.println("# Set the default logging level for new ConsoleHandler instances");
                    writer.println("java.util.logging.ConsoleHandler.level= INFO");
                    writer.println("");
                    writer.println("# Set global verbose level");
                    writer.println(".level= INFO");
                    writer.println("");
                    writer.println("# Set log verbose level for ShrinkWrap Resolvers");
                    writer.println("org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener.level= WARNING");
                    writer.println("org.jboss.shrinkwrap.resolver.impl.maven.logging.LogRepositoryListener.level= WARNING");
                    writer.println("org.jboss.shrinkwrap.resolver.impl.maven.logging.LogModelProblemCollector.level= SEVERE");
                    writer.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            propFile = new File(loggingProp);
        }
    }

    private Boolean offline = false;

    private String buildKernelKey(Instance instance, DeployUnit deployUnit) {
    	return instance.getName() + ":" + instance.getTypeDefinition().getName() + ":" + instance.getTypeDefinition().getVersion() + ":" + deployUnit.getUrl();
    }

    @Override
    public FlexyClassLoader get(Instance instance, DeployUnit deployUnit) {
        return this.get(buildKernelKey(instance, deployUnit));
    }

    @Override
    public FlexyClassLoader get(String key) {
        return this.bs.getKernel().get(key);
    }

    @Override
	public FlexyClassLoader installDeployUnit(Instance instance, DeployUnit deployUnit) {
		FlexyClassLoader fcl = get(instance, deployUnit);
		if (fcl != null) {
			return fcl;
		} else {
            ConfigurableMavenResolverSystem resolver = Maven
                    .configureResolver()
                    .useLegacyLocalRepo(true)
                    .workOffline(offline);

            try {
                LogManager.getLogManager().readConfiguration(new FileInputStream(propFile));
            } catch (IOException e) {
                throw new RuntimeException("Unable to read logging properties from " + propFile.getAbsolutePath());
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

            Log.info("Resolving ............. " + deployUnit.getUrl());
            long before = System.currentTimeMillis();
            MavenResolvedArtifact[] artifacts = resolver.resolve(deployUnit.getUrl())
                    .withTransitivity().asResolvedArtifact();
            Log.info("Resolved in {}ms", (System.currentTimeMillis() - before));

            File duJar = null;
            Set<MavenResolvedArtifact> duDeps = new HashSet<MavenResolvedArtifact>();
            for (MavenResolvedArtifact artifact : artifacts) {
                if (artifact.getCoordinate().toCanonicalForm().equals(deployUnit.getUrl())) {
                    duJar = artifact.asFile();
                } else if (!artifact.getCoordinate().getGroupId().equals("org.kevoree")) {
                    duDeps.add(artifact);
                }
            }
            if (duJar != null) {
                Log.trace("Installing {}", deployUnit.getUrl());
                fcl = bs.getKernel().put(buildKernelKey(instance, deployUnit), duJar);
                for (MavenResolvedArtifact dep : duDeps) {
                    String key = dep.getCoordinate().toCanonicalForm();
                    Log.trace("  + {}", key);
                    fcl.attachChild(bs.getKernel().put(key, dep.asFile()));
                }
            } else {
                Log.error("Unable to resolve {}", deployUnit.getUrl());
            }
		}
		return fcl;
	}

    @Override
    public void removeDeployUnit(Instance instance, DeployUnit deployUnit) {
        bs.getKernel().drop(buildKernelKey(instance, deployUnit));
    }

    @Nullable
    @Override
    public FlexyClassLoader installTypeDefinition(Instance instance) {
        FlexyClassLoader fcl = FlexyClassLoaderFactory.INSTANCE.create();
        fcl.resolutionPriority = ResolutionPriority.CHILDS;
        fcl.setKey(instance.getTypeDefinition().path());

        TypeDefinition td = instance.getTypeDefinition();
        List<KMFContainer> filters = td.select("deployUnits[]/filters[name=platform,value=java]");
        if (filters.size() > 1) {
            String filtersStr = "";
            for (int i=0; i < filters.size(); i++) {
                filtersStr += filters.get(i).eContainer().path();
                if (i < filters.size() - 1) {
                    filtersStr += ", ";
                }
            }
            throw new RuntimeException("Instance " + instance.path() + " has " + filters.size() + " deployUnits ("+filtersStr+") that matches platform \"java\" (must only be one)");
        } else {
            DeployUnit du = (DeployUnit) filters.get(0).eContainer();
            fcl.attachChild(installDeployUnit(instance, du));
        }

        return fcl;
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
    public Object createInstance(final Instance instance, final FlexyClassLoader classLoader) {
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
		List<KMFContainer> filters = td.select("deployUnits[]/filters[name=platform,value=java]");

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
		
		DeployUnit du = (DeployUnit) filters.get(0).eContainer();
		String tag = "class:" + td.getName() + ":" + td.getVersion();
		Value tdefClassName = du.findFiltersByID(tag);
		if (tdefClassName != null) {
			return tdefClassName.getValue();
		} else {
			throw new RuntimeException("Cannot find meta-data \"" + tag + "\" in DeployUnit " + du.getHashcode() + "/" + du.getName() + "/" + du.getVersion());
		}
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
                        setter.invoke(target, new Boolean(Boolean.parseBoolean(value)));
                        isSet = true;
                    }
                    if (pClazz.equals(int.class)) {
                        setter.invoke(target, Integer.parseInt(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Integer.class)) {
                        setter.invoke(target, new Integer(Integer.parseInt(value)));
                        isSet = true;
                    }
                    if (pClazz.equals(long.class)) {
                        setter.invoke(target, Long.parseLong(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Long.class)) {
                        setter.invoke(target, new Long(Long.parseLong(value)));
                        isSet = true;
                    }
                    if (pClazz.equals(double.class)) {
                        setter.invoke(target, Double.parseDouble(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Double.class)) {
                        setter.invoke(target, new Double(Double.parseDouble(value)));
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
                        setter.invoke(target, new Short(Short.parseShort(value)));
                        isSet = true;
                    }
                    if (pClazz.equals(float.class)) {
                        setter.invoke(target, Float.parseFloat(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Float.class)) {
                        setter.invoke(target, new Float(Float.parseFloat(value)));
                        isSet = true;
                    }
                    if (pClazz.equals(byte.class)) {
                        setter.invoke(target, Byte.parseByte(value));
                        isSet = true;
                    }
                    if (pClazz.equals(Byte.class)) {
                        setter.invoke(target, new Byte(Byte.parseByte(value)));
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
                        f.set(target, new Double(Double.parseDouble(value)));
                    }
                    if (f.getType().equals(String.class)) {
                        f.set(target, value);
                    }
                    if (f.getType().equals(short.class)) {
                        f.set(target, Short.parseShort(value));
                    }
                    if (f.getType().equals(Short.class)) {
                        f.set(target, new Short(Short.parseShort(value)));
                    }
                    if (f.getType().equals(float.class)) {
                        f.set(target, Float.parseFloat(value));
                    }
                    if (f.getType().equals(Float.class)) {
                        f.set(target, new Float(Float.parseFloat(value)));
                    }
                    if (f.getType().equals(byte.class)) {
                        f.set(target, Byte.parseByte(value));
                    }
                    if (f.getType().equals(Byte.class)) {
                        f.set(target, new Byte(Byte.parseByte(value)));
                    }
                    if (value.length() == 1) {
                        if (f.getType().equals(char.class)) {
                            f.set(target, value.charAt(0));
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                Log.error("No field corresponding to annotation, consistency error {} on {}", e, fieldName, target.getClass().getName());
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
