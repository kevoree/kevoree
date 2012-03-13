package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.nanohttp.NanoHTTPD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
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
		@DictionaryAttribute(name = "SSH_Public_Key", optional = true),
		@DictionaryAttribute(name = "displayIP", vals = {"true", "false"}, optional = false)
})
public class KloudPaaSNanoGroup extends AbstractGroupType {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContainerRoot userModel;

	private NanoHTTPD server = null;

	private int port;

	private ExecutorService poolUpdate = Executors.newSingleThreadExecutor();

	@Start
	public void startRestGroup () throws IOException {
		poolUpdate = Executors.newSingleThreadExecutor();
		port = Integer.parseInt(this.getDictionary().get("port").toString());
		String address = this.getDictionary().get("ip").toString();
		logger.info("starting HTTP server on {}:{}", address, port);

		server = new NanoHTTPD(new InetSocketAddress(InetAddress.getByName(address), port)) {

			public Response serve (String uri, String method, Properties header, Properties parms, Properties files, InputStream body) {
				if (method.equals("POST")) {
//					String srcNodeName = "";
					Boolean externalSender = true;
					Enumeration e = parms.propertyNames();
					while (e.hasMoreElements()) {
						String value = (String) e.nextElement();
						if (value.endsWith("nodesrc")) {
//							srcNodeName = parms.getProperty(value);
							externalSender = false;
							// we presume that if nodesrc is define so a node have set it and maybe the master node but as the master node doesn't appear on this local model, we cannot check the src
						}
					}

					ContainerRoot model = KevoreeXmiHelper.loadStream(body);

					// looking if this instance is on top of a IaaS node or a PaaS node (PJavaSeNode)
					if (KloudHelper.isIaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						// if this instance is on top of IaaS node then we try to dispatch the received model on the kloud
						if (KloudReasoner.needsNewDeployment(model, userModel)) {
							logger.debug("A new Deployment must be done!");
							if (userModel == null) {
								userModel = KevoreeFactory.createContainerRoot();
							}
							ContainerRoot oldModel = userModel;
							userModel = model;
							boolean deploymentOK = KloudReasoner.processDeployment(userModel, oldModel, getModelService(), getKevScriptEngineFactory(), getName());
							if (deploymentOK) {
								// update user node that are already started
								KloudReasoner.sendUserConfiguration(getName(), userModel, getModelService(), getKevScriptEngineFactory(), getNodeName());
								return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
							}
							return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
						} else {
							Option<ContainerRoot> cleanModelOption = KloudHelper.cleanUserModel(userModel);
							if (cleanModelOption.isDefined()) {
								logger.debug("An update will be done!");
								// there is no new node so we simply push model on each PaaSNode
								KloudReasoner.sendUserConfiguration(getName(), model, getModelService(), getKevScriptEngineFactory(), getNodeName());
								userModel = model;
								return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
							}
							new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
						}
					} else if (KloudHelper.isPaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
						// if this instance is on top of PaaS node then we deploy the model on the node
						getModelService().updateModel(model);
						if (externalSender) {
							logger.debug("Forward model to master node");
							// forward model to masterNode
							KloudHelper.pushOnMaster(model, getName(), getDictionary().get("masterNode").toString() + "?nodesrc=" + getNodeName());
						}
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
					} else {
						logger.debug("Unable to manage this kind of node as a Kloud node");
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<nack nodeName=\"" + getNodeName() + "\" />");
					}
				} else if (method.equals("GET")) {
					if (uri.endsWith("/model/current")) {
						String msg;
						if (KloudHelper.isIaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
							Option<ContainerRoot> userModelOption = KloudReasoner.updateUserConfiguration(getName(), userModel, getModelService(), getKevScriptEngineFactory());
							if (userModelOption.isDefined()) {
								msg = KevoreeXmiHelper.saveToString(userModelOption.get(), false);
							} else {
								logger.debug("Unable to find a valid user model to send it");
								msg = "Unable to find a valid user model to send it";
							}
						} else if (KloudHelper.isPaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
							// if this instance is on top of PaaS node then we deploy the model on the node
							msg = KevoreeXmiHelper.saveToString(getModelService().getLastModel(), false);
						} else {
							logger.debug("Unable to manage this kind of node as a Kloud node");
							msg = "Unable to manage this kind of node as a Kloud node";
						}
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
					} else if (uri.endsWith("/release")) {
						if (KloudHelper.isIaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
							KloudReasoner.processDeployment(KevoreeFactory.createContainerRoot(), userModel, getModelService(), getKevScriptEngineFactory(), getName());
							return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "");
						}
					}
				}
				return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_XML, "ONLY GET OR POST METHOD SUPPORTED");
			}
		};

		if (KloudHelper.isPaaSNode(getModelService().getLastModel(), getName(), getNodeName())) {
			// configure ssh authorized keys for user nodes
			Object sshKeyObject = this.getDictionary().get("SSH_Public_Key");
			if (sshKeyObject != null) {
				// build directory if necessary
				File f = new File(System.getProperty("user.home") + File.separator + ".ssh");

				if ((f.exists() && f.isDirectory()) || (!f.exists() && f.mkdirs())) {
					// copy key
					addStringToFile(sshKeyObject.toString(), System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "authorized_keys");
				}
			}
		}
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
		if (userModel == null) {
			if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
				// pull the model to the master node
				userModel = pullModel(this.getDictionary().get("masterNode").toString());
				if (userModel != null) {
					logger.debug("Try to apply a new model on PaaSNode coming from {}", this.getDictionary().get("masterNode").toString());
					this.getModelService().atomicUpdateModel(userModel);
				} else {
					logger.warn("Unable to get user model on master node");
				}
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
		String param = "";
		if (!getNodeName().equals("")) {
			param = "?nodesrc=" + getNodeName();
		}
		boolean sent = false;
		for (String ip : ips) {
			logger.debug("try to send model on url=>" + "http://" + ip + ":" + PORT + "/model/current" + param);
			if (sendModel(model, "http://" + ip + ":" + PORT + "/model/current" + param)) {
				sent = true;
				break;
			}
		}
		if (!sent) {
			logger.debug("try to send model on url=>" + "http://127.0.0.1:" + PORT + "/model/current" + param);
			if (!sendModel(model, "http://127.0.0.1:" + PORT + "/model/current" + param)) {
				logger.debug("Unable to push a model on {}", targetNodeName);
				logger.debug("try to send model using master node property");
				if (!sendModel(model, this.getDictionary().get("masterNode").toString() + param)) {
					logger.debug("Unable to send model using master node property");
				}
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
		String param = "";
		if (getNodeName().equals("")) {
			param = "?nodesrc=" + getNodeName();
		}

		for (String ip : ips) {
			logger.debug("try to pull model on url=>" + "http://" + ip + ":" + PORT + "/model/current" + param);
			ContainerRoot model = pullModel("http://" + ip + ":" + PORT + "/model/current" + param);
			if (model != null) {
				return model;
			}
		}
		ContainerRoot model = pullModel("http://127.0.0.1:" + PORT + "/model/current" + param);
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

	private void addStringToFile (String data, String outputFile) {
		try {
			logger.debug("trying to add \"{}\" into {}", data, outputFile);
			StringBuilder stringBuilder = new StringBuilder();
			if (new File(outputFile).exists()) {
				InputStream reader = new DataInputStream(new FileInputStream(new File(outputFile)));
				ByteArrayOutputStream writer = new ByteArrayOutputStream();

				byte[] bytes = new byte[2048];
				int length = reader.read(bytes);
				while (length != -1) {
					writer.write(bytes, 0, length);
					length = reader.read(bytes);

				}
				writer.flush();
				writer.close();
				reader.close();
				stringBuilder.append(new String(writer.toByteArray(), "UTF-8"));
			}
			boolean added = false;
			if (!stringBuilder.toString().contains(data)) {
				stringBuilder.append(data).append("\n");
				added = true;
			}

			if (added && copyStringToFile(stringBuilder.toString(), outputFile)) {
				logger.debug("add \"{}\" into {} is done", data, outputFile);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean copyStringToFile (String data, String outputFile) {
		logger.debug("trying to copy \"{}\" to {}", data, outputFile);
		try {
			if (new File(outputFile).exists()) {
				new File(outputFile).delete();
			}
			File f = new File(outputFile);
			if (f.createNewFile()) {
				OutputStream writer = new DataOutputStream(new FileOutputStream(f));

				writer.write(data.getBytes());
				writer.flush();
				writer.close();
				logger.debug("copy \"{}\" into {} is done", data, outputFile);
				return true;
			} else {
				return false;
			}
		} /*catch {
	      case _@e => logger.error("Unable to copy \"{}\" on {}", Array[AnyRef](data, outputFile), e);
	    }*/ catch (FileNotFoundException e) {
			logger.debug("Unable to copy \"{}\" on {}", data, outputFile);
			return false;
		} catch (IOException e) {
			logger.debug("Unable to copy \"{}\" on {}", data, outputFile);
			return false;
		}
	}
}