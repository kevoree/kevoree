package org.kevoree.kevscript;

import org.kevoree.kevscript.util.TypeFQN;
import org.waxeye.ast.IAST;

public class TypeFqnInterpreter {

	public TypeFQN interpret(final IAST<Type> node) {
		final String fqn = parseTypeFQN(node.getChildren().get(0));
		String namespace;
		String name;
		final int dotIndex = fqn.lastIndexOf(".");
		if (dotIndex == -1) {
			namespace = "kevoree";
			name = fqn;
		} else {
			namespace = fqn.substring(0, dotIndex);
			name = fqn.substring(dotIndex + 1);
		}
		final TypeFQN typeFqn = new TypeFQN();
		typeFqn.namespace = namespace;
		typeFqn.name = name;
		typeFqn.version = interpretVersion(node);
		return typeFqn;
	}

	private TypeFQN.Version interpretVersion(final IAST<Type> typeNode) {
		TypeFQN.Version version = new TypeFQN.Version();

		if (typeNode.getChildren().size() == 1) {
			version = TypeFQN.Version.defaultVersion();
		} else {
			IAST<Type> versionNode = typeNode.getChildren().get(1);
			if (versionNode.getChildren().isEmpty()) {
				version = TypeFQN.Version.defaultVersion();
			} else {
				if (versionNode.getChildren().get(0).getType().equals(Type._Char)) {
					// Tdef version is a number
					IAST<Type> duNode = null;
					version.tdef = "";
					for (IAST<Type> child : versionNode.getChildren()) {
						if (child.getType().equals(Type._Char)) {
							version.tdef += child.toString();
						} else {
							duNode = child;
						}
					}
					if (duNode == null) {
						version.du = TypeFQN.Version.RELEASE;
					} else {
						version.du = duNode.toString().toUpperCase();
					}
				} else {
					// Tdef version is LATEST
					version.tdef = TypeFQN.Version.LATEST;
					if (versionNode.getChildren().size() == 1) {
						version.du = TypeFQN.Version.RELEASE;
					} else {
						version.du = versionNode.getChildren().get(1).toString().toUpperCase();
					}
				}
			}
		}

		return version;
	}

	private String parseTypeFQN(final IAST<Type> node) {
		String fqn = "";
		for (IAST<Type> child: node.getChildren()) {
			if (child.getChildren().isEmpty()) {
				fqn += child.toString();
			} else {
				fqn += child.childrenAsString();
			}
		}
		return fqn;
	}
}
