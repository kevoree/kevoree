package org.kevoree.kevscript.version;

import java.util.List;

import org.kevoree.TypeDefinition;

public interface IVersionResolver {

	TypeDefinition findBestVersion(String typeDefName, String expectedVersion, List<TypeDefinition> availableTypeDef);
}
