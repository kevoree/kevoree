package org.kevoree.kevscript.resolver;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.util.TypeFQN;

import java.util.Map;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class ModelResolver extends AbstractResolver {

    public ModelResolver(Resolver next) {
        super(next);
    }

    /**
     * Tries to find type in context model
     * @param fqn fqn of the type
     * @param model context model
     * @return resolved type
     * @throws KevScriptException if something goes wrong
     */
    @Override
    public TypeDefinition resolve(TypeFQN fqn, ContainerRoot model) throws KevScriptException {
        TypeDefinition tdef = findBestTdef(fqn, model);
        if (tdef != null) {
            if (!fqn.version.isDUTag() && !fqn.version.getDUS().isEmpty()) {
                // deployUnit versions are set explicitly: check them
                if (checkDUS(tdef, fqn.version.getDUS())) {
                    return tdef;
                }
            }
        }

        // unable to find type in model: ask next resolver
        return next().resolve(fqn, model);
    }

    private TypeDefinition findBestTdef(TypeFQN fqn, ContainerRoot model) {
        TypeDefinition[] tdefs = new TypeDefinition[1]; // trick the final lambda stuff :o
        org.kevoree.Package pkg = model.findPackagesByID(fqn.namespace);
        if (pkg != null) {
            String path = "typeDefinitions[name=" + fqn.name;
            if (fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
                path += ']';
            } else {
                path += ",version=" + fqn.version.tdef + ']';
            }
            pkg.select(path)
                    .stream()
                    .map(t -> (TypeDefinition) t)
                    .forEach(t -> {
                // if they are multiple versions of a TypeDefinition
                // then we need the LATEST one
                // or the only one if a version has been specified
                if (tdefs[0] != null) {
                    long t0 = Long.valueOf(tdefs[0].getVersion());
                    long t1 = Long.valueOf(t.getVersion());
                    if (t0 < t1) {
                        tdefs[0] = t;
                    }
                } else {
                    tdefs[0] = t;
                }
            });
        }
        return tdefs[0];
    }

    private boolean checkDUS(TypeDefinition tdef, Map<String, Object> duVersions) {
        boolean satisfied = true;
        for (Map.Entry<String, Object> entry : duVersions.entrySet()) {
            boolean found = false;
            for (int i=0; i < tdef.getDeployUnits().size(); i++) {
                DeployUnit du = tdef.getDeployUnits().get(i);
                String p = du.findFiltersByID("platform").getValue();
                if (p.equals(entry.getKey()) && du.getVersion().equals(entry.getValue().toString())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                satisfied = false;
                break;
            }
        }
        return satisfied;
    }
}
