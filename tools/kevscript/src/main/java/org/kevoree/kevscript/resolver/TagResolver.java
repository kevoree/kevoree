package org.kevoree.kevscript.resolver;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class TagResolver extends AbstractResolver {

    private Map<String, String> tdefTags;
    private Map<String, Map<String, String>> latestDUS;
    private Map<String, Map<String, String>> releaseDUS;

    public TagResolver(Resolver next) {
        super(next);
        this.tdefTags = new HashMap<>();
        this.latestDUS = new HashMap<>();
        this.releaseDUS = new HashMap<>();
    }

    /**
     * Tries to convert tags (LATEST, RELEASE) to a real version if possible
     * @param fqn fqn of the type
     * @param model context model
     * @return resolved type
     * @throws KevScriptException if something goes wrong
     */
    @Override
    public TypeDefinition resolve(TypeFQN fqn, ContainerRoot model) throws KevScriptException {
        final String key = fqn.namespace + "." + fqn.name;
        final boolean duIsTag = fqn.version.isDUTag();
        final String tdefTag = fqn.version.tdef;
        final String duTag = fqn.version.duTag;

        boolean tdefChanged = false;
        boolean duChanged = false;

        if (tdefTag.equals(TypeFQN.Version.LATEST)) {
            String tdefVersion = tdefTags.get(key);
            if (tdefVersion != null) {
                // found typeDef version in cache
                fqn.version.tdef = tdefVersion;
                Log.trace("TagResolver changed {}.{}/LATEST to {}.{}/{}", fqn.namespace, fqn.name, fqn.namespace, fqn.name, tdefVersion);
                tdefChanged = true;
            }
        }

        if (!fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
            Map<String, String> duVersions = null;
            if (fqn.version.isDUTag()) {
                if (fqn.version.duTag.equals(TypeFQN.Version.LATEST)) {
                    duVersions = latestDUS.get(key + "/" + fqn.version.tdef);
                } else if (fqn.version.duTag.equals(TypeFQN.Version.RELEASE)) {
                    duVersions = releaseDUS.get(key + "/" + fqn.version.tdef);
                }
                if (duVersions != null) {
                    // found du versions in cache
                    fqn.version.addDUVersions(duVersions);
                    String fqnStr = key + "/" + fqn.version.tdef;
                    Log.trace("TagResolver changed {}/{} to {}/{}", fqnStr, duTag, fqnStr, fqn.version.getDUS());
                    duChanged = true;
                }
            }
        }

        TypeDefinition tdef = next().resolve(fqn, model);
        if (!tdefChanged && tdefTag.equals(TypeFQN.Version.LATEST)) {
            tdefTags.put(key, tdef.getVersion());
            Log.trace("TagResolver linked {}/LATEST to {}/{}", key, key, tdef.getVersion());
        }

        if (!duChanged) {
            if (duIsTag) {
                if (duTag.equals(TypeFQN.Version.LATEST)) {
                    latestDUS.put(key + '/' + tdef.getVersion(), getDUVersions(tdef.getDeployUnits()));
                    Log.trace("TagResolver linked {} to {}/{}/{}", fqn, key, fqn.version.tdef, latestDUS.get(key + '/' + tdef.getVersion()));
                } else if (duTag.equals(TypeFQN.Version.RELEASE)) {
                    releaseDUS.put(key + '/' + tdef.getVersion(), getDUVersions(tdef.getDeployUnits()));
                    Log.trace("TagResolver linked {} to {}/{}/{}", fqn, key, fqn.version.tdef, releaseDUS.get(key + '/' + tdef.getVersion()));
                }
            }
        }
        return tdef;
    }

    private Map<String, String> getDUVersions(List<DeployUnit> dus) {
        Map<String, String> versions = new HashMap<>();
        dus.forEach(du -> {
            String platform = du.findFiltersByID("platform").getValue();
            versions.put(platform, du.getVersion());
        });
        return versions;
    }
}
