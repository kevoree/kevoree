package org.kevoree.library.sky.manager.nodeType;

import org.kevoree.AdaptationPrimitiveType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.NodeType;
import org.kevoree.annotation.PrimitiveCommands;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.PrimitiveCommand;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.manager.Helper;
import org.kevoree.library.sky.manager.KevoreeNodeManager;
import org.kevoree.library.sky.manager.KevoreeNodeRunner;
import org.kevoree.library.sky.manager.command.AddNodeCommand;
import org.kevoree.library.sky.manager.command.RemoveNodeCommand;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.kevoreeAdaptation.KevoreeAdaptationFactory;
import org.kevoreeAdaptation.ParallelStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Some;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@PrimitiveCommands(value = {}, values = {SkyNode.REMOVE_NODE, SkyNode.ADD_NODE})
@NodeType
public class SkyNode extends JavaSENode {
	private static final Logger logger = LoggerFactory.getLogger(SkyNode.class);

	protected static final String REMOVE_NODE = "RemoveNode";
	protected static final String ADD_NODE = "AddNode";

	public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName, String bootStrapModel) {
		return null;
	}


	@Start
	@Override
	public void startNode () {

		super.startNode();

		KevoreeNodeManager.setNode(this);

		Helper.setModelHandlerService(this.getModelService());
		Helper.setNodeName(this.getNodeName());
	}

	@Stop
	@Override
	public void stopNode () {
		logger.debug("stopping node type of " + this.getNodeName());
		super.stopNode();
		KevoreeNodeManager.stop();
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		logger.debug("starting kompare...");
		AdaptationPrimitiveType removeNodeType = null;
		AdaptationPrimitiveType addNodeType = null;
//		AdaptationPrimitiveType updateNodeType = null;
		// looking for managed AdaptationPrimitiveType
		for (AdaptationPrimitiveType primitiveType : current.getAdaptationPrimitiveTypesForJ()) {
			if (primitiveType.getName().equals(REMOVE_NODE)) {
				removeNodeType = primitiveType;
			} else if (primitiveType.getName().equals(ADD_NODE)) {
				addNodeType = primitiveType;
			} /*else if (primitiveType.getName().equals(UPDATE_NODE)) {
				updateNodeType = primitiveType;
			}*/
		}

		if (removeNodeType == null || addNodeType == null /*|| updateNodeType == null*/) {
			for (AdaptationPrimitiveType primitiveType : target.getAdaptationPrimitiveTypesForJ()) {
				if (primitiveType.getName().equals(REMOVE_NODE)) {
					removeNodeType = primitiveType;
				} else if (primitiveType.getName().equals(ADD_NODE)) {
					addNodeType = primitiveType;
				} /*else if (primitiveType.getName().equals(UPDATE_NODE)) {
					updateNodeType = primitiveType;
				}*/
			}
		}
		if (removeNodeType == null) {
			logger.warn("there is no adaptation primitive for " + REMOVE_NODE);
		}
		if (addNodeType == null) {
			logger.warn("there is no adaptation primitive for " + ADD_NODE);
		}
		/*if (updateNodeType == null) {
			logger.warn("there is no adaptation primitive for " + UPDATE_NODE);
		}*/

		AdaptationModel adaptationModel = KevoreeAdaptationFactory.eINSTANCE().createAdaptationModel();
		ParallelStep step = KevoreeAdaptationFactory.eINSTANCE().createParallelStep();
		adaptationModel.setOrderedPrimitiveSet(new Some<ParallelStep>(step));

		// find all containerNode to remove
		for (ContainerNode node : current.getNodesForJ()) {
			if (node.getName().equals(this.getNodeName())) {
				for (ContainerNode subNode : node.getHostsForJ()) {
					boolean found = false;
					for (ContainerNode node1 : target.getNodesForJ()) {
						if (node1.getName().equals(this.getNodeName())) {
							for (ContainerNode subNode1 : node1.getHostsForJ()) {
								if (subNode.getName().equals(subNode1.getName())) {
									found = true;
									break;
								}
							}
							if (!found) {
								// create RemoveNode command
								logger.debug("add a " + REMOVE_NODE + " adaptation primitive with " + subNode.getName()
										+ " as parameter");
								AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE()
										.createAdaptationPrimitive();
								command.setPrimitiveType(removeNodeType);
								command.setRef(subNode);
								ParallelStep subStep = KevoreeAdaptationFactory.eINSTANCE().createParallelStep();
								subStep.addAdaptations(command);
								adaptationModel.addAdaptations(command);
								step.setNextStep(new Some<ParallelStep>(subStep));
								step = subStep;
							}
						}
					}
				}
			}
		}

		// find all containerNode to add
		for (ContainerNode node : target.getNodesForJ()) {
			if (node.getName().equals(this.getNodeName())) {
				for (ContainerNode subNode : node.getHostsForJ()) {
					boolean found = false;
					for (ContainerNode node1 : current.getNodesForJ()) {
						if (node1.getName().equals(this.getNodeName())) {
							for (ContainerNode subNode1 : node1.getHostsForJ()) {
								if (subNode.getName().equals(subNode1.getName())) {
									found = true;
									// create UpdateNode command
									/*logger.debug("add a " + UPDATE_NODE + " adaptation primitive with " + subNode.getName()
																		+ " as parameter");
																AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE()
																		.createAdaptationPrimitive();
																command.setPrimitiveType(updateNodeType);
																command.setRef(subNode);
																ParallelStep subStep = KevoreeAdaptationFactory.eINSTANCE().createParallelStep();
																subStep.addAdaptations(command);
																adaptationModel.addAdaptations(command);
																step.setNextStep(new Some<ParallelStep>(subStep));
																step = subStep;*/
									break;
								}
							}
							if (!found) {
								// create AddNode command
								logger.debug("add a " + ADD_NODE + " adaptation primitive with " + subNode.getName()
										+ " as parameter");
								AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE()
										.createAdaptationPrimitive();
								command.setPrimitiveType(addNodeType);
								command.setRef(subNode);
								ParallelStep subStep = KevoreeAdaptationFactory.eINSTANCE().createParallelStep();
								subStep.addAdaptations(command);
								adaptationModel.addAdaptations(command);
								step.setNextStep(new Some<ParallelStep>(subStep));
								step = subStep;
							}
						}
					}
				}
			}
		}

		AdaptationModel superModel = super.kompare(current, target);
		adaptationModel.addAllAdaptations(superModel.getAdaptations());
		step.setNextStep(superModel.getOrderedPrimitiveSet());
		logger.debug("Kompare model contain " + adaptationModel.getAdaptations().size() + " primitives");

		for (AdaptationPrimitive p : adaptationModel.getAdaptationsForJ()) {
			logger.debug("primitive " + p.getPrimitiveType().getName());
		}


		return adaptationModel;
	}

	@Override
	public PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		logger.debug("ask for primitiveCommand corresponding to " + adaptationPrimitive.getPrimitiveType().getName());
		PrimitiveCommand command = null;
		if (adaptationPrimitive.getPrimitiveType().getName().equals(REMOVE_NODE)) {
			logger.debug("add REMOVE_NODE command on " + ((ContainerNode) adaptationPrimitive.getRef()).getName());
			command = new RemoveNodeCommand((ContainerNode) adaptationPrimitive.getRef(),
					(ContainerRoot) (((ContainerNode) adaptationPrimitive.getRef()).eContainer()));
		} else if (adaptationPrimitive.getPrimitiveType().getName().equals(ADD_NODE)) {
			logger.debug("add ADD_NODE command on " + ((ContainerNode) adaptationPrimitive.getRef()).getName());
			command = new AddNodeCommand((ContainerNode) adaptationPrimitive.getRef(),
					(ContainerRoot) (((ContainerNode) adaptationPrimitive.getRef()).eContainer()));
		} /*else if (adaptationPrimitive.getPrimitiveType().getName().equals(UPDATE_NODE)) {
			command = new UpdateNodeCommand((ContainerNode) adaptationPrimitive.getRef(),
					(ContainerRoot) (((ContainerNode) adaptationPrimitive.getRef()).eContainer()), kevoreeNodeManager);
		}*/
		if (command == null) {
			command = super.getPrimitive(adaptationPrimitive);
		}
		return command;
	}
}
