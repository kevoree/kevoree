package org.kevoree.kevscript.util;

import org.kevoree.ContainerRoot;
import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.Type;
import org.kevoree.resolver.util.MavenVersionComparator;
import org.waxeye.ast.IAST;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 16:04
 */
public class TypeDefinitionResolver {

    public static TypeDefinition resolve(ContainerRoot model, IAST<Type> typeNode) throws Exception {
        if (!typeNode.getType().equals(Type.TypeDef)) {
            throw new Exception("Parse error, should be a typedefinition : " + typeNode.toString());
        }
        String typeDefName = typeNode.getChildren().get(0).childrenAsString();
        String version = null;
        if (typeNode.getChildren().size() > 1) {
            version = typeNode.getChildren().get(1).childrenAsString();
        }

        String[] packages = typeDefName.split("\\.");
        org.kevoree.Package pack = null;
        for(int i=0;i<packages.length-1;i++){
            if(pack==null){
                pack = model.findPackagesByID(packages[i]);
            } else {
                pack = pack.findPackagesByID(packages[i]);
            }
        }

        TypeDefinition bestTD = null;
        assert pack != null;
        for (TypeDefinition td : pack.getTypeDefinitions()) {
            if (version != null) {
                if (td.getName().equals(typeDefName) && version.equals(td.getVersion())) {
                    return td;
                }
            } else {
                if (td.getName().equals(typeDefName)) {
                    if (bestTD == null) {
                        bestTD = td;
                    } else {
                        if (MavenVersionComparator.max(bestTD.getVersion(), td.getVersion()) == td.getVersion()) {
                            bestTD = td;
                        }
                    }
                }
            }
        }
        if (bestTD == null) {
            throw new Exception("TypeDefinition not found with : " + typeDefName.toString());
        }
        return bestTD;
    }

}
