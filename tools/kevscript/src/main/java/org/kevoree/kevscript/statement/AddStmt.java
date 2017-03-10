package org.kevoree.kevscript.statement;

import org.kevoree.*;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.kevscript.Type;
import org.kevoree.kevscript.expression.InterpretExpr;
import org.kevoree.kevscript.expression.TypeDefExpr;
import org.kevoree.kevscript.resolver.Resolver;
import org.kevoree.kevscript.util.TypeFQN;
import org.waxeye.ast.IAST;

import java.util.Map;

/**
 *
 * Created by leiko on 12/15/16.
 */
public class AddStmt {

    public static void interpret(IAST<Type> stmt, ContainerRoot model, Map<String, String> ctxVars,
                                 Resolver resolver) throws KevScriptException {
        TypeFQN fqn = TypeDefExpr.interpret(stmt.getChildren().get(1), ctxVars);
        TypeDefinition td = resolver.resolve(fqn, model);
        if (td == null) {
            throw new KevScriptException("Unable to find TypeDefinition \"" + fqn.toString() + "\" in model");
        } else {
            final IAST<Type> instanceNames = stmt.getChildren().get(0);
            if (instanceNames.getType().equals(Type.NameList)) {
                for (final IAST<Type> name : instanceNames.getChildren()) {
                    apply(td, name, model, ctxVars);
                }
            } else {
                apply(td, instanceNames, model, ctxVars);
            }
        }
    }

    private static void apply(TypeDefinition td, IAST<Type> name, ContainerRoot model, Map<String, String> ctxVars)
            throws KevScriptException {
        DefaultKevoreeFactory factory = new DefaultKevoreeFactory();
        Instance i = null;
        if (td instanceof NodeType) {
            final ContainerNode instance = factory.createContainerNode();
            instance.setTypeDefinition(td);
            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
                final String newNodeName = InterpretExpr.interpret(name.getChildren().get(0), ctxVars);
                instance.setName(newNodeName);
                if (model.findNodesByID(newNodeName) != null) {
                    throw new KevScriptException("Node already exist for name : " + newNodeName);
                }

                model.addNodes(instance);
                i = instance;
            } else {
                final String parentNodeName = InterpretExpr.interpret(name.getChildren().get(0), ctxVars);
                final String newNodeName = InterpretExpr.interpret(name.getChildren().get(1), ctxVars);
                instance.setName(newNodeName);
                final ContainerNode parentNode = model.findNodesByID(parentNodeName);
                if (parentNode == null) {
                    throw new KevScriptException("Node not exist for name : " + parentNodeName);
                }
                model.addNodes(instance);
                parentNode.addHosts(instance);
                i = instance;
            }
        }
        if (td instanceof ComponentType) {
            final ComponentInstance instance = factory.createComponentInstance();
            instance.setTypeDefinition(td);
            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 2) {
                instance.setName(InterpretExpr.interpret(name.getChildren().get(1), ctxVars));
                // add port
                final ComponentType ctd = (ComponentType) td;
                for (final PortTypeRef rport : ctd.getProvided()) {
                    final org.kevoree.Port newPort = factory.createPort();
                    newPort.setPortTypeRef(rport);
                    newPort.setName(rport.getName());
                    instance.addProvided(newPort);
                }
                for (final PortTypeRef rport : ctd.getRequired()) {
                    final org.kevoree.Port newPort = factory.createPort();
                    newPort.setPortTypeRef(rport);
                    newPort.setName(rport.getName());
                    instance.addRequired(newPort);
                }

                String nodeName = InterpretExpr.interpret(name.getChildren().get(0), ctxVars);
                final ContainerNode parentNode = model.findNodesByID(nodeName);
                if (parentNode == null) {
                    throw new KevScriptException(
                            "Unable to find a node named: " + nodeName);
                } else {
                    parentNode.addComponents(instance);
                    i = instance;
                }
            } else {
                throw new KevScriptException("Bad component name (must be nodeName.componentName) : " + name.toString());
            }
        }
        if (td instanceof ChannelType) {
            final Channel instance = factory.createChannel();
            instance.setTypeDefinition(td);

            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
                instance.setName(InterpretExpr.interpret(name.getChildren().get(0), ctxVars));
                model.addHubs(instance);
                i = instance;
            } else {
                throw new KevScriptException("Bad channel name : " + name.toString());
            }
        }
        if (td instanceof GroupType) {
            final Group instance = factory.createGroup();
            instance.setTypeDefinition(td);

            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
                instance.setName(InterpretExpr.interpret(name.getChildren().get(0), ctxVars));
                model.addGroups(instance);
                i = instance;
            } else {
                throw new KevScriptException("Bad group name : " + name.toString());
            }
        }
        if (i != null) {
            i.setStarted(true);
            Dictionary dictionary = factory.createDictionary().withGenerated_KMF_ID("0.0");
            DictionaryType dictionaryType = i.getTypeDefinition().getDictionaryType();
            if (dictionaryType != null) {
                for (DictionaryAttribute attr : dictionaryType.getAttributes()) {
                    if (!attr.getFragmentDependant()) {
                        Value value = factory.createValue();
                        value.setName(attr.getName());
                        value.setValue(attr.getDefaultValue());
                        dictionary.addValues(value);
                    }
                }
            }
            i.setDictionary(dictionary);
        } else {
            // TODO throw exception
        }
    }
}
