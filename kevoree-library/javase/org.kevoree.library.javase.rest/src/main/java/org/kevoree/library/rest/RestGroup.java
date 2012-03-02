package org.kevoree.library.rest;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 18:27
 */


@DictionaryType({
		@DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true)
})
@GroupType
@Library(name = "JavaSE")
public class RestGroup extends AbstractGroupType {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ServerBootstrap server = new ServerBootstrap(this);
	protected ExecutorService poolUpdate;

	@Start
	public void startRestGroup () {

		poolUpdate = Executors.newSingleThreadExecutor();
		logger.warn("Rest service start on port " + this.getDictionary().get("port").toString());
		Object ipOption = this.getDictionary().get("ip");
		String ip = "0.0.0.0";
		if (ipOption != null) {
			ip = ipOption.toString();
		}
		server.startServer(Integer.parseInt(this.getDictionary().get("port").toString()), ip);

		//logger.info("!!! try to block => "+getModelService().getLastModel()+"->"+getModelService().getLastModification());
		NodeNetworkHelper.updateModelWithNetworkProperty(this);

	}

	@Stop
	public void stopRestGroup () {
		server.stop();
		poolUpdate.shutdownNow();
	}

	@Override
	public void triggerModelUpdate () {
		Group group = getModelElement();
		ContainerRoot model = this.getModelService().getLastModel();

		for (ContainerNode subNode : group.getSubNodesForJ()) {
			if (!subNode.getName().equals(this.getNodeName())) {
				internalPush(model, subNode.getName(), this.getNodeName());
			}
		}
	}

	@Update
	public void updateRestGroup () {
		stopRestGroup();
		startRestGroup();
	}

	@Override
	public void push (ContainerRoot model, String targetNodeName) {
		List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, this.getName(), "port", true, targetNodeName);
		int PORT = 8000;
		if (portOption.isDefined()) {
			PORT = portOption.get();
		}
		boolean sent = false;
		for (String ip : ips) {
			logger.debug("try to send model on url=>" + "http://" + ip + ":" + PORT + "/model/current");
			if (sendModel(model, "http://" + ip + ":" + PORT + "/model/current")) {
				sent = true;
				break;
			}
		}
		if (!sent) {
			logger.debug("try to send model on url=>" + "http://127.0.0.1:" + PORT + "/model/current");
			if (!sendModel(model, "http://127.0.0.1:" + PORT + "/model/current")) {
				logger.debug("Unable to push a model on " + targetNodeName);
			}
		}
	}

	public void internalPush (ContainerRoot model, String targetNodeName, String sender) {
		List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, this.getName(), "port", true, targetNodeName);
		int PORT = 8000;
		if (portOption.isDefined()) {
			PORT = portOption.get();
		}
		boolean sent = false;
		for (String ip : ips) {
			logger.debug("try to send model on url=>" + "http://" + ip + ":" + PORT + "/model/current?sender=" + sender);
			if (sendModel(model, "http://" + ip + ":" + PORT + "/model/current?sender=" + sender)) {
				sent = true;
				break;
			}
		}
		if (!sent) {
			logger.debug("try to send model on url=>" + "http://127.0.0.1:" + PORT + "/model/current?sender=" + sender);
			if (!sendModel(model, "http://127.0.0.1:" + PORT + "/model/current?sender=" + sender)) {
				logger.debug("Unable to push a model on " + targetNodeName);
			}
		}
	}

	private boolean sendModel (ContainerRoot model, String urlPath) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			KevoreeXmiHelper.saveStream(outStream, model);
			outStream.flush();
			URL url = new URL(urlPath);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3000);
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
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public ContainerRoot pull (String targetNodeName) {

		List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(this.getModelService().getLastModel(), targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(this.getModelService().getLastModel(), this.getName(), "port", true, targetNodeName);
		int PORT = 8000;
		if (portOption.isDefined()) {
			PORT = portOption.get();
		}
		for (String ip : ips) {
			logger.debug("try to pull model on url=>" + "http://" + ip + ":" + PORT + "/model/current");
			ContainerRoot model = pullModel("http://" + ip + ":" + PORT + "/model/current");
			if (model != null) {
				return model;
			}
		}
		ContainerRoot model = pullModel("http://127.0.0.1:" + PORT + "/model/current");
		if (model == null) {
			logger.debug("Unable to pull a model on " + targetNodeName);
			return null;
		} else {
			return model;
		}
	}

	private ContainerRoot pullModel (String urlPath) {
		try {
			URL url = new URL(urlPath);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(2000);
			InputStream inputStream = conn.getInputStream();
			return KevoreeXmiHelper.loadStream(inputStream);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * <b>This method must only be use by RootService</b>
	 *
	 * @param model  the new model to apply on the node
	 * @param sender the sender name of the model (maybe an empty string if the sender is not a node of the group
	 * @return the result may depend of the implementation. Basic implementation in RestGroup always returns <code>true</code>
	 */
	public boolean updateModel (final ContainerRoot model, final String sender) {
		Runnable t = new Runnable() {
			@Override
			public void run () {
				boolean externalSender = true;
				if (!sender.equals("")) {
					for (ContainerNode n : RestGroup.this.getModelElement().getSubNodesForJ()) {
						if (n.getName().equals(sender)) {
							externalSender = false;
						}
					}
				}
				if (!externalSender) {
					RestGroup.this.getModelService().unregisterModelListener(RestGroup.this.getModelListener());
				}
				RestGroup.this.getModelService().atomicUpdateModel(model);
				if (!externalSender) {
					RestGroup.this.getModelService().registerModelListener(RestGroup.this.getModelListener());
				}
			}
		};
		poolUpdate.submit(t);
		logger.debug("Rest Group updateModel");
		return true;
	}

	public String getModel () {
		return KevoreeXmiHelper.saveToString(this.getModelService().getLastModel(), false);
	}

	public RootService getRootService (String id) {
		return new RootService(id, this);
	}
}
