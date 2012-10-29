package org.kevoree.library.sky.api.nodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.sky.api.KevoreeNodeManager;
import org.kevoree.library.sky.api.KevoreeNodeRunner;
import org.kevoree.library.sky.api.PlanningManager;
//import org.kevoree.library.sky.api.http.IaaSHTTPServer;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@DictionaryType({
//		@DictionaryAttribute(name = "port", defaultValue = "7000", optional = false)      ,
        @DictionaryAttribute(name = "role", defaultValue = "host/container", vals = {"host", "container", "host/container"}, optional = true),
		@DictionaryAttribute(name = "log_folder", defaultValue = "/tmp", optional = true)
})
@NodeFragment
public abstract class AbstractHostNode extends JavaSENode implements HostNode {
	private static final Logger logger = LoggerFactory.getLogger(AbstractHostNode.class);


//	private IaaSHTTPServer server;

	private KevoreeNodeManager nodeManager;

	public abstract KevoreeNodeRunner createKevoreeNodeRunner (String nodeName);

	public KevoreeNodeManager getNodeManager () {
		return nodeManager;
	}

	@Start
	@Override
	public void startNode () {
		super.startNode();
		nodeManager = new KevoreeNodeManager(this);
//		nodeManager.start();
		// start HTTP Server
//		server = new IaaSHTTPServer(this);
//		String port = (String) this.getDictionary().get("port");
//		int portInt = Integer.parseInt(port);
//		server.startServer(portInt);
//		logger.info("IaaS node started , web console : http://localhost:{}", portInt);
	}

	@Stop
	@Override
	public void stopNode () {
		logger.debug("stopping node type of " + this.getNodeName());
//		server.stopServer();
		nodeManager.stop();
		super.stopNode();
//        server.close(Duration.apply(300, TimeUnit.MILLISECONDS));
	}

	public boolean isHost () {
		String role = this.getDictionary().get("role").toString();
		return (role != null && role.contains("host"));
	}

	public boolean isContainer () {
		String role = this.getDictionary().get("role").toString();
		return (role != null && role.contains("container"));
	}

	public AdaptationModel superKompare (ContainerRoot current, ContainerRoot target) {
		return super.kompare(current, target);
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		return PlanningManager.kompare(current, target, this);
	}

	public org.kevoree.api.PrimitiveCommand superGetPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return super.getPrimitive(adaptationPrimitive);
	}

	@Override
	public org.kevoree.api.PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return PlanningManager.getPrimitive(adaptationPrimitive, this);
	}
}
