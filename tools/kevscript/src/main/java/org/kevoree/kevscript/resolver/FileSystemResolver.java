package org.kevoree.kevscript.resolver;

import org.apache.commons.io.FileUtils;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelCloner;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.ModelSerializer;
import org.kevoree.modeling.api.compare.ModelCompare;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class FileSystemResolver extends AbstractResolver {

    private String cacheRoot;
    private KevoreeFactory factory;
    private ModelLoader loader;
    private ModelSerializer serializer;
    private ModelCompare compare;
    private ModelCloner cloner;

    public FileSystemResolver(Resolver next, String cacheRoot) {
        super(next);
        this.cacheRoot = cacheRoot;
        this.factory = new DefaultKevoreeFactory();
        this.loader = factory.createJSONLoader();
        this.serializer = factory.createJSONSerializer();
        this.compare = factory.createModelCompare();
        this.cloner = factory.createModelCloner();
    }

    /**
     * Tries to resolve type from cached models on the file system ($HOME/.kevoree/tdefs)
     * @param fqn fqn of the type
     * @param model context model
     * @return resolved type
     * @throws KevScriptException if something goes wrong
     */
    @Override
    public TypeDefinition resolve(TypeFQN fqn, ContainerRoot model) throws KevScriptException {
        if (fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
            return askNext(fqn, model);
        } else {
            ContainerRoot tdefModel = readTdef(fqn);
            if (tdefModel != null) {
                // tdef found in filesystem: no need to hit registry for it
                // now looking for deployUnits
                if (!fqn.version.isDUTag()) {
                    // explicit versions specified
                    List<ContainerRoot> duModels = readDUS(fqn, fqn.version.getDUS());
                    if (duModels != null && !duModels.isEmpty()) {
                        // deployUnits found in filesystem
                        // TODO remove current tdefs DUs in order to use the resolved ones only?
                        compare.merge(model, tdefModel).applyOn(model);
                        TypeDefinition tdef = (TypeDefinition) model.findByPath(fqn.toKevoreePath());
                        duModels.forEach(duModel -> {
                            if (duModel != null) {
                                DeployUnit du = duModel.getPackages().get(0).getDeployUnits().get(0);
                                compare.merge(model, duModel).applyOn(model);
                                tdef.addDeployUnits((DeployUnit) model.findByPath(du.path()));
                            }
                        });
                        Log.info("Found {} in filesystem", fqn);
                        return tdef;
                    } else {
                        return askNext(fqn, model);
                    }
                } else {
                    // LATEST | RELEASE => gotta hit registry to resolve those tags
                    return askNext(fqn, model);
                }
            } else {
                return askNext(fqn, model);
            }
        }
    }

    private TypeDefinition askNext(TypeFQN fqn, ContainerRoot model) throws KevScriptException {
        ContainerRoot emptyModel = factory.createContainerRoot();
        factory.root(emptyModel);

        TypeDefinition tdef = next().resolve(fqn, emptyModel);
        // tdef has been resolved from registry
        // save it in filesystem
        saveTdef(fqn, tdef);
        saveDeployUnits(fqn, tdef.getDeployUnits());
        // TODO remove current tdefs DUs in order to use the resolved ones only?
        compare.merge(model, emptyModel).applyOn(model);
        return (TypeDefinition) model.findByPath(tdef.path());
    }

    private ContainerRoot readTdef(TypeFQN fqn) {
        Path path = getTdefPath(fqn);
        KMFContainer tdefModel = readFile(path.toFile());
        if (tdefModel != null) {
            TypeDefinition tdef = (TypeDefinition) tdefModel;
            ContainerRoot model = factory.createContainerRoot().withGenerated_KMF_ID("0");
            factory.root(model);
            org.kevoree.Package pkg = (org.kevoree.Package) factory.createPackage().withName(fqn.namespace);
            model.addPackages(pkg);
            pkg.addTypeDefinitions(tdef);
            return model;
        }
        return null;
    }

    private List<ContainerRoot> readDUS(TypeFQN fqn, final Map<String, Object> versions) {
        return versions.entrySet().stream()
                .map(entry -> readDU(fqn, entry.getKey(), entry.getValue().toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ContainerRoot readDU(TypeFQN fqn, String platform, String version) {
        Path path = getDUPath(fqn, platform, version);
        KMFContainer duModel = readFile(path.toFile());
        if (duModel != null) {
            DeployUnit du = (DeployUnit) duModel;
            ContainerRoot model = factory.createContainerRoot().withGenerated_KMF_ID("0");
            factory.root(model);
            org.kevoree.Package pkg = (org.kevoree.Package) factory.createPackage().withName(fqn.namespace);
            model.addPackages(pkg);
            pkg.addDeployUnits(du);
            return model;
        }
        return null;
    }

    private void saveTdef(TypeFQN fqn, TypeDefinition tdef) {
        tdef.getDeployUnits().clear();
        Path path = getTdefPath(fqn);
        String modelStr = serializer.serialize(tdef);
        writeFile(path.toFile(), modelStr);
    }

    private void saveDeployUnits(TypeFQN fqn, List<DeployUnit> dus) {
        dus.forEach(du -> saveDeployUnit(fqn, du));
    }

    private void saveDeployUnit(TypeFQN fqn, DeployUnit du) {
        Path path = getDUPath(fqn, du.findFiltersByID("platform").getValue(), du.getVersion());
        String modelStr = serializer.serialize(du);
        writeFile(path.toFile(), modelStr);
    }

    private void writeFile(File file, String data) {
        try {
            FileUtils.writeStringToFile(file, data, "UTF-8");
            Log.debug("FileSystemResolver cached {}", file.getAbsolutePath());
        } catch (Exception e) {
            Log.trace("FileSystemResolver failed to cache {} (ignored)", e, file.getAbsolutePath());
        }
    }

    private KMFContainer readFile(File file) {
        try {
            Log.trace("FileSystemResolver is looking for {}", file.getAbsolutePath());
            String modelStr = FileUtils.readFileToString(file, "UTF-8");
            return loader.loadModelFromString(modelStr).get(0);
        } catch (Exception e) {
            Log.trace("FileSystemResolver failed to read {}", e, file.getAbsoluteFile());
            return null;
        }
    }

    private Path getTdefPath(TypeFQN fqn) {
        return Paths.get(cacheRoot, fqn.namespace, fqn.name, fqn.version.tdef, "type.json");
    }

    private Path getDUPath(TypeFQN fqn, String platform, String version) {
        return Paths.get(cacheRoot, fqn.namespace, fqn.name, fqn.version.tdef, "deployUnits", platform,  version + ".json");
    }
}
