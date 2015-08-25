package org.kevoree.kevscript.util;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.version.IVersionResolver;
import org.kevoree.kevscript.version.SemverVersionResolver;
import org.kevoree.pmodeling.api.KMFContainer;
import org.kevoree.pmodeling.api.util.ModelVisitor;
import org.waxeye.ast.IAST;

import jet.runtime.typeinfo.JetValueParameter;

/**
 * Created with IntelliJ IDEA. User: duke Date: 25/11/2013 Time: 16:04
 */
public class TypeDefinitionResolver {

	private static final IVersionResolver resolver = new SemverVersionResolver();

	public static TypeDefinition resolve(final ContainerRoot model, final IAST<Type> typeNode) throws Exception {
		if (!typeNode.getType().equals(Type.TypeDef)) {
			throw new Exception("Parse error, should be a TypeDefinition : " + typeNode.toString());
		}
		String typeFQN;
		if (typeNode.getChildren().get(0).getChildren().size() != 1) {
			final StringBuilder builder = new StringBuilder();
			for (int i = 0; i < typeNode.getChildren().get(0).getChildren().size(); i++) {
				if (typeNode.getChildren().get(0).getChildren().get(i).getType().toString().toLowerCase()
						.contains("string")) {
					builder.append(typeNode.getChildren().get(0).getChildren().get(i).childrenAsString());
				} else {
					builder.append(typeNode.getChildren().get(0).getChildren().get(i));
				}
			}
			typeFQN = builder.toString();
		} else {
			typeFQN = typeNode.getChildren().get(0).getChildren().get(0).childrenAsString();
		}
		final String typeDefName = typeFQN;
		String version = null;
		if (typeNode.getChildren().size() > 1) {
			version = typeNode.getChildren().get(1).childrenAsString();
		}

		final String[] packages = typeDefName.split("\\.");
		org.kevoree.Package pack = null;
		for (int i = 0; i < packages.length - 1; i++) {
			if (pack == null) {
				pack = model.findPackagesByID(packages[i]);
			} else {
				pack = pack.findPackagesByID(packages[i]);
			}
		}
		final ArrayList<TypeDefinition> selected = new ArrayList<TypeDefinition>();
		if (pack == null) {
			for (final org.kevoree.Package loopPack : model.getPackages()) {
				loopPack.deepVisitContained(new ModelVisitor() {
					@Override
					public void visit(@JetValueParameter(name = "elem") @NotNull final KMFContainer kmfContainer,
							@JetValueParameter(name = "refNameInParent") @NotNull final String s,
							@JetValueParameter(name = "parent") @NotNull final KMFContainer kmfContainer2) {
						if (kmfContainer instanceof TypeDefinition) {
							final TypeDefinition casted = (TypeDefinition) kmfContainer;
							String name = casted.getName();
							if (name.contains(".")) {
								name = name.substring(name.lastIndexOf(".") + 1);
							}
							if (name.equals(typeDefName)) {
								selected.add((TypeDefinition) kmfContainer);
							}
						}
					}
				});
			}
		} else {
			for (final TypeDefinition td : pack.getTypeDefinitions()) {
				if (td.getName().equals(packages[packages.length - 1])) {
					selected.add(td);
				}
			}
		}
		final TypeDefinition bestTD = resolver.findBestVersion(typeDefName, version, selected);

		// Still not found :( try again
		if (bestTD == null) {
			throw new Exception("TypeDefinition not found with : " + typeDefName.toString() + " and version " + version
					+ " in " + selected.size() + " selected");
		}
		return bestTD;
	}
}
