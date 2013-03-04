package org.kevoree.library.javase.webSocketGrp.group;

import java.util.ArrayList;
import java.util.List;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.webSocketGrp.WebSocketComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;

@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true) })
@GroupType
@Library(name = "JavaSE", names = "Android")
public class WebSocketGroup extends AbstractGroupType implements
		WebSocketComponent {

	protected Logger logger = LoggerFactory.getLogger(WebSocketGroup.class);
	private List<WebSocketConnection> activeConnections = new ArrayList<WebSocketConnection>();

	@Start
	public void startWebSocketGroup() {
		int port = Integer.parseInt(getDictionary().get("port").toString());
		WebServer server = WebServers.createWebServer(port);
		server.add("/model/current", new BaseWebSocketHandler() {
			
			@Override
			public void onMessage(WebSocketConnection connection, String msg)
					throws Throwable {
				// TODO faudrait peut-être threader ça, parce que si tu rends pas la main vite à
				// la réception d'un message je pense que le serveur va plus trop répondre
				logger.debug("Model received: loading...");
				ContainerRoot model = KevoreeXmiHelper.$instance.loadString(msg);
				logger.debug("Model loaded from XMI String");
//				processOnModelReceived(externalSender, model);
			}
			
			@Override
			public void onOpen(WebSocketConnection connection) throws Exception {
				// keep a pointer on the new connection
				activeConnections.add(connection);
			}
			
			@Override
			public void onClose(WebSocketConnection connection)
					throws Exception {
				// remove pointer from active connections on close
				activeConnections.remove(connection);
			}
		});

		BaseWebSocketHandler socketHandler = new BaseWebSocketHandler() {
			@Override
			public void onOpen(WebSocketConnection connection) throws Exception {
				// when a connection is open it means that you can push you
				// stuff
			}

			@Override
			public void onMessage(WebSocketConnection connection, String msg)
					throws Throwable {

			}

			@Override
			public void onClose(WebSocketConnection connection)
					throws Exception {

			}
		};
	}

	@Stop
	public void stopWebSocketGroup() {

	}

	protected void processOnModelReceived(boolean externalSender,
			ContainerRoot model) {
		// DO NOT NOTIFY ALL WHEN REC FROM THIS GROUP
//		final boolean finalexternalSender = externalSender;
//		final ContainerRoot finalModel = model;
//		Runnable t = new Runnable() {
//			@Override
//			public void run() {
//				if (!finalexternalSender) {
//					getModelService().unregisterModelListener(self);
//				}
//				handler.atomicUpdateModel(finalModel);
//				if (!finalexternalSender) {
//					getModelService().registerModelListener(self);
//				}
//			}
//		};
//		poolUpdate.submit(t);
	}

	@Override
	public ContainerRoot pull(String targetNodeName) throws Exception {

		return null;
	}

	@Override
	public void push(ContainerRoot model, String targetNodeName)
			throws Exception {
		logger.debug("PUSH PUSH LADY LIGHTNING");
	}

	@Override
	public void triggerModelUpdate() {
		logger.debug("triggerModelUpdate !!!!!");
	}

}
