package org.kevoree.library.sky.virtualCloud;

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
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.kevoree.framework.PrimitiveCommand;
import org.kevoree.library.sky.virtualCloud.command.AddNodeCommand;
import org.kevoree.library.sky.virtualCloud.command.RemoveNodeCommand;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.kevoreeAdaptation.KevoreeAdaptationFactory;
import org.kevoreeAdaptation.ParallelStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
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
@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "7000", optional = false)
})
@PrimitiveCommands(value = {}, values = {VirtualCloudNode.REMOVE_NODE, VirtualCloudNode.ADD_NODE/*, "UpdateNode"*/})
public class VirtualCloudNode extends AbstractNodeType {
	private static final Logger logger = LoggerFactory.getLogger(VirtualCloudNode.class);

	protected static final String REMOVE_NODE = "RemoveNode";
	protected static final String ADD_NODE = "AddNode";

	private Server server;
	protected KevoreeNodeManager kevoreeNodeManager;

	@Start
	@Override
	public void startNode () {

		// TODO start KevoreeNodeManager
		kevoreeNodeManager = new KevoreeNodeManager(this);

		// start HTTP Server
		String port = (String) this.getDictionary().get("port");
		int portint = Integer.parseInt(port);

		Service<HttpRequest, HttpResponse> myService = new HttpServer.Respond(this.getModelService(),kevoreeNodeManager);
		server = ServerBuilder
				.safeBuild(myService, ServerBuilder.get().codec(Http.get()).bindTo(new InetSocketAddress(portint))
						.name(this.getNodeName()));

		Helper.setModelHandlerServvice(this.getModelService());
		Helper.setNodeName(this.getNodeName());
	}

	@Stop
	@Override
	public void stopNode () {
		server.close(Duration.apply(300, TimeUnit.MILLISECONDS));
		kevoreeNodeManager.stop();
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		logger.debug("starting kompare...");
		AdaptationPrimitiveType removeNodeType = null;
		AdaptationPrimitiveType addNodeType = null;
		// looking for managed AdaptationPrimitiveType
		for (AdaptationPrimitiveType primitiveType : current.getAdaptationPrimitiveTypes()) {
			if (primitiveType.getName().equals(REMOVE_NODE)) {
				removeNodeType = primitiveType;
			} else if (primitiveType.getName().equals(ADD_NODE)) {
				addNodeType = primitiveType;
			}
		}

		if (removeNodeType == null || addNodeType == null) {
			for (AdaptationPrimitiveType primitiveType : target.getAdaptationPrimitiveTypes()) {
				if (primitiveType.getName().equals(REMOVE_NODE)) {
					removeNodeType = primitiveType;
				} else if (primitiveType.getName().equals(ADD_NODE)) {
					addNodeType = primitiveType;
				}
			}
		}
		if (removeNodeType == null) {
			logger.warn("there is no adaptation primitive for " + REMOVE_NODE);
		}
		if (addNodeType == null) {
			logger.warn("there is no adaptation primitive for " + ADD_NODE);
		}


		AdaptationModel adaptationModel = org.kevoreeAdaptation.KevoreeAdaptationFactory.eINSTANCE
				.createAdaptationModel();
		ParallelStep step = KevoreeAdaptationFactory.eINSTANCE.createParallelStep();
		adaptationModel.setOrderedPrimitiveSet(step);


		// find all containerNode to remove
		for (ContainerNode node : current.getNodes()) {
			if (node.getName().equals(this.getNodeName())) {
				for (ContainerNode subNode : node.getHosts()) {
					boolean found = false;
					for (ContainerNode node1 : target.getNodes()) {
						if (subNode.getName().equals(node1.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						// create RemoveNode command
						logger.debug("add a " + REMOVE_NODE + " adaptation primitive with " + subNode.getName() + " as parameter");
						AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive();
						command.setPrimitiveType(removeNodeType);
						command.setRef(subNode);
						step.getAdaptations().add(command);
						adaptationModel.getAdaptations().add(command);
					}
				}
			}
		}


		// find all containerNode to add
		for (ContainerNode node : target.getNodes()) {
			if (node.getName().equals(this.getNodeName())) {
				for (ContainerNode subNode : node.getHosts()) {
					boolean found = false;
					for (ContainerNode node1 : current.getNodes()) {
						if (subNode.getName().equals(node1.getName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						// create AddNode command
						logger.debug("add a " + ADD_NODE + " adaptation primitive with " + subNode.getName() + " as parameter");
						AdaptationPrimitive command = KevoreeAdaptationFactory.eINSTANCE.createAdaptationPrimitive();
						command.setPrimitiveType(addNodeType);
						command.setRef(subNode);
						step.getAdaptations().add(command);
						adaptationModel.getAdaptations().add(command);
					}
				}
			}
		}

		return adaptationModel;
	}

	@Override
	public PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		logger.debug("ask for primitiveCommand corresponding to " + adaptationPrimitive.getPrimitiveType().getName());
		PrimitiveCommand command = null;
		if (adaptationPrimitive.getPrimitiveType().getName().equals(REMOVE_NODE)) {
			command = new RemoveNodeCommand((ContainerNode) adaptationPrimitive.getRef(),
					(ContainerRoot) (adaptationPrimitive.getRef().eContainer()), kevoreeNodeManager);
		} else if (adaptationPrimitive.getPrimitiveType().getName().equals(ADD_NODE)) {
			command = new AddNodeCommand((ContainerNode) adaptationPrimitive.getRef(),
					(ContainerRoot) (adaptationPrimitive.getRef().eContainer()), kevoreeNodeManager);
		} else if (adaptationPrimitive.getPrimitiveType().getName().equals("UpdateNode")) {
			// TODO ?
		}
		return command;
	}

	@Override
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
			String PORT = KevoreePlatformHelper
					.getProperty(root, physicalNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
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


}
