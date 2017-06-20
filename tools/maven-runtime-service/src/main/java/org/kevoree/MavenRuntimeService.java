package org.kevoree;

import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.kevoree.api.ChannelContext;
import org.kevoree.api.ChannelContextImpl;
import org.kevoree.api.Context;
import org.kevoree.api.InstanceContext;
import org.kevoree.core.KevoreeCore;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.reflect.Injector;
import org.kevoree.resolver.MavenResolver;
import org.kevoree.resolver.MavenResolverException;
import org.kevoree.service.ContextAwareModelServiceAdapter;
import org.kevoree.service.ModelService;
import org.kevoree.service.RuntimeService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Created by leiko on 6/7/17.
 */
public class MavenRuntimeService implements RuntimeService {

    private static final URL[] URL_ARRAY = new URL[] {};

    private KevoreeCore core;
    private Injector injector;
    private Map<String, ClassLoader> classLoaders;
    private MavenResolver resolver;

    public MavenRuntimeService(KevoreeCore core, Injector injector) throws MavenResolverException {
        this.core = core;
        this.injector = injector;
        this.classLoaders = new HashMap<>();
        this.resolver = new MavenResolver.Builder().build();
    }

    @Override
    public ClassLoader get(String key) {
        return this.classLoaders.get(key);
    }

    @Override
    public ClassLoader get(DeployUnit du) {
        return this.classLoaders.get(du.getUrl());
    }

    @Override
    public ClassLoader installDeployUnit(DeployUnit du) throws KevoreeCoreException {
        Value kevVersion = du.findFiltersByID("kevoree_version");
        if (kevVersion == null || kevVersion.getValue() == null) {
            throw new KevoreeCoreException("DeployUnit " + du.getName() + ":" + du.getVersion() + " is incompatible with current runtime v" + core.getFactory().getVersion());
        } else if (!kevVersion.getValue().equals(core.getFactory().getVersion())) {
            throw new KevoreeCoreException("DeployUnit " + du.getName() + ":" + du.getVersion() + " targets v" + kevVersion.getValue() + " which is incompatible with current runtime v" + core.getFactory().getVersion());
        }

        final String KEVOREE_API_ARTIFACT = "org" + File.separator + "kevoree" + File.separator + "org.kevoree.api" + File.separator + "org.kevoree.api-" + core.getFactory().getVersion() + ".jar";
        final List<RemoteRepository> repos = new ArrayList<>();
        getRepositories(du)
                .forEach((id, url) -> repos.add(new RemoteRepository.Builder(id, "default", url).build()));

        ClassLoader classLoader = this.classLoaders.get(du.getUrl());

        if (classLoader != null) {
            return classLoader;
        }

        try {
            Log.info("Resolving ............. {}", du.getUrl());
            long before = System.currentTimeMillis();
            PreorderNodeListGenerator nlg = resolver
                    .resolve(du.getUrl(), repos);
            Log.info("Resolved in {}ms", (System.currentTimeMillis() - before));

            URL[] jars = nlg.getFiles()
                    .stream()
                    .map(f -> {
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(f -> !f.toString().endsWith(KEVOREE_API_ARTIFACT))
                    .collect(Collectors.toList()).toArray(URL_ARRAY);

            Log.trace("ClassLoader content for {}:{}:", du.getName(), du.getVersion());
            for (URL url : jars) {
                Log.trace(" - {}", url.toString());
            }

            classLoader = new URLClassLoader(jars, core.getClass().getClassLoader());
            this.classLoaders.put(du.getUrl(), classLoader);
            return classLoader;
        } catch (MavenResolverException e) {
            throw new KevoreeCoreException("Unable to resolve DeployUnit " + du.getName() + ":" + du.getVersion(), e);
        }
    }

    @Override
    public ClassLoader installTypeDefinition(Instance instance) throws KevoreeCoreException {
        TypeDefinition td = instance.getTypeDefinition();
        DeployUnit du = validateFilters(instance, td.select("deployUnits[]/filters[name=platform,value=java]"));
        return installDeployUnit(du);
    }

    @Override
    public void removeDeployUnit(DeployUnit du) {

    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        return this.injector.get(serviceClass);
    }

    @Override
    public synchronized Object createInstance(final Instance instance, final ClassLoader classLoader)
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

    private String searchMainClassName(final Instance instance) throws KevoreeCoreException {
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

    private DeployUnit validateFilters(Instance instance, List<KMFContainer> filters) throws KevoreeCoreException {
        if (filters.isEmpty()) {
            throw new KevoreeCoreException("Instance " + instance2fqn(instance) + " has no DeployUnit for \"java\" platform");
        } else if (filters.size() > 1) {
            StringBuilder filtersStr = new StringBuilder();
            for (int i=0; i < filters.size(); i++) {
                filtersStr.append(filters.get(i).eContainer().path());
                if (i < filters.size() - 1) {
                    filtersStr.append(", ");
                }
            }
            throw new RuntimeException("Instance " + instance2fqn(instance) + " has " + filters.size() + " deployUnits ("+filtersStr+") that matches platform \"java\" (must only be one)");
        }

        return (DeployUnit) filters.get(0).eContainer();
    }

    private Map<String, String> getRepositories(DeployUnit deployUnit) {
        Map<String, String> repositories = new HashMap<>();
        // hacky way to treat Maven repositories as I don't want to change the Kevoree MM
        for (Value val : deployUnit.getFilters()) {
            if (val.getName().startsWith("repo_")) {
                repositories.put(val.getName().substring(5), val.getValue());
            }
        }

        return repositories;
    }

    private String instance2fqn(Instance instance) {
        if (instance instanceof ContainerNode || instance instanceof Group || instance instanceof Channel) {
            return instance.getName() + ": " + instance.getTypeDefinition().getName() + "/" + instance.getTypeDefinition().getVersion();
        } else {
            return ((NamedElement) instance.eContainer()).getName() + "." + instance.getName() + ": " + instance.getTypeDefinition().getName() + "/" + instance.getTypeDefinition().getVersion();
        }
    }
}
