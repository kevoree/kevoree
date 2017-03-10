package org.kevoree.kevscript.resolver;

import org.apache.commons.io.FileUtils;
import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.ModelSerializer;
import org.kevoree.modeling.api.compare.ModelCompare;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    public FileSystemResolver(Resolver next, String cacheRoot) {
        super(next);
        this.cacheRoot = cacheRoot;
        this.factory = new DefaultKevoreeFactory();
        this.loader = factory.createJSONLoader();
        this.serializer = factory.createJSONSerializer();
        this.compare = factory.createModelCompare();
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
        Log.debug("Looking for {} in {}", fqn, cacheRoot);

        ContainerRoot tdefModel = readFromFile(fqn);
        if (tdefModel != null) {
            // found in file system
            compare.merge(model, tdefModel).applyOn(model);
            Log.trace("FileSystemResolver is trying to find {} in cached model", fqn.toKevoreePath());
            List<KMFContainer> tdefs = model.select(fqn.toKevoreePath());
            if (!tdefs.isEmpty()) {
                for (KMFContainer elem : tdefs) {
                    TypeDefinition t = (TypeDefinition) elem;
                    if (((TypeDefinition) elem).getName().equals(fqn.name)) {
                        Log.info("Found {} in {}", fqn, cacheRoot);
                        return t;
                    }
                }
            }
        }

        boolean isTag = false;
        if (fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
            // tag version
            isTag = true;
        }

        TypeFQN fqnCache = fqn.copy();
        ContainerRoot emptyModel = factory.createContainerRoot();
        factory.root(emptyModel);
        // give the job to the next resolver but give him an empty model
        TypeDefinition tdef = next().resolve(fqn, emptyModel);
        String tdefModelStr = serializer.serialize(emptyModel);

        if (isTag) {
            saveToFile(fqnCache, tdefModelStr);
        }
        saveToFile(fqn, tdefModelStr);

        // all set: merge context model and new resolved model together
        compare.merge(model, emptyModel).applyOn(model);

        return tdef;
    }

    private ContainerRoot readFromFile(TypeFQN fqn) {
        Path path = getPath(fqn);

        try {
            Log.trace("FileSystemResolver is looking for {}", path);
            String tdefModelJson = FileUtils.readFileToString(path.toFile(), "UTF-8");
            return (ContainerRoot) loader.loadModelFromString(tdefModelJson).get(0);
        } catch (Exception e) {
            Log.trace("FileSystemResolver failed to read {} from {}", e, fqn, path);
            return null;
        }
    }

    private void saveToFile(TypeFQN fqn, String modelStr) {
        Path path = getPath(fqn);
        try {
            FileUtils.writeStringToFile(path.toFile(), modelStr, "UTF-8");
            Log.debug("FileSystemResolver cached {} in {}", fqn, path);
        } catch (Exception e) {
            Log.trace("FileSystemResolver failed to cache {} in {} (ignored)", e, fqn, path);
        }
    }

    private Path getPath(TypeFQN fqn) {
        return Paths.get(cacheRoot, fqn.namespace, fqn.name, fqn.version.tdef + "-" + fqn.version.du + ".json");
    }
}
