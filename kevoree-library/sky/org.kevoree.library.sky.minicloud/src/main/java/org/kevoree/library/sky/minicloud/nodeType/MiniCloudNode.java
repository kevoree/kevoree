package org.kevoree.library.sky.minicloud.nodeType;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.Server;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.util.Duration;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.kevoree.annotation.*;
import org.kevoree.library.sky.manager.KevoreeNodeRunner;
import org.kevoree.library.sky.manager.nodeType.IaaSNode;
import org.kevoree.library.sky.minicloud.HttpServer;
import org.kevoree.library.sky.minicloud.MiniCloudKevoreeNodeRunner;
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

@Library(name = "SKY")
@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "7000", optional = false)
})
@NodeType
public class MiniCloudNode extends IaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(MiniCloudNode.class);

	private Server server;

	@Override
	public KevoreeNodeRunner createKevoreeNodeRunner (String nodeName, String bootStrapModel) {
		return new MiniCloudKevoreeNodeRunner(nodeName, bootStrapModel);
	}

	@Start
	@Override
	public void startNode () {

		super.startNode();

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
		super.stopNode();
		server.close(Duration.apply(300, TimeUnit.MILLISECONDS));
	}

}
