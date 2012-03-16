package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.*;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.javase.authentication.Authentication;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/01/12
 * Time: 10:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentType
@Requires({
		@RequiredPort(name = "deploy", type = PortType.MESSAGE, optional = false), // TODO define a message type
		@RequiredPort(name = "release", type = PortType.MESSAGE, optional = false), // TODO define a message type
		@RequiredPort(name = "authentication", type = PortType.SERVICE, className = Authentication.class, optional = true)
})
public class PaaSKloudResourceManagerPage extends ParentAbstractPage {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request != null) {
			if (request.getResolvedParams().get("login") != null && request.getResolvedParams().get("password") != null) {
				// check authentication information
				boolean isAuthenticate = true;
				if (isPortBinded("authentication")) {
					isAuthenticate = getPortByName("authentication", Authentication.class).authenticate(request.getResolvedParams().get("login"), request.getResolvedParams().get("password"));
				}
				if (isAuthenticate) {
					if (request.getResolvedParams().get("action") != null) {
						if (request.getResolvedParams().get("action").equals("release")) {
							response = processRelease(request, response);
						} else if (request.getResolvedParams().get("action").equals("model")) {
							response = processModel(request, response);
						}
					} else {
						logger.debug("Unable to process {}", request.getUrl());
						response.setContent("<nack error=\"unknown uri\" />");
					}
				} else {
					response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"Authentication failure\" />");
				}
			}
		} else {
			logger.debug("Request seems to be null: {}", request);
		}
		logger.debug("sending response");
		return response;
	}

	private KevoreeHttpResponse processRelease (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.debug("Try to release {} configuration", request.getResolvedParams().get("login"));
		StdKevoreeMessage message = new StdKevoreeMessage();
		message.putValue("login", request.getResolvedParams().get("login"));
		getPortByName("release", MessagePort.class).process(message);
		response.setContent("<ack login=\"" + request.getResolvedParams().get("login") + "\" />");

		return response;
	}

	private KevoreeHttpResponse processModel (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request.getResolvedParams().get("model") != null) {
			ContainerRoot model = KevoreeXmiHelper.loadString(request.getResolvedParams().get("model"));
			// forward model to group if it exist or submit a new model
			Option<String> masterNodeOption = KevoreePropertyHelper
					.getStringPropertyForGroup(getModelService().getLastModel(), request.getResolvedParams().get("login"), "masterNode", false, "");
			if (masterNodeOption.isDefined()) {
				List<String> accessPoints = KloudHelper.getMasterIP_PORT(masterNodeOption.get());
				if (accessPoints.size() > 0) {
					for (String ipPort : KloudHelper.getMasterIP_PORT(getDictionary().get("masterNode").toString())) {
						if (KloudHelper.sendModel(model, "http://" + ipPort + "/model/current")) {
							ContainerRoot newModel = KloudHelper.pullModel("http://" + ipPort + "/model/current");
							if (newModel != null) {
								response.setContent(KevoreeXmiHelper.saveToString(newModel, false));
							}
							break;
						} else {
							response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"Unable to send model to the group\" />");
						}
					}
				} else {
					response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"Unable to send model to the group\" />");
				}

			} else {
				response.setContent(deploy(request.getResolvedParams().get("login"), request.getResolvedParams().get("ssh_key"), model));
			}
		} else {
			response.setContent(findModel(request.getResolvedParams().get("login"), request.getResolvedParams().get("ssh_key")));
		}
		return response;
	}

	private String findModel (String login, String sshKey) { // Here we may had the proxy page
		logger.info("Try to find model for {}", login);

		Option<String> masterNodeOption = KevoreePropertyHelper.getStringPropertyForGroup(getModelService().getLastModel(), login, "masterNode", false, "");
		if (masterNodeOption.isDefined()) {
			List<String> accessPoints = KloudHelper.getMasterIP_PORT(masterNodeOption.get());
			if (accessPoints.size() > 0) {
				for (String ipPort : KloudHelper.getMasterIP_PORT(getDictionary().get("masterNode").toString())) {
					ContainerRoot model = KloudHelper.pullModel("http://" + ipPort + "/model/current");
					if (model != null) {
						return KevoreeXmiHelper.saveToString(model, false);
					}
				}
				return "<nack login=\"" + login + "\" error=\"No model found\"";
			} else {
				return deploy(login, sshKey, KevoreeFactory.createContainerRoot());
			}
		} else {
			return "<nack login=\"" + login + "\" error=\"Unable to send model to the group\"";
		}
	}

	private String deploy (String login, String sshKey, ContainerRoot model) {
		StdKevoreeMessage message = new StdKevoreeMessage();
		message.putValue("login", login);
		message.putValue("model", KevoreeXmiHelper.saveToString(model, false));
		if (sshKey != null && !sshKey.equals((""))) {
			message.putValue("sshKey", sshKey);
		}
		getPortByName("deploy", MessagePort.class).process(message);
		return "<wait login=\"" + login + "\" />";
	}

	/*private boolean createProxy (String login, int nbTry) {
						 UUIDModel uuidModel = this.getModelService().getLastUUIDModel();
						 Option<ContainerRoot> kloudModelOption = KloudReasoner.createProxy(login, this.getNodeName(), "/" + login, uuidModel.getModel(), getKevScriptEngineFactory());
						 if (kloudModelOption.isDefined()) {
						 }
						 try {
							 this.getModelService().atomicCompareAndSwapModel(uuidModel, kloudModelOption.get());
							 return true;
						 } catch (Exception ignored) {
						 }
						 return nbTry > 0 && createProxy(login, nbTry - 1);
					 }*/
}