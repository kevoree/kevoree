package org.kevoree.kevscript;

import java.util.Objects;

import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.kevscript.version.VersionDef;
import org.waxeye.ast.IAST;

public class TypeFqnInterpreter {

	public TypeFQN interpret(final IAST<Type> node) {
		final String fqn = parseTypeFQN(node.getChildren().get(0));
		String namespace = "";
		String name;
		final VersionDef version = interpretVersion(node);
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
		typeFqn.version = version;
		return typeFqn;
	}

	private VersionDef interpretVersion(final IAST<Type> typeNode) {
		final VersionDef ret;
		if (typeNode.getChildren().size() > 1) {
			final IAST<Type> versionNode = typeNode.getChildren().get(1);
			final boolean isRelease;
			if (versionNode.getChildren().size() > 1) {
				final IAST<Type> versionTDPart = versionNode.getChildren().get(1);
				isRelease = Objects.equals(versionTDPart.getType(), Type.Release);
			} else {
				isRelease = true;
			}
			if (Objects.equals(versionNode.getChildren().get(0).getType(), Type.Latest)) {
				ret = VersionDef.latestDu(isRelease);
			} else {
				final Long version = Long.parseLong(versionNode.getChildren().get(0).toString());
				ret = VersionDef.version(version, isRelease);
			}
			// a version is explicitly defined

		} else {
			/*
			 * no version requirement are defined. In such case the version of
			 * the TD is set to LATEST and the version of the DU is set to
			 * RELEASE (we are looking for the latest stable version).
			 * 
			 */
			ret = VersionDef.defaultVersion();

		}
		return ret;
	}

	private String parseTypeFQN(final IAST<Type> node) {
		String fqn = "";
		for (final IAST<Type> child : node.getChildren()) {
			fqn += child.childrenAsString();
		}
		return fqn;
	}

}
