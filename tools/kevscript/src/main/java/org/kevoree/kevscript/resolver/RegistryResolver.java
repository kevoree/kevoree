package org.kevoree.kevscript.resolver;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.kevoree.*;
import org.kevoree.Package;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.ModelCloner;
import org.kevoree.modeling.api.ModelLoader;
import org.kevoree.modeling.api.compare.ModelCompare;
import org.kevoree.registry.client.KevoreeRegistryClient;
import org.kevoree.registry.client.domain.RDeployUnit;
import org.kevoree.registry.client.domain.RTypeDefinition;

import java.util.Arrays;
import java.util.Map;

public class RegistryResolver implements Resolver {

    private String url;
    private KevoreeRegistryClient client;

    public RegistryResolver(String url) {
        this.url = url;
        Log.info("Registry " + this.url);
        this.client = new KevoreeRegistryClient(this.url);
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
        try {
            Log.debug("Looking for " + fqn.toString() + " in {}", this.url);
            HttpResponse<RTypeDefinition> tdefRes;
            if (fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
                // retrieve latest from registry
                tdefRes = client.getLatestTdef(fqn.namespace, fqn.name);
            } else {
                // specified version is a real long
                tdefRes = client.getTdef(fqn.namespace, fqn.name, Long.valueOf(fqn.version.tdef));
            }

            if (tdefRes.getStatus() == 200) {
                DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
                ModelCloner cloner = factory.createModelCloner();
                ModelCompare compare = factory.createModelCompare();

                ContainerRoot tmpModel = cloner.clone(model);
                factory.root(tmpModel);
                processRegistryTypeDef(fqn, tdefRes.getBody(), tmpModel);
                compare.merge(model, tmpModel).applyOn(model);
                return (TypeDefinition) model.findByPath("/packages[" + fqn.namespace + "]/typeDefinitions[name=" + fqn.name + ",version=" + fqn.version.tdef + "]");
            } else {
                throw new KevScriptException("Unable to find " + fqn.namespace + "." + fqn.name + "/" + fqn.version + " in " + this.url + " (status: " + tdefRes.getStatusText() + ")");
            }
        } catch (UnirestException e) {
            throw new KevScriptException("Unable to find " + fqn.namespace + "." + fqn.name + "/" + fqn.version + " in " + this.url, e);
        }
    }

    private void processRegistryTypeDef(final TypeFQN fqn, final RTypeDefinition regTdef, final ContainerRoot model)
            throws UnirestException, KevScriptException {
        KevoreeFactory factory = new DefaultKevoreeFactory();
        ModelLoader loader = factory.createJSONLoader();
        ModelCompare compare = factory.createModelCompare();

        fqn.version.tdef = regTdef.getVersion().toString();
        Log.info("Found {} in {}", fqn, this.url);
        TypeDefinition tdef = (TypeDefinition) loader.loadModelFromString(regTdef.getModel()).get(0);
        Package pkg = model.findPackagesByID(fqn.namespace);
        if (pkg == null) {
            pkg = (Package) factory.createPackage().withName(fqn.namespace);
            model.addPackages(pkg);
        }
        pkg.addTypeDefinitions(tdef);

        HttpResponse<RDeployUnit[]> dusRes;
        if (fqn.version.isDUTag()) {
            if (fqn.version.duTag.equals(TypeFQN.Version.LATEST)) {
                dusRes = client.getLatestsDus(fqn.namespace, fqn.name, Long.valueOf(fqn.version.tdef));
            } else {
                dusRes = client.getReleasesDus(fqn.namespace, fqn.name, Long.valueOf(fqn.version.tdef));
            }
        } else {
            // deployUnit versions are set explicitly
            dusRes = client.getSpecificDus(fqn.namespace, fqn.name, Long.valueOf(fqn.version.tdef), fqn.version.getDUS());
        }

        if (dusRes.getStatus() == 200) {
            RDeployUnit[] regDus = dusRes.getBody();
            if (!fqn.version.isDUTag()) {
                // confirm that versions from registry satisfy versions asked
                for (Map.Entry<String, Object> entry : fqn.version.getDUS().entrySet()) {
                    if (satisfyingDu(regDus, entry.getKey(), entry.getValue().toString()) == null) {
                        throw new KevScriptException("Unable to find satisfying DeployUnit " +
                                entry.toString() + " for " + fqn.namespace + "." + fqn.name + "/" + fqn.version.tdef +
                                " in " + this.url);
                    }
                }
            }

            // confirm that there is at least one DeployUnit for that type
            if (regDus.length == 0) {
                throw new KevScriptException("No DeployUnit found for " + fqn.namespace + "." + fqn.name + "/" + fqn.version.tdef + " that matches " + fqn.version.getDUS());
            }

            // merge DeployUnits to current model
            for (final RDeployUnit regDu : regDus) {
                ContainerRoot duModel = (ContainerRoot) loader.loadModelFromString(regDu.getModel()).get(0);
                compare.merge(model, duModel).applyOn(model);
                String path = "/packages[" + fqn.namespace + "]" + "/deployUnits[name=" + regDu.getName() + ",version=" + regDu.getVersion() + "]";
                for (KMFContainer elem : model.select(path)) {
                    DeployUnit du = (DeployUnit) elem;
                    tdef.addDeployUnits(du);
                    Log.debug(" + {}:{}:{} ({})",
                            regDu.getPlatform(), regDu.getName(), regDu.getVersion(), du.getHashcode());
                }
            }
        } else {
            throw new KevScriptException("Unable to find DeployUnits "+fqn.version.getDUS()+" for "
                    + fqn.namespace + "."+fqn.name+"/"+fqn.version.tdef+" in " + this.url
                    + " (status: " + dusRes.getStatusText() + ")");
        }
    }

    private RDeployUnit satisfyingDu(RDeployUnit[] dus, String platform, String version) {
        return Arrays.stream(dus)
                .filter(du -> du.getPlatform().equals(platform) && du.getVersion().equals(version))
                .findFirst()
                .orElse(null);
    }
}