package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 16:04
 */
public class TypeDefinitionResolver {

    public static TypeDefinition resolve(ContainerRoot model, String typeName) {
        for (TypeDefinition td : model.getTypeDefinitions()) {
            if (td.getName().equals(typeName)) {
                return td;
            }
        }
        return null;
    }

}
