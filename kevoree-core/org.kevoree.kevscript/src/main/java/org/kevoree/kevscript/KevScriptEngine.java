package org.kevoree.kevscript;

import org.kevoree.*;
import org.kevoree.api.KevScriptService;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kevscript.util.InstanceResolver;
import org.kevoree.kevscript.util.MergeResolver;
import org.kevoree.kevscript.util.PortResolver;
import org.kevoree.kevscript.util.TypeDefinitionResolver;
import org.kevoree.log.Log;
import org.waxeye.ast.IAST;
import org.waxeye.input.BufferFiller;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import java.io.InputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 15:53
 */
public class KevScriptEngine implements KevScriptService {

    Parser parser = new Parser();
    KevoreeFactory factory = new DefaultKevoreeFactory();

    public void execute(String script, ContainerRoot model) throws Exception {
        ParseResult<Type> parserResult = parser.parse(new InputBuffer(script.toCharArray()));
        interpret(parserResult.getAST(), model);
    }

    public void executeFromStream(InputStream script, ContainerRoot model) throws Exception {
        ParseResult<Type> parserResult = parser.parse(new InputBuffer(BufferFiller.asArray(script)));
        IAST<Type> ast = parserResult.getAST();
        if (ast != null) {
            interpret(parserResult.getAST(), model);
        } else {
            Log.error(parserResult.getError().toString());
        }
    }

    public void interpret(IAST<Type> node, ContainerRoot model) throws Exception {
        switch (node.getType()) {
            case KevScript:
                for (IAST<Type> child : node.getChildren()) {
                    interpret(child, model);
                }
                break;
            case Statement:
                for (IAST<Type> child : node.getChildren()) {
                    interpret(child, model);
                }
                break;
            case Add:
                TypeDefinition td = TypeDefinitionResolver.resolve(model, node.getChildren().get(1));
                if (td == null) {
                    throw new Exception("TypeDefinition not found : " + node.getChildren().get(1).childrenAsString());
                } else {
                    IAST<Type> instanceNames = node.getChildren().get(0);
                    if (instanceNames.getType().equals(Type.NameList)) {
                        for (IAST<Type> name : instanceNames.getChildren()) {
                            applyAdd(td, name, model);
                        }
                    } else {
                        applyAdd(td, instanceNames, model);
                    }
                }
                break;
            case Move:
                List<Instance> leftHands = InstanceResolver.resolve(model, node.getChildren().get(0));
                List<Instance> rightHands = InstanceResolver.resolve(model, node.getChildren().get(1));
                for (Instance leftH : leftHands) {
                    for (Instance rightH : rightHands) {
                        applyMove(leftH, rightH, model);
                    }
                }
                break;
            case Attach:
                List<Instance> leftHands2 = InstanceResolver.resolve(model, node.getChildren().get(0));
                List<Instance> rightHands2 = InstanceResolver.resolve(model, node.getChildren().get(1));
                for (Instance leftH : leftHands2) {
                    for (Instance rightH : rightHands2) {
                        applyAttach(leftH, rightH, model, false);
                    }
                }
                break;
            case Detach:
                List<Instance> leftHands3 = InstanceResolver.resolve(model, node.getChildren().get(0));
                List<Instance> rightHands3 = InstanceResolver.resolve(model, node.getChildren().get(1));
                for (Instance leftH : leftHands3) {
                    for (Instance rightH : rightHands3) {
                        applyAttach(leftH, rightH, model, true);
                    }
                }
                break;
            case AddRepo:
                Repository repo = factory.createRepository();
                repo.setUrl(node.getChildren().get(0).childrenAsString());
                model.addRepositories(repo);
                break;
            case Remove:
                List<Instance> toRemove = InstanceResolver.resolve(model, node.getChildren().get(0));
                for (Instance toDrop : toRemove) {
                    if (toDrop instanceof ComponentInstance) {
                        ComponentInstance ci = (ComponentInstance) toDrop;
                        for (Port p : ci.getProvided()) {
                            for (MBinding mb : p.getBindings()) {
                                mb.delete();
                            }
                        }
                        for (Port p : ci.getRequired()) {
                            for (MBinding mb : p.getBindings()) {
                                mb.delete();
                            }
                        }
                    }
                    toDrop.delete();
                }
                break;
            case Network:
                Log.error("Network not implemented yet !!!");

                break;
            case Include:
                MergeResolver.merge(model, node.getChildren().get(0).childrenAsString(), node.getChildren().get(1).childrenAsString());
                break;
            case Set:
                List<Instance> toChangeDico = InstanceResolver.resolve(model, node.getChildren().get(0));
                for (Instance target : toChangeDico) {
                    if (target.getDictionary() == null) {
                        target.setDictionary(factory.createDictionary());
                    }
                    IAST<Type> dictionary = node.getChildren().get(1);
                    String targetNode = null;
                    for (IAST<Type> list : dictionary.getChildren()) {

                        if (list.getChildren().size() > 1) {
                            IAST<Type> last = list.getChildren().get(list.getChildren().size() - 1);
                            if (last.getType().equals(Type.String)) {
                                targetNode = last.childrenAsString();
                            }
                        }

                        for (IAST<Type> attribute : list.getChildren()) {
                            String key = attribute.getChildren().get(0).childrenAsString();
                            DictionaryValue dicValue = target.getDictionary().findValuesByID(key);
                            if (dicValue == null) {
                                dicValue = factory.createDictionaryValue();
                                if (target.getTypeDefinition().getDictionaryType() != null) {
                                    DictionaryAttribute dicAtt = target.getTypeDefinition().getDictionaryType().findAttributesByID(key);
                                    if (dicAtt == null) {
                                        Log.error("Param does not existe in type {} -> {}", target.getName(), key);
                                    } else {
                                        dicValue.setAttribute(dicAtt);
                                    }
                                    target.getDictionary().addValues(dicValue);
                                }
                            }
                            String value = attribute.getChildren().get(1).childrenAsString();
                            dicValue.setValue(value);
                            if (targetNode != null) {
                                dicValue.setTargetNode(model.findNodesByID(targetNode));
                                if (dicValue.getTargetNode() == null) {
                                    Log.error("Node not found for @" + targetNode + " property");
                                }
                            }
                        }
                    }
                }
                break;
            case AddBinding:
                List<Instance> channelsInstance = InstanceResolver.resolve(model, node.getChildren().get(1));
                for (Instance instance : channelsInstance) {
                    Channel channel = (Channel) instance;
                    List<Port> ports = PortResolver.resolve(model, node.getChildren().get(0));
                    for (Port p : ports) {
                        MBinding mb = factory.createMBinding();
                        mb.setPort(p);
                        mb.setHub(channel);
                        model.addMBindings(mb);
                    }
                }
                break;
            case DelBinding:
                List<Instance> channelsInstance2 = InstanceResolver.resolve(model, node.getChildren().get(1));
                List<Port> ports = PortResolver.resolve(model, node.getChildren().get(0));

                for (Instance instance : channelsInstance2) {
                    Channel channel = (Channel) instance;
                    MBinding toDrop = null;
                    for (MBinding mb : channel.getBindings()) {
                        for (Port p : ports) {
                            if (mb.getPort().equals(p)) {
                                toDrop = mb;
                            }
                        }

                    }
                    if (toDrop != null) {
                        toDrop.delete();
                    }
                }
                break;
            default:
                System.out.println(node);
                break;
        }
    }


