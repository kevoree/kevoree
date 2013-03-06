package org.kevoree.library.javase.webSocketGrp.group;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.library.NodeNetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;

@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true) })
@GroupType
@Library(name = "JavaSE", names = "Android")
public class WebSocketGroup extends AbstractGroupType {

	protected Logger logger = LoggerFactory.getLogger(WebSocketGroup.class);
	
	private List<WebSocket> activeConnections = new ArrayList<WebSocket>();
	private WebSocketServer server;
	private int port;
	private boolean isStarted = false;

	@Start
	public void startWebSocketGroup() {
		port = Integer.parseInt(getDictionary().get("port").toString());
		
		server = new WebSocketServer(new InetSocketAddress(port)) {
			
			@Override
			public void onOpen(WebSocket socket, ClientHandshake hshk) {
				// keep a pointer on the new connection
				activeConnections.add(socket);
			}
			
			@Override
			public void onMessage(WebSocket socket, String msg) {
				// TODO faudrait peut-être threader ça, parce que si tu rends pas la main vite à
				// la réception d'un message je pense que le serveur va plus trop répondre
				logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXX Model received from "+socket.getLocalSocketAddress()+": loading...");
				ContainerRoot model = KevoreeXmiHelper.$instance.loadString(msg);
				updateLocalModel(model);
				logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXX Model loaded from XMI String");
			}
			
			@Override
			public void onError(WebSocket arg0, Exception arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onClose(WebSocket socket, int arg1, String arg2, boolean arg3) {
				// remove pointer from active connections on close
				activeConnections.remove(socket);
			}
		};
		startServer();
		logger.debug("XXXXXXXXXXXXXX WebSocket server started on port "+port);
	}

	@Stop
	public void stopWebSocketGroup() {
		stopServer();
	}
	
    @Update
    public void updateRestGroup() throws IOException {
    	logger.debug("XXXXXXXXXXX updateRestGroup");
        if (port != Integer.parseInt(this.getDictionary().get("port").toString())) {
            stopWebSocketGroup();
            startWebSocketGroup();
        }
    }

	@Override
	public ContainerRoot pull(String targetNodeName) throws Exception {
		logger.debug("pull("+targetNodeName+")");
		
        ContainerRoot model = getModelService().getLastModel();
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        int PORT = 8000;
        Group groupOption = model.findByPath("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            if (portOption.isDefined()) {
                try {
                    PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e){
                    logger.warn("Attribute \"port\" of {} must be an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
        }
        
        logger.debug("Trying to pull model to "+"ws://"+ip+":"+PORT+"/current/model");
        WebSocketClient client = new WebSocketClient(URI.create("ws://"+ip+":"+PORT+"/current/model")) {			
			@Override
			public void onMessage(String msg) {
				logger.debug("onMessage receive client");
				
			}
        	@Override
			public void onOpen(ServerHandshake arg0) {}
			@Override
			public void onError(Exception arg0) {}
			@Override
			public void onClose(int arg0, String arg1, boolean arg2) {}
		};
		client.connectBlocking();
		String stringifiedModel = KevoreeXmiHelper.$instance.saveToString(getModelService().getLastModel(), false);
		client.send(stringifiedModel);
		client.close();
		
		return model;
	}
	
	@Override
	public void triggerModelUpdate() {
		logger.debug("XXXXXXXXXXX trigger model update");
		if (isStarted) {
            final ContainerRoot modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
            if (modelOption != null) {
            	updateLocalModel(modelOption);
            }
            isStarted = false;
        } else {
            Group group = getModelElement();
            ContainerRoot currentModel = (ContainerRoot) group.eContainer();
            for (ContainerNode subNode : group.getSubNodes()) {
                if (!subNode.getName().equals(this.getNodeName())) {
                    try {
                        push(currentModel, subNode.getName());
                    } catch (Exception e) {
                        logger.warn("Unable to notify other members of {} group", group.getName());
                    }
                }
            }
        }
	}

	@Override
	public void push(ContainerRoot model, String targetNodeName) throws Exception {
		logger.debug("XXXXXXXXXXX trying to push");
		
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        KevoreeXmiHelper.$instance.saveStream(output, model);
        output.flush();
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        } else {
            logger.warn("No addr, found default local");
        }

        int PORT = 8000;
        Group groupOption = model.findByPath("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            if (portOption.isDefined()) {
                try {
                PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e){
                    logger.warn("Attribute \"port\" of {} must be an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
        }
        logger.debug("Trying to give model to "+"ws://"+ip+":"+PORT+"/current/model");
        WebSocketClient client = new WebSocketClient(URI.create("ws://"+ip+":"+PORT+"/current/model")) {			
			@Override
			public void onMessage(String msg) {
				logger.debug("onMessage receive client");
			}
        	
        	@Override
			public void onOpen(ServerHandshake arg0) {}
			@Override
			public void onError(Exception arg0) {}
			@Override
			public void onClose(int arg0, String arg1, boolean arg2) {}
		};
		client.connectBlocking();
		client.send(output.toString());
		client.close();
	}

    protected void updateLocalModel(final ContainerRoot model) {
    	logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXX local update model");
        new Thread() {
            public void run() {
                getModelService().unregisterModelListener(WebSocketGroup.this);
                getModelService().atomicUpdateModel(model);
                getModelService().registerModelListener(WebSocketGroup.this);
            }
        }.start();
    }
    
    private void startServer() {
    	if (server != null) {
    		server.start();
    		logger.debug("WebSocketServer started on "+server.getAddress().getHostName()+":"+server.getAddress().getPort());
    	}
    	isStarted = true;
    }
    
    private void stopServer() {
    	if (server != null) {
			try {
				server.stop();
			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
    	}
    	isStarted = false;
    }
}
