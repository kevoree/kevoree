package org.kevoree.kevscript;

import org.kevoree.*;
import org.kevoree.api.KevScriptService;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.factory.KevoreeFactory;
import org.kevoree.kevscript.util.InstanceResolver;
import org.kevoree.kevscript.util.KevoreeRegistryResolver;
import org.kevoree.kevscript.util.PortResolver;
import org.kevoree.kevscript.util.TypeFQN;
import org.kevoree.log.Log;
import org.waxeye.ast.IAST;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA. User: duke Date: 25/11/2013 Time: 15:53
 */
public class KevScriptEngine implements KevScriptService {

	private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final Parser parser = new Parser();
	private final KevoreeFactory factory = new DefaultKevoreeFactory();
	private final KevoreeRegistryResolver resolver;

	public KevScriptEngine(final String registryUrl) {
		resolver = new KevoreeRegistryResolver(registryUrl);
	}

	public void execute(final String script, final ContainerRoot model) throws Exception {
		this.execute(script, model, null);
	}

	public void execute(final String script, final ContainerRoot model, final HashMap<String, String> ctxVars)
			throws Exception {
		this.executeFromStream(new ByteArrayInputStream(script.getBytes()), model, ctxVars);
	}

	public void executeFromStream(final InputStream script, final ContainerRoot model, HashMap<String, String> ctxVars)
			throws Exception {
		if (ctxVars == null) {
			ctxVars = new HashMap<String, String>();
		}

		// override ctxVar with System.props (ie. -DctxVar.foo=bar
		// -DctxVar.port=4242)
		Properties props = System.getProperties();
		for (String propName : props.stringPropertyNames()) {
			String[] splitted = propName.split("\\.");
			if (splitted[0].equals("ctxVar")) {
				Log.debug("Adding ctxVar {}={}", splitted[1], System.getProperty(propName));
				ctxVars.put(splitted[1], System.getProperty(propName));
			}
		}

		String kevs = new Scanner(script).useDelimiter("\\A").next();
		Pattern p = Pattern.compile("(%(%([a-zA-Z0-9_]+)%)%)");
		Matcher m = p.matcher(kevs);
		while (m.find()) {
			ctxVars.put(m.group(3), shortId());
			kevs = kevs.replaceAll(m.group(1), m.group(2));
		}

		for (final String key : ctxVars.keySet()) {
			kevs = kevs.replaceAll("%" + key + "%", ctxVars.get(key));
		}

		p = Pattern.compile("(%[a-zA-Z0-9_]+%)");
		m = p.matcher(kevs);
		if (m.find()) {
			throw new Exception("Context variable " + m.group(1) + " has no value");
		} else {
			final ParseResult<Type> parserResult = parser.parse(new InputBuffer(kevs.toCharArray()));
			final IAST<Type> ast = parserResult.getAST();
			if (ast != null) {
				interpret(ast, model);
			} else {
				throw new Exception(parserResult.getError().toString());
			}
		}
	}

	public void executeFromStream(final InputStream script, final ContainerRoot model) throws Exception {
		this.executeFromStream(script, model, null);
	}

	private TypeFQN interpretTypeDef(IAST<Type> node) throws Exception {
		return new TypeFqnInterpreter().interpret(node);
	}

