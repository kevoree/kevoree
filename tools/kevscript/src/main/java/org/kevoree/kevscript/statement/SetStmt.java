package org.kevoree.kevscript.statement;

import org.kevoree.*;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.KevScriptException;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.expression.CtxVarExpr;
import org.kevoree.kevscript.expression.GenCtxVarExpr;
import org.kevoree.kevscript.expression.RealStringExpr;
import org.kevoree.kevscript.util.InstanceResolver;
import org.waxeye.ast.IAST;

import java.util.List;
import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class SetStmt {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars) throws KevScriptException {
        String propToSet;
        List<Instance> targetNodes = null;
        IAST<Type> setRightPart;

        if (stmt.getChildren().size() == 3) {
            // frag dep
            // (CtxVar | GenCtxVar | RealString)
            setRightPart = stmt.getChildren().get(2);
        } else {
            // (CtxVar | GenCtxVar | RealString)
            setRightPart = stmt.getChildren().get(1);
        }

        if (setRightPart.getType().equals(Type.RealString)) {
            propToSet = RealStringExpr.interpret(setRightPart);
        } else if (setRightPart.getType().equals(Type.CtxVar)) {
            propToSet = CtxVarExpr.interpret(setRightPart, ctxVars);
        } else {
            propToSet = GenCtxVarExpr.interpret(setRightPart, ctxVars);
        }

        if (stmt.getChildren().size() == 3) {
            targetNodes = InstanceResolver.resolve(stmt.getChildren().get(1), model, ctxVars);
        }

        final IAST<Type> leftHnodes = stmt.getChildren().get(0);
        if (leftHnodes.getChildren().size() < 2) {
            throw new KevScriptException("Bad kevs.dictionary value description");
        }

        final IAST<Type> portName = leftHnodes.getChildren().get(leftHnodes.getChildren().size() - 1);
        leftHnodes.getChildren().remove(portName);
        final List<Instance> toChangeDico = InstanceResolver.resolve(leftHnodes, model, ctxVars);
        final String propName = portName.childrenAsString();
        DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
        for (final Instance target : toChangeDico) {
            if (targetNodes == null) {
                if (target.getDictionary() == null) {
                    target.setDictionary(factory.createDictionary());
                }
                Value dicValue = target.getDictionary().findValuesByID(propName);
                if (dicValue == null) {
                    dicValue = factory.createValue();
                    if (target.getTypeDefinition().getDictionaryType() != null) {
                        final DictionaryAttribute dicAtt = target.getTypeDefinition().getDictionaryType()
                                .findAttributesByID(propName);
                        if (dicAtt == null) {
                            throw new KevScriptException(
                                    "Param does not exist in type " + target.getName() + " -> " + propName);
                        } else {
                            dicValue.setName(dicAtt.getName());
                        }
                    }
                    target.getDictionary().addValues(dicValue);
                }
                dicValue.setValue(propToSet);
            } else {
                for (final Instance targetNode : targetNodes) {
                    if (target.findFragmentDictionaryByID(targetNode.getName()) == null) {
                        final FragmentDictionary newDictionary = factory.createFragmentDictionary();
                        newDictionary.setName(targetNode.getName());
                        target.addFragmentDictionary(newDictionary);
                    }
                    Value dicValue = target.findFragmentDictionaryByID(targetNode.getName())
                            .findValuesByID(propName);
                    if (dicValue == null) {
                        dicValue = factory.createValue();
                        if (target.getTypeDefinition().getDictionaryType() != null) {
                            final DictionaryAttribute dicAtt = target.getTypeDefinition().getDictionaryType()
                                    .findAttributesByID(propName);
                            if (dicAtt == null) {
                                throw new KevScriptException(
                                        "Param does not existe in type " + target.getName() + " -> " + propName);
                            } else {
                                if (!dicAtt.getFragmentDependant()) {
                                    throw new KevScriptException(
                                            "Dictionary Attribute is not fragment dependent " + dicAtt.getName());
                                }
                                dicValue.setName(dicAtt.getName());
                            }
                        }
                        target.findFragmentDictionaryByID(targetNode.getName()).addValues(dicValue);
                    }
                    dicValue.setValue(propToSet);
                }
            }
        }
    }
}
