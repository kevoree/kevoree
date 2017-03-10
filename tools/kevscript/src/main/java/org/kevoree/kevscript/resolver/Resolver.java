package org.kevoree.kevscript.resolver;

import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.KevScriptException;
import org.kevoree.kevscript.util.TypeFQN;

/**
 *
 * Created by leiko on 3/8/17.
 */
public interface Resolver {

    TypeDefinition resolve(TypeFQN fqn, ContainerRoot model) throws KevScriptException;
}
