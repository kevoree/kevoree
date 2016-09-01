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

			} else if (versionNode.getChildren().size() == 1) {
				// eg. add node0: JavaNode/1
				// eg. add node1: JavaNode/LATEST
				version.tdef = versionNode.getChildren().get(0).toString().toUpperCase();
				version.du = TypeFQN.Version.RELEASE;

			} else {
				// eg. add node0: JavaNode/1/LATEST
				// eg. add node0: JavaNode/1/RELEASE
				version.tdef = versionNode.getChildren().get(0).toString().toUpperCase();
				version.du = versionNode.getChildren().get(1).toString().toUpperCase();
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
