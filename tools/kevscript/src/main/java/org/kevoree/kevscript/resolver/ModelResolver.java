package org.kevoree.kevscript.resolver;

import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;

import java.util.List;

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
        Log.debug("Looking for {} in model", fqn);
        Log.trace("ModelResolver is looking for {}", fqn.toKevoreePath());

        List<KMFContainer> tdefs = model.select(fqn.toKevoreePath());
        if (!tdefs.isEmpty()) {
            for (KMFContainer elem : tdefs) {
                TypeDefinition t = (TypeDefinition) elem;
                if (((TypeDefinition) elem).getName().equals(fqn.name)) {
                    Log.info("Found {} in model", fqn);
                    return t;
                }
            }
        }

        return next().resolve(fqn, model);
    }
}