	public void interpret(final IAST<Type> node, final ContainerRoot model) throws Exception {
		StringBuilder builder;
		switch (node.getType()) {
		case KevScript:
			for (final IAST<Type> child : node.getChildren()) {
				interpret(child, model);
			}
			break;
		case Statement:
			for (final IAST<Type> child : node.getChildren()) {
				interpret(child, model);
			}
			break;
		case Add:
			TypeFQN fqn = interpretTypeDef(node.getChildren().get(1));
			TypeDefinition td = resolver.resolve(fqn, model);
			if (td == null) {
				throw new Exception("Unable to find TypeDefinition \"" + fqn.toString() + "\" in model");
			} else {
				final IAST<Type> instanceNames = node.getChildren().get(0);
				if (instanceNames.getType().equals(Type.NameList)) {
					for (final IAST<Type> name : instanceNames.getChildren()) {
						applyAdd(td, name, model);
					}
				} else {
					applyAdd(td, instanceNames, model);
				}
			}
			break;
		case Move:
			final List<Instance> leftHands = InstanceResolver.resolve(model, node.getChildren().get(0));
			final List<Instance> rightHands = InstanceResolver.resolve(model, node.getChildren().get(1));
			for (final Instance leftH : leftHands) {
				for (final Instance rightH : rightHands) {
					applyMove(leftH, rightH, model);
				}
			}
			break;
		case Attach:
			final List<Instance> leftHands2 = InstanceResolver.resolve(model, node.getChildren().get(0));
			final List<Instance> rightHands2 = InstanceResolver.resolve(model, node.getChildren().get(1));
			for (final Instance leftH : leftHands2) {
				for (final Instance rightH : rightHands2) {
					applyAttach(leftH, rightH, model, false);
				}
			}
			break;
		case Detach:
			final List<Instance> leftHands3 = InstanceResolver.resolve(model, node.getChildren().get(0));
			final List<Instance> rightHands3 = InstanceResolver.resolve(model, node.getChildren().get(1));
			for (final Instance leftH : leftHands3) {
				for (final Instance rightH : rightHands3) {
					applyAttach(leftH, rightH, model, true);
				}
			}
			break;
		case AddRepo:
			final Repository repo = factory.createRepository();
			repo.setUrl(node.getChildren().get(0).childrenAsString());
			model.addRepositories(repo);
			break;
		case Remove:
			final List<Instance> toRemove = InstanceResolver.resolve(model, node.getChildren().get(0));
			for (final Instance toDrop : toRemove) {
				if (toDrop instanceof ComponentInstance) {
					final ComponentInstance ci = (ComponentInstance) toDrop;
					for (final Port p : ci.getProvided()) {
						for (final MBinding mb : p.getBindings()) {
							mb.delete();
						}
					}
					for (final Port p : ci.getRequired()) {
						for (final MBinding mb : p.getBindings()) {
							mb.delete();
						}
					}
				}
				toDrop.delete();
			}
			break;

		case Start:
			final List<Instance> instances = InstanceResolver.resolve(model, node.getChildren().get(0));
			for (final Instance i : instances) {
				i.setStarted(true);
			}
			break;

		case Stop:
			final List<Instance> instances1 = InstanceResolver.resolve(model, node.getChildren().get(0));
			for (final Instance i : instances1) {
				i.setStarted(false);
			}
			break;

		case Pause:
			// TODO
			throw new Exception("Pause statement is not implemented yet.");

		case Network:
			final IAST<Type> leftHandNetwork = node.getChildren().get(0);
			if (leftHandNetwork.getChildren().size() != 3) {
				throw new Exception("Network must be : network nodeName.propertyType.interfaceName IP");
			} else {
				final String nodeName = leftHandNetwork.getChildren().get(0).childrenAsString();
				final String propType = leftHandNetwork.getChildren().get(1).childrenAsString();
				final String interfaceName = leftHandNetwork.getChildren().get(2).childrenAsString();
				final ContainerNode networkTargetNode = model.findNodesByID(nodeName);
				if (networkTargetNode == null) {
					throw new Exception("Node not found for name " + nodeName);
				}
				NetworkInfo info = networkTargetNode.findNetworkInformationByID(propType);
				if (info == null) {
					info = factory.createNetworkInfo();
					info.setName(propType);
					networkTargetNode.addNetworkInformation(info);
				}
				Value netprop = info.findValuesByID(interfaceName);
				if (netprop == null) {
					netprop = factory.createValue();
					netprop.setName(interfaceName);
					info.addValues(netprop);
				}
				netprop.setValue(node.getChildren().get(1).childrenAsString());
			}
			break;
		case Set:
			String propToSet = null;
			List<Instance> targetNodes = null;
			if (node.getChildren().size() == 3) {
				// frag dep
				builder = new StringBuilder();
				for (final IAST<Type> child : node.getChildren().get(2).getChildren()) {
					builder.append(child.childrenAsString());
				}
				propToSet = builder.toString();
				targetNodes = InstanceResolver.resolve(model, node.getChildren().get(1));
			} else {
				builder = new StringBuilder();
				for (final IAST<Type> child : node.getChildren().get(1).getChildren()) {
					switch (child.getType()) {
					case SingleQuoteLine:
					case DoubleQuoteLine:
						builder.append(child.childrenAsString());
						break;

					case NewLine:
						builder.append('\n');
						break;
					}
				}
				propToSet = builder.toString();
			}

			final IAST<Type> leftHnodes = node.getChildren().get(0);
			if (leftHnodes.getChildren().size() < 2) {
				throw new Exception("Bad dictionary value description");
			}

			final IAST<Type> portName = leftHnodes.getChildren().get(leftHnodes.getChildren().size() - 1);
			leftHnodes.getChildren().remove(portName);
			final List<Instance> toChangeDico = InstanceResolver.resolve(model, leftHnodes);
			final String propName = portName.childrenAsString();

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
								throw new Exception(
										"Param does not existe in type " + target.getName() + " -> " + propName);
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
									throw new Exception(
											"Param does not existe in type " + target.getName() + " -> " + propName);
								} else {
									if (!dicAtt.getFragmentDependant()) {
										throw new Exception(
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
			break;
		case AddBinding:
			final List<Instance> channelsInstance = InstanceResolver.resolve(model, node.getChildren().get(1));
			for (final Instance instance : channelsInstance) {
				final Channel channel = (Channel) instance;
				final List<Port> ports = PortResolver.resolve(model, node.getChildren().get(0));
				for (final Port p : ports) {
					final MBinding mb = factory.createMBinding();
					mb.setPort(p);
					mb.setHub(channel);
					model.addMBindings(mb);
				}
			}
			break;
		case DelBinding:
			final List<Instance> channelsInstance2 = InstanceResolver.resolve(model, node.getChildren().get(1));
			final List<Port> ports = PortResolver.resolve(model, node.getChildren().get(0));

			for (final Instance instance : channelsInstance2) {
				final Channel channel = (Channel) instance;
				MBinding toDrop = null;
				for (final MBinding mb : channel.getBindings()) {
					for (final Port p : ports) {
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
			Log.info("Deprecated KevScript statement: {}", node.getType().name());
			break;
		}
	}

	private void applyAttach(final Instance leftH, final Instance rightH, final ContainerRoot model,
			final boolean reverse) {
		if (!(leftH instanceof ContainerNode)) {
			Log.error("Not a ContainerNode {}", leftH.getName());
		}
		if (!(rightH instanceof Group)) {
			Log.error("Not a Group {}", rightH.getName());
		}
		final ContainerNode node = (ContainerNode) leftH;
		final Group group = (Group) rightH;
		if (!reverse) {
			group.addSubNodes(node);
		} else {
			group.removeSubNodes(node);
		}

	}

	private void applyMove(final Instance leftH, final Instance rightH, final ContainerRoot model) {
		if (!(rightH instanceof ContainerNode)) {
			Log.error("Not a ContainerNode {}", rightH.getName());
		} else {
			final ContainerNode node = (ContainerNode) rightH;
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

	private boolean applyAdd(final TypeDefinition td, final IAST<Type> name, final ContainerRoot model)
			throws Exception {
		Instance process = null;
		if (td instanceof NodeType) {
			final ContainerNode instance = factory.createContainerNode();
			instance.setTypeDefinition(td);
			if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
				final String newNodeName = name.getChildren().get(0).childrenAsString();
				instance.setName(newNodeName);
				if (model.findNodesByID(newNodeName) != null) {
					throw new Exception("Node already exist for name : " + newNodeName);
				}

				model.addNodes(instance);
				process = instance;
			} else {
				final String parentNodeName = name.getChildren().get(0).childrenAsString();
				final String newNodeName = name.getChildren().get(1).childrenAsString();
				instance.setName(newNodeName);
				final ContainerNode parentNode = model.findNodesByID(parentNodeName);
				if (parentNode == null) {
					throw new Exception("Node not exist for name : " + parentNodeName);
				}
				model.addNodes(instance);
				parentNode.addHosts(instance);
				process = instance;
			}
		}
		if (td instanceof ComponentType) {
			final ComponentInstance instance = factory.createComponentInstance();
			instance.setTypeDefinition(td);
			if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 2) {
				instance.setName(name.getChildren().get(1).childrenAsString());
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

				final ContainerNode parentNode = model.findNodesByID(name.getChildren().get(0).childrenAsString());
				if (parentNode == null) {
					throw new Exception(
							"Can find parent node for name : " + name.getChildren().get(1).childrenAsString());
				} else {
					parentNode.addComponents(instance);
					process = instance;
				}
			} else {
				throw new Exception("Bad component name (must be nodeName.componentName) : " + name.toString());
			}
		}
		if (td instanceof ChannelType) {
			final Channel instance = factory.createChannel();
			instance.setTypeDefinition(td);

			if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
				instance.setName(name.getChildren().get(0).childrenAsString());
				model.addHubs(instance);
				process = instance;
			} else {
				throw new Exception("Bad channel name : " + name.toString());
			}
		}
		if (td instanceof GroupType) {
			final Group instance = factory.createGroup();
			instance.setTypeDefinition(td);

			if (name.getType().equals(Type.InstancePath) && name.getChildren().size() == 1) {
				instance.setName(name.getChildren().get(0).childrenAsString());
				model.addGroups(instance);
				process = instance;
			} else {
				throw new Exception("Bad group name : " + name.toString());
			}
		}
		process.setStarted(true);
		return process != null;
	}

	private String shortId() {
		final StringBuilder builder = new StringBuilder();
		final Random random = new Random();
		for (int i = 0; i < 9; i++) {
			builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
		}
		return builder.toString();
	}
}
