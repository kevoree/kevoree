package org.kevoree;

import org.jboss.shrinkwrap.resolver.api.maven.*;
import org.jetbrains.annotations.NotNull;
import org.kevoree.annotation.KevoreeInject;
import org.kevoree.api.ChannelContext;
import org.kevoree.api.ChannelContextImpl;
import org.kevoree.api.Context;
import org.kevoree.api.InstanceContext;
import org.kevoree.core.KevoreeCore;
import org.kevoree.kcl.api.FlexyClassLoader;
import org.kevoree.kernel.KevoreeKernel;
import org.kevoree.kernel.KevoreeKernelImpl;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.reflect.Injector;
import org.kevoree.resolver.MavenResolver;
import org.kevoree.service.ContextAwareModelServiceAdapter;
import org.kevoree.service.ModelService;
import org.kevoree.service.RuntimeService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by leiko on 6/7/17.
 */
public class MavenRuntimeService implements RuntimeService {

    private KevoreeCore core;
    private KevoreeKernel kernel;
    private Injector injector;

    public MavenRuntimeService(KevoreeCore core, Injector injector) {
        this.kernel = new KevoreeKernelImpl();
        this.core = core;
        this.injector = injector;
    }

    @Override
    public FlexyClassLoader get(DeployUnit deployUnit) {
        return this.get("mvn:"+deployUnit.getUrl());
    }

    @Override
    public FlexyClassLoader get(String key) {
        return this.kernel.get(key);
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
                            // TODO are you sure that's the way to go?
                            if (dep.getScope().equals(ScopeType.RUNTIME)) {
                                try {
                                    parentCl.load(depJar);
                                } catch (IOException e) {
                                    Log.error("Unable to load jar {} in class loader {}", depJar.getAbsolutePath(), parentCl.getKey());
                                }
                            } else {
                                depCl = kernel.put(key, depJar);
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

    private Map<String, URL> getRepositories(DeployUnit deployUnit) {
        Map<String, URL> repositories = new HashMap<>();
        // hacky way to treat Maven repositories as I don't want to change the Kevoree MM
        for (Value val : deployUnit.getFilters()) {
            if (val.getName().startsWith("repo_")) {
                try {
                    repositories.put(val.getName().substring(5), new URL(val.getValue()));
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Invalid repository URL: " + val.getValue());
                }
            }
        }

        return repositories;
    }

    @Override
    public FlexyClassLoader installDeployUnit(DeployUnit du) throws KevoreeCoreException {
        Value kevVersion = du.findFiltersByID("kevoree_version");
        if (kevVersion == null || kevVersion.getValue() == null) {
            throw new KevoreeCoreException("DeployUnit " + du.getName() + ":" + du.getVersion() + " is incompatible with current runtime v" + core.getFactory().getVersion());
        } else if (!kevVersion.getValue().equals(core.getFactory().getVersion())) {
            throw new KevoreeCoreException("DeployUnit " + du.getName() + ":" + du.getVersion() + " targets v" + kevVersion.getValue() + " which is incompatible with current runtime v" + core.getFactory().getVersion());
        }

        FlexyClassLoader duClassLoader;
        ClassLoader previousCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(kernel.getRootClassLoader());

        try {
            duClassLoader = get(du);
            if (duClassLoader == null) {
                ConfigurableMavenResolverSystem resolver = MavenResolver.get(getRepositories(du));

                long before = System.currentTimeMillis();
                Log.info("Resolving ............. {}", du.getUrl());
                MavenResolvedArtifact artifact = resolver
                        .resolve(du.getUrl())
                        .withoutTransitivity()
                        .asSingleResolvedArtifact();

                File duJar = artifact.asFile();
                if (duJar != null && duJar.exists()) {
                    String key = getKey(artifact);
                    duClassLoader = kernel.put(key, duJar);
                    installDependencies(resolver, duClassLoader, artifact, 0);
                    Log.info("Resolved in {}ms", (System.currentTimeMillis() - before));
                } else {
                    Log.error("Unable to resolve {}", du.getUrl());
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previousCL);
        }

        return duClassLoader;
    }

    @Override
    public void removeDeployUnit(DeployUnit deployUnit) {
        kernel.drop(deployUnit.getUrl());
    }

    @Override
    public FlexyClassLoader installTypeDefinition(Instance instance) throws KevoreeCoreException {
        TypeDefinition td = instance.getTypeDefinition();
        DeployUnit du = validateFilters(instance, td.select("deployUnits[]/filters[name=platform,value=java]"));
        return installDeployUnit(du);
    }

    @Override
    public synchronized Object createInstance(final Instance instance, final FlexyClassLoader classLoader)
            throws KevoreeCoreException {
        final DeployUnit du = getJavaDeployUnit(instance);
        try {
            final String mainClassName = searchMainClassName(instance);
            final Class clazz = classLoader.loadClass(mainClassName);
            final Object newInstance = clazz.newInstance();

            final InstanceContext instanceContext = new InstanceContext(instance.path(), core.getNodeName(), instance.getName());
            injector.register(Context.class, instanceContext);
            injector.register(ModelService.class, new ContextAwareModelServiceAdapter(core, instance.path()));
            // TODO there should be a GroupContext also that gives attached nodes etc..
            injector.register(ChannelContext.class, new ChannelContextImpl(instanceContext));
            injector.inject(newInstance);

            return newInstance;
        } catch (NoClassDefFoundError e) {
            throw new KevoreeCoreException("@KevoreeInject failed (is " + du.getName() + ":" + du.getVersion() + ":"+du.getHashcode().substring(0, 6)+" up-to-date?)", e);
        } catch (Throwable e) {
            throw new KevoreeCoreException("Unable to create instance " + instance.getName(), e);
        }
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
    private DeployUnit getJavaDeployUnit(Instance instance) {
        TypeDefinition tdef = instance.getTypeDefinition();
        if (tdef != null) {
            return tdef.getDeployUnits().stream().filter(du -> {
                Value filter = du.findFiltersByID("platform");
                return filter != null && filter.getValue() != null && filter.getValue().equals("java");
            }).findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return injector.get(serviceClass);
    }

    @Override
    @Deprecated
    public File resolve(String url, Set<String> repos) {
        Log.warn("MavenRuntimeService#resolve(String, Set<String>) has been deprecated. You are not supposed to used that");
        ConfigurableMavenResolverSystem resolver = MavenResolver.get();
        return resolver.resolve(url).withTransitivity().asSingleFile();
    }
}
