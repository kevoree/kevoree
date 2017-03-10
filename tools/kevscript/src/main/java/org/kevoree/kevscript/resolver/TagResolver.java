package org.kevoree.kevscript.resolver;

import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by leiko on 3/8/17.
 */
public class TagResolver extends AbstractResolver {

    private Map<String, String> tags;

    public TagResolver(Resolver next) {
        super(next);
        this.tags = new HashMap<>();
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
        String fqnCache = fqn.toString();
        if (fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
            String version = this.tags.get(fqnCache);
            if (version != null) {
                // found version in cache
                Log.trace("TagResolver changed {} to {} for {}", fqn.version.tdef, version, fqn);
                fqn.version.tdef = version;
                return next().resolve(fqn, model);
            } else {
                // unable to find version in cache: resolve
                TypeDefinition tdef = next().resolve(fqn, model);
                if (!fqn.version.tdef.equals(TypeFQN.Version.LATEST)) {
                    this.tags.put(fqnCache, fqn.version.tdef);
                    Log.trace("TagResolver linked {} <-> {}", fqnCache, fqn);
                }
                return tdef;
            }
        } else {
            // version is not LATEST no need to tag it
            return next().resolve(fqn, model);
        }
    }
}
