package org.kevoree.kevscript.resolver;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.TypeDefinition;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.KevScriptException;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.registry.api.RegistryRestClient;
import org.kevoree.registry.api.model.TypeDef;

import java.util.List;

public class RegistryResolver implements Resolver {

    private String url;
    private RegistryRestClient client;

    public RegistryResolver(String url) {
        this.url = url;
        Log.info("Registry " + this.url);
        this.client = new RegistryRestClient(this.url, null);
    }

    /**
     * Tries to resolve type from a Kevoree Registry
     * @param fqn fqn of the type
     * @param model context model
     * @return resolved type
     * @throws KevScriptException if something goes wrong
     */
    @Override
    public TypeDefinition resolve(final TypeFQN fqn, final ContainerRoot model) throws KevScriptException {
        TypeDefinition tdef;

        try {
            Log.debug("Looking for " + fqn.toString() + " in {}", this.url);
            if (fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
                // specified version is LATEST: ask registry
                TypeDef regTdef = client.getLatestTypeDef(fqn.namespace, fqn.name);
                tdef = processRegistryTypeDef(fqn, regTdef, model);
            } else {
                // specified version is a real long
                TypeDef regTdef = client.getTypeDef(fqn.namespace, fqn.name, fqn.version.tdef);
                tdef = processRegistryTypeDef(fqn, regTdef, model);
            }
        } catch (UnirestException e) {
            throw new KevScriptException("Unable to resolve " + fqn, e);
        }

        return tdef;
    }

    private TypeDefinition processRegistryTypeDef(final TypeFQN fqn, final TypeDef regTdef, final ContainerRoot model)
            throws UnirestException, KevScriptException {
        TypeDefinition tdef;
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ModelCompare compare = factory.createModelCompare();
        ContainerRoot tmpModel = factory.createContainerRoot().withGenerated_KMF_ID("0");
        factory.root(tmpModel);

        if (regTdef != null) {
            fqn.version.tdef = regTdef.getVersion();
            Log.info("Found {} in {}", fqn, this.url);
            ModelLoader loader = factory.createJSONLoader();
            org.kevoree.Package pkg;

            try {
                pkg = createPackage(factory, tmpModel, fqn.namespace);
                tdef = (TypeDefinition) loader.loadModelFromString(regTdef.getModel()).get(0);
                pkg.addTypeDefinitions(tdef);
            } catch (Exception e) {
                throw new KevScriptException("Unable to merge " + fqn + " model (corrupted model on registry?)");
            }

            List<org.kevoree.registry.api.model.DeployUnit> regDus;
            if (fqn.version.du.equals(TypeFQN.Version.LATEST)) {
                regDus = client.getAllDeployUnitLatest(fqn.namespace, fqn.name, fqn.version.tdef);
            } else {
                regDus = client.getAllDeployUnitRelease(fqn.namespace, fqn.name, fqn.version.tdef);
            }

            if (regDus != null && !regDus.isEmpty()) {
                for (final org.kevoree.registry.api.model.DeployUnit regDu : regDus) {
                    ContainerRoot duModel = (ContainerRoot) loader.loadModelFromString(regDu.getModel()).get(0);
                    compare.merge(tmpModel, duModel).applyOn(tmpModel);
                    String path = pkg.path() + "/deployUnits[name=" + regDu.getName() + ",version=" + regDu.getVersion() + "]";
                    for (KMFContainer elem : tmpModel.select(path)) {
                        tdef.addDeployUnits((DeployUnit) elem);
                        Log.debug("DeployUnit {}/{}/{} added to {}",
                                regDu.getName(), regDu.getVersion(), regDu.getPlatform(), fqn);
                    }
                }
            } else {
                throw new KevScriptException("Unable to find any DeployUnit attached to " + fqn);
            }

        } else {
            throw new KevScriptException("Unable to find " + fqn + " in " + this.url);
        }

        factory.root(model); // just in case
        compare.merge(model, tmpModel).applyOn(model);

        return (TypeDefinition) model.findByPath(tdef.path());
    }

    private org.kevoree.Package createPackage(KevoreeFactory factory, ContainerRoot model, String namespace) {
        org.kevoree.Package deepestPkg = null;
        org.kevoree.Package pkg = null;
        String[] splitted = namespace.split("\\.");
        for (int i=0; i < splitted.length; i++) {
            org.kevoree.Package newPkg = factory.createPackage();
            newPkg.setName(splitted[i]);
            if (pkg != null) {
                pkg.addPackages(newPkg);
            } else {
                model.addPackages(newPkg);
            }
            pkg = newPkg;
            if (i + 1 == splitted.length) {
                deepestPkg = pkg;
            }
        }
        return deepestPkg;
    }
}