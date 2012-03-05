package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.nanohttp.NodeNetworkHelper;
import org.kevoree.library.nanohttp.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
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
		@DictionaryAttribute(name = "masterNode", optional = false),
		@DictionaryAttribute(name = "port", optional = false, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "SSH_Public_Key", optional = true)
})
public class KloudPaaSNanoGroup extends AbstractGroupType {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContainerRoot userModel;

	private NanoHTTPD server = null;

	private int port;
	private boolean first = true;

	private ExecutorService poolUpdate = Executors.newSingleThreadExecutor();

	@Start
	public void startRestGroup () throws IOException {
		poolUpdate = Executors.newSingleThreadExecutor();
		port = Integer.parseInt(this.getDictionary().get("port").toString());
		String address = this.getDictionary().get("ip").toString();
		server = new NanoHTTPD(new InetSocketAddress(InetAddress.getByName(address), port)) {

			public Response serve (String uri, String method, Properties header, Properties parms, Properties files, InputStream body) {
				if (method.equals("POST")) {
					final ContainerRoot model = KevoreeXmiHelper.loadStream(body);

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
								return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
							}
							return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
						} else {
							Option<ContainerRoot> cleanModelOption = KloudHelper.cleanUserModel(userModel);
							if (cleanModelOption.isDefined()) {
								logger.debug("An update will be done!");
								// there is no new node so we simply push model on each PaaSNode
								if (KloudReasoner.sendUserConfiguration(getName(), cleanModelOption.get(), model, getModelService(), getKevScriptEngineFactory())) {
									userModel = model;
									return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
								}
							}
							new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
						}
					} else if (KloudHelper.isPaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						// if this instance is on top of PaaS node then we deploy the model on the node
						logger.debug("get new model from /model/current on {}", KloudPaaSNanoGroup.this.getName());
						getModelService().updateModel(model);
						// forward model to masterNode
						pushInternals(model, selectMasterNode(getDictionary().get("masterNode").toString()));
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
					} else {
						logger.debug("Unable to manage this kind of node as a Kloud node");
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
					}
				} else if (method.equals("GET")) {
					String msg;
					if (KloudHelper.isIaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						Option<ContainerRoot> userModelOption = KloudReasoner.updateUserConfiguration(getName(), userModel, getModelService(), getKevScriptEngineFactory());
						if (userModelOption.isDefined()) {/*
							userModelOption = KloudReasoner.setAccesPointConfiguration(getName(), userModel, getModelService(), getKevScriptEngineFactory());
							if (userModelOption.isDefined()) {*/
							msg = KevoreeXmiHelper.saveToString(userModelOption.get(), false);
						} else {
							logger.debug("Unable to find a valid user model to send it");
							msg = "Unable to find a valid user model to send it";
						}
						/*} else {
							logger.debug("Unable to find a valid user model to send it due to unable access point configuration");
							msg = "Unable to find a valid user model to send it due to unable access point configuration";
						}*/
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

	@Update
	public void update () throws IOException {
		if (!this.getDictionary().get("port").toString().equals("" + port)) {
			stopRestGroup();
			startRestGroup();
		}
	}

	@Override
	public void triggerModelUpdate () {
		if (first) {
			first = false;
			NodeNetworkHelper.updateModelWithNetworkProperty(this);
		} else if (userModel == null) {
			if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
				// pull the model to the master node
				userModel = pull(selectMasterNode(this.getDictionary().get("masterNode").toString()));
				logger.debug("Try to apply a new model on PaaSNode");
				this.getModelService().atomicUpdateModel(userModel);
			}
		}
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

	private void pushInternals (ContainerRoot model, String targetNodeName) {
		List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(this.getModelService().getLastModel(), targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForGroup(this.getModelService().getLastModel(), this.getName(), "port", true, targetNodeName);
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

	private String selectMasterNode (String masterNodeList) {
		return masterNodeList.split(",")[0];
	}

}