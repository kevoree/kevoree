package org.kevoree.kevscript;

import org.kevoree.*;
import org.kevoree.api.KevScriptService;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.kevscript.util.InstanceResolver;
import org.kevoree.kevscript.util.MergeResolver;
import org.kevoree.kevscript.util.TypeDefinitionResolver;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;
import org.waxeye.ast.IAST;
import org.waxeye.input.BufferFiller;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
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

    /*
    public static void main(String[] args) throws FileNotFoundException {
        KevScriptEngine engine = new KevScriptEngine();
        JSONModelLoader loader = new JSONModelLoader();

        ContainerRoot root = (ContainerRoot) loader.loadModelFromStream(new FileInputStream("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.kevscript/src/examples/lib.json")).get(0);
        engine.executeFromStream(new FileInputStream("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.kevscript/src/examples/test.kevs"), root);
        JSONModelSerializer saver = new JSONModelSerializer();
        //saver.serializeToStream(root, System.out);
    } */



    public void execute(String script, ContainerRoot model) {
        ParseResult<Type> parserResult = parser.parse(new InputBuffer(script.toCharArray()));
        interpret(parserResult.getAST(), model);
    }

    public void executeFromStream(InputStream script, ContainerRoot model) {
        ParseResult<Type> parserResult = parser.parse(new InputBuffer(BufferFiller.asArray(script)));
        interpret(parserResult.getAST(), model);
    }

    private List<Instance> pending = new ArrayList<Instance>();

    public void interpret(IAST<Type> node, ContainerRoot model) {
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
                TypeDefinition td = TypeDefinitionResolver.resolve(model, node.getChildren().get(1).childrenAsString());
                if (td == null) {
                    Log.error("TypeDefinition not found : {}", node.getChildren().get(1).childrenAsString());
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
                List<Instance> leftHands = InstanceResolver.resolve(model, node.getChildren().get(0), pending);
                List<Instance> rightHands = InstanceResolver.resolve(model, node.getChildren().get(1), pending);
                for (Instance leftH : leftHands) {
                    for (Instance rightH : rightHands) {
                        pending.remove(leftH);
                        applyMove(leftH, rightH, model);
                    }
                }
                break;
            case Attach:
                List<Instance> leftHands2 = InstanceResolver.resolve(model, node.getChildren().get(0), pending);
                List<Instance> rightHands2 = InstanceResolver.resolve(model, node.getChildren().get(1), pending);
                for (Instance leftH : leftHands2) {
                    for (Instance rightH : rightHands2) {
                        applyAttach(leftH, rightH, model, false);
                    }
                }
                break;
            case Detach:
                List<Instance> leftHands3 = InstanceResolver.resolve(model, node.getChildren().get(0), pending);
                List<Instance> rightHands3 = InstanceResolver.resolve(model, node.getChildren().get(1), pending);
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
                List<Instance> toRemove = InstanceResolver.resolve(model, node.getChildren().get(0), pending);
                for (Instance toDrop : toRemove) {
                    toDrop.delete();
                }
                break;
            case Network:
                Log.error("Network not implemented yet !!!");

                break;
            case Merge:
                MergeResolver.merge(model, node.getChildren().get(0).childrenAsString(), node.getChildren().get(1).childrenAsString());
                break;
            case Set:
                List<Instance> toChangeDico = InstanceResolver.resolve(model, node.getChildren().get(0), pending);
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
                            DictionaryValue att = target.getDictionary().findValuesByID(key);
                            if (att == null) {
                                att = factory.createDictionaryValue();
                                target.getDictionary().addValues(att);
                            }
                            String value = attribute.getChildren().get(1).childrenAsString();
                            att.setValue(value);
                            if (targetNode != null) {
                                att.setTargetNode(model.findNodesByID(targetNode));
                                if (att.getTargetNode() == null) {
                                    Log.error("Node not found for @" + targetNode + " property");
                                }
                            }
                        }
                    }
                }
                break;
            default:
                // System.out.println(node);
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

    private boolean applyAdd(TypeDefinition td, IAST<Type> name, ContainerRoot model) {
        boolean process = false;
        if (td instanceof NodeType) {
            ContainerNode instance = factory.createContainerNode();
            instance.setTypeDefinition(td);
            instance.setName(name.childrenAsString());
            model.addNodes(instance);
            process = true;
        }
        if (td instanceof ComponentType) {
            ComponentInstance instance = factory.createComponentInstance();
            instance.setTypeDefinition(td);
            instance.setName(name.childrenAsString());
            pending.add(instance);
            process = true;
        }
        if (td instanceof ChannelType) {
            Channel instance = factory.createChannel();
            instance.setTypeDefinition(td);
            instance.setName(name.childrenAsString());
            model.addHubs(instance);
            process = true;
        }
        if (td instanceof GroupType) {
            Group instance = factory.createGroup();
            instance.setTypeDefinition(td);
            instance.setName(name.childrenAsString());
            model.addGroups(instance);
            process = true;
        }
        return process;
    }


}
