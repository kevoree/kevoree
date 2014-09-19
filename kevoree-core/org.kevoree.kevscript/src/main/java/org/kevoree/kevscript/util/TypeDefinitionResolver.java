package org.kevoree.kevscript.util;

import jet.runtime.typeinfo.JetValueParameter;
import org.jetbrains.annotations.NotNull;
import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.Type;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.KMFContainer;
import org.kevoree.modeling.api.util.ModelVisitor;
import org.kevoree.resolver.util.MavenVersionComparator;
import org.waxeye.ast.IAST;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 16:04
 */
public class TypeDefinitionResolver {

    public static TypeDefinition resolve(ContainerRoot model, IAST<Type> typeNode) throws Exception {
        if (!typeNode.getType().equals(Type.TypeDef)) {
            throw new Exception("Parse error, should be a TypeDefinition : " + typeNode.toString());
        }
        String typeFQN;
        if (typeNode.getChildren().get(0).getChildren().size() != 1) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < typeNode.getChildren().get(0).getChildren().size(); i++) {
                if (typeNode.getChildren().get(0).getChildren().get(i).getType().toString().toLowerCase().contains("string")) {
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

        String[] packages = typeDefName.split("\\.");
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
            for (org.kevoree.Package loopPack : model.getPackages()) {
                loopPack.deepVisitContained(new ModelVisitor() {
                    @Override
                    public void visit(@JetValueParameter(name = "elem") @NotNull KMFContainer kmfContainer, @JetValueParameter(name = "refNameInParent") @NotNull String s, @JetValueParameter(name = "parent") @NotNull KMFContainer kmfContainer2) {
                        if (kmfContainer instanceof TypeDefinition) {
                            TypeDefinition casted = (TypeDefinition) kmfContainer;
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
            for (TypeDefinition td : pack.getTypeDefinitions()) {
                if (td.getName().equals(packages[packages.length - 1])) {
                    selected.add(td);
                }
            }
        }
        TypeDefinition bestTD = null;

        //first round, check non snapshot version
        for (TypeDefinition td : selected) {
            if (td.getVersion() != null && !td.getVersion().toLowerCase().contains("snapshot")) {
                if (version != null) {
                    if (version.equals(td.getVersion())) {
                        return td;
                    }
                } else {
                    if (bestTD == null) {
                        bestTD = td;
                    } else {
                        if (MavenVersionComparator.max(bestTD.getVersion(), td.getVersion()).equals(td.getVersion())) {
                            bestTD = td;
                        }
                    }
                }
            }
        }
        if (bestTD == null) {
            Log.warn("Try to select snapshot in best effort mode for " + typeDefName.toString());
            for (TypeDefinition td : selected) {
                if (version != null) {
                    if (version.equals(td.getVersion())) {
                        return td;
                    }
                } else {
                    if (bestTD == null) {
                        bestTD = td;
                    } else {
                        if (MavenVersionComparator.max(bestTD.getVersion(), td.getVersion()).equals(td.getVersion())) {
                            bestTD = td;
                        }
                    }
                }
            }
        }

        //Still not found :( try again
        if (bestTD == null) {
            throw new Exception("TypeDefinition not found with : " + typeDefName.toString()+ " and version "+ version + " in " + selected.size() + " selected");
        }
        return bestTD;
    }

}
