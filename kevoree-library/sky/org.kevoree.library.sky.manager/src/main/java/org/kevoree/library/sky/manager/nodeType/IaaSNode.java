package org.kevoree.library.sky.manager.nodeType;


import com.twitter.finagle.Service;
import com.twitter.finagle.builder.Server;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.util.Duration;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.PrimitiveCommand;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.manager.*;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@PrimitiveCommands(value = {}, values = {IaaSNode.REMOVE_NODE, IaaSNode.ADD_NODE})
@DictionaryType({
		@DictionaryAttribute(name = "role", defaultValue = "host", vals = {"host", "container", "host/container"},
				optional = false),
		@DictionaryAttribute(name = "port", defaultValue = "7000", optional = false)
})
@NodeFragment
public abstract class IaaSNode extends JavaSENode {
	private static final Logger logger = LoggerFactory.getLogger(IaaSNode.class);

	public static final String REMOVE_NODE = "RemoveNode";
	public static final String ADD_NODE = "AddNode";

	private Server server;

	public abstract KevoreeNodeRunner createKevoreeNodeRunner (String nodeName, String bootStrapModel,
			ContainerRoot model); /*{
		logger.error("createKevoreeNodeRunner from IaaSNode must be override by subtypes and never be used as is");
		return null;
	}*/


	@Start
	@Override
	public void startNode () {

		super.startNode();

		KevoreeNodeManager.setNode(this);

		Helper.setModelHandlerService(this.getModelService());
		Helper.setNodeName(this.getNodeName());

		// start HTTP Server
		String port = (String) this.getDictionary().get("port");
		int portint = Integer.parseInt(port);

		Service<HttpRequest, HttpResponse> myService = new HttpServer.Respond(this.getModelService());
		server = ServerBuilder.safeBuild(myService, ServerBuilder.get().codec(Http.get())
				.bindTo(new InetSocketAddress(portint))
				.name(this.getNodeName()));
	}

	@Stop
	@Override
	public void stopNode () {
		logger.debug("stopping node type of " + this.getNodeName());
		super.stopNode();
		KevoreeNodeManager.stop();
		server.close(Duration.apply(300, TimeUnit.MILLISECONDS));
	}

	public boolean isHost () {
		String role = this.getDictionary().get("role").toString();
		return role.contains("host");
	}

	public boolean isContainer () {
		String role = this.getDictionary().get("role").toString();
		return role.contains("container");
	}

	public AdaptationModel superKompare (ContainerRoot current, ContainerRoot target) {
		return super.kompare(current, target);
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		return PlanningManager.kompare(current, target, this);
	}

	public PrimitiveCommand superGetPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return super.getPrimitive(adaptationPrimitive);
	}

	@Override
	public PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return PlanningManager.getPrimitive(adaptationPrimitive, this);
	}
}
