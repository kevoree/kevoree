package org.kevoree.library.sky.provider;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeFragmentPropertyHelper;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.nanohttp.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/01/12
 * Time: 18:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "masterNode", optional = false)
})
public class KloudPaaSGroup extends AbstractGroupType {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContainerRoot userModel;

	private NanoHTTPD server = null;
	private KevoreeModelHandlerService handler = null;

	ExecutorService poolUpdate = Executors.newSingleThreadExecutor();

	@Start
	public void startRestGroup () throws IOException {
		poolUpdate = Executors.newSingleThreadExecutor();
		handler = this.getModelService();
		int port = Integer.parseInt(this.getDictionary().get("port").toString());
		server = new NanoHTTPD(port) {
			@Override
			public Response serve (String uri, String method, Properties header, Properties parms, Properties files, String body) {
				if (method.equals("POST")) {
//					try {

					final ContainerRoot model = KevoreeXmiHelper.loadString(body.trim());

					// looking if this instance is on top of a IaaS node or a PaaS node (PJavaSeNode)
					if (KloudHelper.isIaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						// if this instance is on top of IaaS node then we try to dispatch the received model on the kloud
						if (KloudReasoner.needsNewDeployment(model, userModel)) {
							logger.debug("A new Deployment must be done!");
							if (userModel == null) {
								userModel = KevoreeFactory.createContainerRoot();
							}
							Option<ContainerRoot> cleanedModelOption = KloudReasoner.processDeployment(model, userModel, getModelService(), getKevScriptEngineFactory(), getName());
							if (cleanedModelOption.isDefined()) {
								userModel = model;
								new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
							}
							new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
						} else {
							Option<ContainerRoot> cleanModelOption = KloudHelper.cleanUserModel(userModel);
							if (cleanModelOption.isDefined()) {
								logger.debug("An update will be done!");
								// there is no new node so we simply push model on each PaaSNode
								if (KloudReasoner.updateUserConfiguration(getName(), cleanModelOption.get(), model, getModelService(), getKevScriptEngineFactory())) {
									userModel = model;
									new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
								}
							}
							new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
						}
					} else if (KloudHelper.isPaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						// if this instance is on top of PaaS node then we deploy the model on the node
						getModelService().updateModel(model);
						new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
					} else {
						logger.debug("Unable to manage this kind of node as a Kloud node");
						new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
					}
				}
				if (method.equals("GET")) {
					String msg = "";
					if (KloudHelper.isIaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						msg = KevoreeXmiHelper.saveToString(handler.getLastModel(), false);

					} else if (KloudHelper.isPaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						// if this instance is on top of PaaS node then we deploy the model on the node
						msg = KevoreeXmiHelper.saveToString(getModelService().getLastModel(), false);
					} else {
						logger.debug("Unable to manage this kind of node as a Kloud node");
						msg = "Unable to manage this kind of node as a Kloud node";
					}
					return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
				}
				return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_XML, "ONLY GET OR POST METHOD SUPPORTED");
			}
		};

