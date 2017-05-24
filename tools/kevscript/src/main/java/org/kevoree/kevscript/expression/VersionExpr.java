package org.kevoree.kevscript.expression;

import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.util.TypeFQN;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class VersionExpr {

    public static TypeFQN.Version interpret(final IAST<Type> versNode, final Map<String, String> ctxVars)
            throws KevScriptException {
        TypeFQN.Version version = new TypeFQN.Version();
        version.tdef = TdefVersionExpr.interpret(versNode.getChildren().get(0), ctxVars);
        if (versNode.getChildren().size() == 1) {
            version.setDUTag("RELEASE");
        } else {
            IAST<Type> duVersionNode = versNode.getChildren().get(1);
            // vNode => (Release | Latest | CtxVar | VersionDecl)
            IAST<Type> vNode = duVersionNode.getChildren().get(0);
            switch (vNode.getType()) {
                case Release:
                    version.setDUTag(TypeFQN.Version.RELEASE);
                    break;

                case Latest:
                    version.setDUTag(TypeFQN.Version.LATEST);
                    break;

                case CtxVar:
                    String ctxVal = CtxVarExpr.interpret(vNode, ctxVars);
                    switch (ctxVal) {
                        case TypeFQN.Version.RELEASE:
                            version.setDUTag(TypeFQN.Version.RELEASE);
                            break;
                        case TypeFQN.Version.LATEST:
                            version.setDUTag(TypeFQN.Version.LATEST);
                            break;
                        default:
                            throw new KevScriptException("Context var %" + vNode.getChildren().get(0).childrenAsString() + "% for DeployUnit version must resolve to RELEASE or LATEST (current=" + ctxVal + ")");
                    }
                    break;

                case VersionDecl:
                    version.addDUVersions(VersionDeclExpr.interpret(vNode, ctxVars));
                    break;
            }
        }

        return version;
    }
}
