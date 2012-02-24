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
	private ExecutorService poolUpdate;

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
		String IP = KevoreePlatformHelper.getProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (IP.equals("")) {
			IP = "127.0.0.1";
		}

		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, this.getName(), "port", true, targetNodeName);
		int PORT = 8000;
		if (portOption.isDefined()) {
			PORT = portOption.get();
		}

		logger.debug("url=>" + "http://" + IP + ":" + PORT + "/model/current");

		if (!sendModel(model, "http://" + IP + ":" + PORT + "/model/current")) {
			logger.debug("Unable to push a model on " + targetNodeName);
		}
	}

	public void internalPush (ContainerRoot model, String targetNodeName, String sender) {
		String IP = KevoreePlatformHelper.getProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (IP.equals("")) {
			IP = "127.0.0.1";
		}

		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(model, this.getName(), "port", true, targetNodeName);
		int PORT = 8000;
		if (portOption.isDefined()) {
			PORT = portOption.get();
		}

		if (!sendModel(model, "http://" + IP + ":" + PORT + "/model/current?sender=" + sender)) {
			logger.debug("Unable to push a model on " + targetNodeName);
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

	public String getAddress (String remoteNodeName) {
		logger.debug("ModelService " + getModelService());

		String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	public int parsePortNumber (String nodeName) throws IOException {
		try {
			//logger.debug("look for port on " + nodeName);
			return KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentGroup(this.getModelService().getLastModel(), this.getName(), "port", nodeName);
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public ContainerRoot pull (String targetNodeName) {
		String localhost = "localhost";
		int port = 8000;

		try {
			localhost = getAddress(targetNodeName);
			port = parsePortNumber(targetNodeName);
		} catch (IOException e) {
			logger.error("Unable to getAddress or Port of " + targetNodeName, e);
		}

		logger.debug("Pulling model " + targetNodeName + " " + "http://" + localhost + ":" + port + "/model/current");
		try {
			URL url = new URL("http://" + localhost + ":" + port + "/model/current");
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(2000);
			InputStream inputStream = conn.getInputStream();
			return KevoreeXmiHelper.loadStream(inputStream);
		} catch (IOException e) {
			logger.error("error while pulling model for name " + targetNodeName, e);
		}
		return null;
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