		logger.info("Rest service start on port ->" + port);
	}

	@Stop
	public void stopRestGroup () {
		poolUpdate.shutdownNow();
		server.stop();
	}

	@Override
	public void triggerModelUpdate () {

		if (userModel != null) {
			Group group = getModelElement();
			for (ContainerNode subNode : group.getSubNodesForJ()) {
				if (!subNode.getName().equals(this.getNodeName())) {
					push(getModelService().getLastModel(), subNode.getName());
				}
			}
		} else {
			if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
				// pull the model to the master node
				userModel = pull(selectMasterNode(this.getDictionary().get("masterNode").toString()));
				this.getModelService().atomicUpdateModel(userModel);
			}
		}


	}

	@Override
	public void push (ContainerRoot model, String targetNodeName) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			KevoreeXmiHelper.saveStream(outStream, model);
			outStream.flush();
			String IP = KevoreePlatformHelper.getProperty(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
			if (IP.equals("")) {
				IP = "127.0.0.1";
			}

			int PORT = KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentGroup(model, this.getName(), "port", targetNodeName);
			/*
				  System.out.println("port=>" + PORT);
				  System.out.println("GetName=> " + getName());
				  System.out.println("GetNodeName=> " + getNodeName());
	  */
			URL url = new URL("http://" + IP + ":" + PORT + "/model/current?nodesrc=" + getNodeName());
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

		} catch (Exception e) {
			//			e.printStackTrace();
			logger.debug("Unable to push a model on " + targetNodeName);

		}
	}

	public String getAddress (String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	public int parsePortNumber (String nodeName) throws IOException {
		try {
			//logger.debug("look for port on " + nodeName);
			return KevoreeFragmentPropertyHelper
					.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
							nodeName);
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


	/*public void startRestGroup () {
		super.startRestGroup();
	}*/

	@Override
	public boolean triggerPreUpdate (ContainerRoot currentModel, ContainerRoot futureModel) {
		if (userModel != null) {
			return super.triggerPreUpdate(currentModel, futureModel);
		} else {
			return true;
		}
	}

	/*@Override
	public void triggerModelUpdate () {
		if (userModel != null) {
			super.triggerModelUpdate();
		} else {
			if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
				// pull the model to the master node
				userModel = pull(selectMasterNode(this.getDictionary().get("masterNode").toString()));
				this.getModelService().atomicUpdateModel(userModel);
			}
		}
	}*/

	private String selectMasterNode (String masterNodeList) {
		return masterNodeList.split(",")[0];
	}

	/*@Override
	public void push (ContainerRoot containerRoot, String s) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			KevoreeXmiHelper.saveStream(outStream, containerRoot);
			outStream.flush();
			Option<String> publicURLOption = KevoreePropertyHelper.getStringPropertyForGroup(containerRoot, this.getName(), "publicURL", false, "");

			String publicURL;
			if (publicURLOption.isEmpty() || publicURLOption.get().equals("")) {
				publicURL = "127.0.0.1";
			} else {
				publicURL = publicURLOption.get();
			}
			if (!publicURL.startsWith("http://")) {
				publicURL = "http://" + publicURL;
			}

			URL url = new URL(publicURL);
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

		} catch (Exception e) {
			logger.error("Unable to push a model on {}", s, e);

		}
	}*/

	/*@Override
	public boolean updateModel (ContainerRoot model, String sender) {
		// looking if this instance is on top of a IaaS node or a PaaS node (PJavaSeNode)
		if (KloudHelper.isIaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of IaaS node then we try to dispatch the received model on the kloud
			if (KloudReasoner.needsNewDeployment(model, userModel)) {
				logger.debug("A new Deployment must be done!");
				if (userModel == null) {
					userModel = KevoreeFactory.createContainerRoot();
				}
				Option<ContainerRoot> cleanedModelOption = KloudReasoner.processDeployment(model, userModel, this.getModelService(), this.getKevScriptEngineFactory(), this.getName());
				if (cleanedModelOption.isDefined()) {
					userModel = model;
					return true;
				}
				return false;
			} else {
				Option<ContainerRoot> cleanModelOption = KloudHelper.cleanUserModel(userModel);
				if (cleanModelOption.isDefined()) {
					logger.debug("An update will be done!");
					// there is no new node so we simply push model on each PaaSNode
					if (KloudReasoner.updateUserConfiguration(this.getName(), cleanModelOption.get(), model, this.getModelService(), getKevScriptEngineFactory())) {
						userModel = model;
						return true;
					}
				}
				return false;
			}
		} else if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of PaaS node then we deploy the model on the node
			this.getModelService().updateModel(model);
			return true;
		} else {
			logger.debug("Unable to manage this kind of node as a Kloud node");
			return false;
		}
	}*/

	/*@Override
	public String getModel () {
		if (KloudHelper.isIaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			return KevoreeXmiHelper.saveToString(userModel, false);
		} else if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of PaaS node then we deploy the model on the node
			return KevoreeXmiHelper.saveToString(this.getModelService().getLastModel(), false);
		} else {
			logger.debug("Unable to manage this kind of node as a Kloud node");
			return "";
		}
	}*/
}