    private void applyAttach(Instance leftH, Instance rightH, ContainerRoot model, boolean reverse) {
        if (!(leftH instanceof ContainerNode)) {
            Log.error("Not a ContainerNode {}", leftH.getName());
        }
        if (!(rightH instanceof Group)) {
            Log.error("Not a Group {}", rightH.getName());
        }
        ContainerNode node = (ContainerNode) leftH;
        Group group = (Group) rightH;
        if (!reverse) {
            group.addSubNodes(node);
        } else {
            group.removeSubNodes(node);
        }

    }

    private void applyMove(Instance leftH, Instance rightH, ContainerRoot model) {
        if (!(rightH instanceof ContainerNode)) {
            Log.error("Not a ContainerNode {}", rightH.getName());
        } else {
            ContainerNode node = (ContainerNode) rightH;
            if (leftH instanceof ComponentInstance) {
                node.addComponents((ComponentInstance) leftH);
            } else {
                if (leftH instanceof ContainerNode) {
                    node.addHosts((ContainerNode) leftH);
                } else {
                    Log.error("Not a containerNode or component : {}", leftH.getName());
                }
            }
        }
    }

    private boolean applyAdd(TypeDefinition td, IAST<Type> name, ContainerRoot model) throws Exception {
        boolean process = false;
        if (td instanceof NodeType) {
            ContainerNode instance = factory.createContainerNode();
            instance.setTypeDefinition(td);
            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
                String newNodeName = name.getChildren().get(0).childrenAsString();
                instance.setName(newNodeName);
                if (model.findNodesByID(newNodeName) != null) {
                    throw new Exception("Node already exsiste for name : " + newNodeName);
                }
                model.addNodes(instance);
                process = true;
            } else {
                throw new Exception("Bad node name : " + name.toString());
            }
        }
        if (td instanceof ComponentType) {
            ComponentInstance instance = factory.createComponentInstance();
            instance.setTypeDefinition(td);
            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 2) {
                instance.setName(name.getChildren().get(1).childrenAsString());
                //add port
                ComponentType ctd = (ComponentType) td;
                for (PortTypeRef rport : ctd.getProvided()) {
                    org.kevoree.Port newPort = factory.createPort();
                    newPort.setPortTypeRef(rport);
                    instance.addProvided(newPort);
                }
                for (PortTypeRef rport : ctd.getRequired()) {
                    org.kevoree.Port newPort = factory.createPort();
                    newPort.setPortTypeRef(rport);
                    instance.addRequired(newPort);
                }
                ContainerNode parentNode = model.findNodesByID(name.getChildren().get(0).childrenAsString());
                if (parentNode == null) {
                    throw new Exception("Can find parent node for name : " + name.getChildren().get(1).childrenAsString());
                } else {
                    parentNode.addComponents(instance);
                    process = true;
                }
            } else {
                throw new Exception("Bad component name (must be nodeName.componentName) : " + name.toString());
            }
        }
        if (td instanceof ChannelType) {
            Channel instance = factory.createChannel();
            instance.setTypeDefinition(td);
            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
                instance.setName(name.getChildren().get(0).childrenAsString());
                model.addHubs(instance);
                process = true;
            } else {
                throw new Exception("Bad channel name : " + name.toString());
            }
        }
        if (td instanceof GroupType) {
            Group instance = factory.createGroup();
            instance.setTypeDefinition(td);
            if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
                instance.setName(name.getChildren().get(0).childrenAsString());
                model.addGroups(instance);
                process = true;
            } else {
                throw new Exception("Bad group name : " + name.toString());
            }
        }
        return process;
    }


}
