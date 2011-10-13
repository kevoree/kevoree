package org.kevoree.library.sky.minicloud;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.Server;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.util.Duration;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.kevoree.AdaptationPrimitiveType;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.PrimitiveCommand;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.minicloud.command.AddNodeCommand;
import org.kevoree.library.sky.minicloud.command.RemoveNodeCommand;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.kevoreeAdaptation.KevoreeAdaptationFactory;
import org.kevoreeAdaptation.ParallelStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Some;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@NodeType
@Library(name = "SKY")
@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "7000", optional = false)
})
@PrimitiveCommands(value = {},
		values = {MiniCloudNode.REMOVE_NODE, MiniCloudNode.ADD_NODE, "UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "StartInstance", "StopInstance"})
public class MiniCloudNode extends JavaSENode {
	private static final Logger logger = LoggerFactory.getLogger(MiniCloudNode.class);

	protected static final String REMOVE_NODE = "RemoveNode";
	protected static final String ADD_NODE = "AddNode";
	protected static final String UPDATE_NODE = "UpdateNode";

	private Server server;
//	protected KevoreeNodeManager kevoreeNodeManager;

	@Start
	@Override
	public void startNode () {

		super.startNode();

		// start KevoreeNodeManager
//		kevoreeNodeManager = new KevoreeNodeManager(this);
		KevoreeNodeManager.setNode(this);

		// start HTTP Server
		String port = (String) this.getDictionary().get("port");
		int portint = Integer.parseInt(port);

		Service<HttpRequest, HttpResponse> myService = new HttpServer.Respond(this.getModelService());
		server = ServerBuilder.safeBuild(myService, ServerBuilder.get().codec(Http.get())
				.bindTo(new InetSocketAddress(portint))
				.name(this.getNodeName()));

		Helper.setModelHandlerService(this.getModelService());
		Helper.setNodeName(this.getNodeName());
	}

	@Stop
	@Override
	public void stopNode () {
		super.stopNode();
		server.close(Duration.apply(300, TimeUnit.MILLISECONDS));
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
						if (subNode.getName().equals(node1.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						// create RemoveNode command
						logger.debug("add a " + REMOVE_NODE + " adaptation primitive with " + subNode.getName()
								+ " as parameter");
						AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE().createAdaptationPrimitive();
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

		// find all containerNode to add
		for (ContainerNode node : target.getNodesForJ()) {
			if (node.getName().equals(this.getNodeName())) {
				for (ContainerNode subNode : node.getHostsForJ()) {
					boolean found = false;
					for (ContainerNode node1 : current.getNodesForJ()) {
						if (subNode.getName().equals(node1.getName())) {
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
						AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE().createAdaptationPrimitive();
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

		AdaptationModel superModel = super.kompare(current, target);
		adaptationModel.addAllAdaptations(superModel.getAdaptations());
		step.setNextStep(superModel.getOrderedPrimitiveSet());

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

	/*@Override
	public void push (String physicalNodeName, ContainerRoot root) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			KevoreeXmiHelper.saveStream(outStream, root);
			outStream.flush();

			String IP = KevoreePlatformHelper
					.getProperty(root, physicalNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
			if (IP.equals("")) {
				IP = "127.0.0.1";
			}
			String PORT = foundPort(physicalNodeName, root);
			if (PORT.equals("")) {
				PORT = "7000";
			}
			URL url = new URL("http://" + IP + ":" + PORT + "/model/current");

			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(2000);
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(outStream.toString());
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = rd.readLine();
			while (line != null) {
				line = rd.readLine();
			}
			wr.close();
			rd.close();

		} catch (Exception e) {
			logger.error("Unable to push a model on " + physicalNodeName, e);
		}
	}

	private String foundPort(String physicalNodeName, ContainerRoot root) {
		for (ContainerNode node : root.getNodesForJ()) {
			if (node.getName().equals(physicalNodeName)) {
				for (DictionaryValue value : node.getDictionary().get().getValuesForJ()) {
					if (value.getAttribute().getName().equals("port")) {
						return value.getValue();
					}
				}
			}
		}
		return "7000";
	}*/
}
