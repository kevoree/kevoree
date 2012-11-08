/*
package org.kevoree.library.sky.provider.web;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.javase.authentication.Authentication;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.kevoree.library.sky.provider.api.PaaSManagerService;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.kevoree.library.sky.api.checker.RootKloudChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

*/
/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/01/12
 * Time: 10:18
 *
 * @author Erwan Daubert
 * @version 1.0
 *//*

@Library(name = "SKY")
@ComponentType
@MessageTypes({
		@MessageType(name = "kloud_request", elems =
				{@MsgElem(name = "login", className = String.class, optional = false),
						@MsgElem(name = "password", className = String.class, optional = true),
						@MsgElem(name = "action", className = String.class, optional = true),
						@MsgElem(name = "ssh_key", className = String.class, optional = true),
						@MsgElem(name = "model", className = String.class, optional = true)}
		)
})
@Requires({
		@RequiredPort(name = "delegate", type = PortType.SERVICE, optional = false, className = PaaSManagerService.class),
//		@RequiredPort(name = "release", type = PortType.MESSAGE, optional = false, messageType = "kloud_request"),
		@RequiredPort(name = "authentication", type = PortType.SERVICE, className = Authentication.class, optional = true)
})
@DictionaryType({
		@DictionaryAttribute(name = "checkModel", defaultValue = "true", vals = {"true", "false"}),
		@DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/kloud/{login}/{action}")
})
public class PaaSKloudResourceManagerRest extends ParentAbstractPage implements PaaSManagerService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private RootKloudChecker rootKloudChecker = new RootKloudChecker();

	*/
/*public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request != null) {
			if (request.getResolvedParams().get("login") != null) {
				if (checkAuthentication(request)) {
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
					logger.debug("Unable to process {} due to authentication failure for {}", request.getUrl(), request.getResolvedParams().get("login"));
					response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"Authentication failure\" />");
				}
			} else {
				logger.debug("Unable to process {} due to missing login parameter", request.getUrl());
				response.setContent("<nack error=\"login not available\" />");
			}
		} else {
			logger.debug("Request seems to be null: {}", request);
			response.setContent("<nack error=\"Null :-(\" />");
		}
		logger.debug("sending response");
		return response;
	}

	private boolean checkAuthentication (KevoreeHttpRequest request) {
		// check authentication information
		boolean isAuthenticate = true;
		if (isPortBinded("authentication")) {
			isAuthenticate = request.getResolvedParams().get("password") != null && getPortByName("authentication", Authentication.class)
					.authenticate(request.getResolvedParams().get("login"), request.getResolvedParams().get("password"));
		}
		return isAuthenticate;
	}

	private KevoreeHttpResponse processRelease (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		logger.debug("Try to release {} configuration", request.getResolvedParams().get("login"));
		StdKevoreeMessage message = new StdKevoreeMessage();
		message.putValue("login", request.getResolvedParams().get("login"));
		try {
//			getPortByName("release", HostService.class).release(request.getResolvedParams().get("login"));
			release(request.getResolvedParams().get("login"));
			response.setContent("<ack login=\"" + request.getResolvedParams().get("login") + "\" />");
		} catch (SubmissionException e) {
			response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"" + e.getMessage() + "\" />");
		}

		return response;
	}

	private KevoreeHttpResponse processModel (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request.getResolvedParams().get("model") != null) {
			logger.debug("A model has been received");
			ContainerRoot model = KevoreeXmiHelper.loadString(request.getResolvedParams().get("model"));
			// TODO ensure there is no IaaS node with the same name than a user node => maybe by using Listener to push the model on the IaaS
			// and then the IaaS check the nodeNames, if they are OK, the IaaSResourceManager refuse the model but push a new one with resource allocation
			if (checkModel(model, request.getResolvedParams().get("login"), response)) {
				// forward model to group if it exist or submit a new model
				Option<String> masterNodeOption = KevoreePropertyHelper
						.getStringPropertyForGroup(getModelService().getLastModel(), request.getResolvedParams().get("login"), "masterNode", false, "");
				if (masterNodeOption.isDefined()) {
					response = forwardModelToUserGroup(request, response, masterNodeOption.get(), model);
				} else {
					logger.debug("Unable to get a existing configuration for the user {}. We try to create a new configuration for this user.", request.getResolvedParams().get("login"));
					response = deploy(request, response, *//*
*/
/*request.getResolvedParams().get("login"), request.getResolvedParams().get("ssh_key"), *//*
*/
/*model);
//					response.setContent(deploy(request.getResolvedParams().get("login"), request.getResolvedParams().get("ssh_key"), model));
				}
			}
		} else {
			logger.debug("No model has been received, looking for an already submitted one or create a empty one");
			response = findModel(request, response);
		}
		return response;
	}

	private KevoreeHttpResponse forwardModelToUserGroup (KevoreeHttpRequest request, KevoreeHttpResponse response, String masterNode, ContainerRoot model) {
		List<String> accessPoints = KloudProviderHelper.getMasterIP_PORT(masterNode);
		if (accessPoints.size() > 0) {
			for (String ipPort : accessPoints) {
				if (KloudProviderHelper.sendModel(model, "http://" + ipPort + "/model/current")) {
					ContainerRoot newModel = KloudProviderHelper.pullModel("http://" + ipPort + "/model/current");
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
		return response;
	}

	private boolean checkModel (ContainerRoot model, String login, KevoreeHttpResponse response) {
		logger.debug("starting to check Cloud user model");
		rootKloudChecker.setLogin(login);
		List<CheckerViolation> violations = rootKloudChecker.check(model);
		if (violations.size() > 0) {
			StringBuilder errorBuilder = new StringBuilder();
			for (CheckerViolation violation : violations) {
				logger.debug("{}", violation.getMessage());
				errorBuilder.append(violation.getMessage()).append("\n");
			}
			logger.debug("Unable to deploy model because it doesn't respect the rule define on Kloud: {} violations", errorBuilder.toString());
			response.setContent("<nack login=\"" + login + "\" error=\"" + errorBuilder.toString() + "\" />");
			return false;
		} else {
			return true;
		}
	}

	private KevoreeHttpResponse findModel (KevoreeHttpRequest request, KevoreeHttpResponse response) { // Here we may had the proxy page
		String login = request.getResolvedParams().get("login");
//		String sshKey = request.getResolvedParams().get("ssh_key");
		logger.info("Try to find model for {}", login);
		Option<String> masterNodeOption = KevoreePropertyHelper.getStringPropertyForGroup(getModelService().getLastModel(), login, "masterNode", false, "");
		if (masterNodeOption.isDefined()) {
			List<String> accessPoints = KloudProviderHelper.getMasterIP_PORT(masterNodeOption.get());
			if (accessPoints.size() > 0) {
				for (String ipPort : accessPoints) {
					ContainerRoot model = KloudProviderHelper.pullModel("http://" + ipPort + "/model/current");
					if (model != null) {
						response.setContent(KevoreeXmiHelper.saveToString(model, false));
						return response;
					}
				}
				response.setContent("<nack login=\"" + login + "\" error=\"No model found\"");
				return response;
			} else {
				return deploy(request, response*//*
*/
/*login, sshKey*//*
*/
/*, KevoreeFactory.createContainerRoot());
			}
		} else {
			return deploy(request, response*//*
*/
/*login, sshKey*//*
*/
/*, KevoreeFactory.createContainerRoot());
//			return "<nack login=\"" + login + "\" error=\"Unable to send model to the group\"";
		}
	}

	private KevoreeHttpResponse deploy (KevoreeHttpRequest request, KevoreeHttpResponse response, *//*
*/
/*String login, String sshKey,*//*
*/
/* ContainerRoot model) {
//		StdKevoreeMessage message = new StdKevoreeMessage();
		String login = request.getResolvedParams().get("login");
		String sshKey = request.getResolvedParams().get("ssh_key");
//		message.putValue("login", login);
//		message.putValue("model", KevoreeXmiHelper.saveToString(model, false));
//		if (sshKey != null && !sshKey.equals((""))) {
//			message.putValue("sshKey", sshKey);
//		}
//		getPortByName("deploy", MessagePort.class).process(message);
		try {

			if (deploy(login, model, sshKey)) {
				response = findModel(request, response);
//				response.setContent("<ack login=\"" + request.getResolvedParams().get("login") + "\" />");
			} else {
				response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"Unknown ! Please contact the administrator\"/>");
			}
		} catch (SubmissionException e) {
			response.setContent("<nack login=\"" + request.getResolvedParams().get("login") + "\" error=\"" + e.getMessage() + "\" />");
		}
//		response.setContent("<wait login=\"" + login + "\" />");
		return response;
	}

	*//*
*/
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
					 }*//*
*/
/*

	@Override
	public boolean deploy (String login, ContainerRoot model, String sshKey) throws SubmissionException {
		return getPortByName("submit", HostService.class).deploy(login, model, sshKey);
	}

	@Override
	public boolean release (String login) throws SubmissionException {
		return getPortByName("submit", HostService.class).release(login);
	}*//*


	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		return null;
	}

	@Override
	public boolean initialize (String id, ContainerRoot model) throws SubmissionException {
		return false;
	}

	@Override
	public boolean add (String id, ContainerRoot model) throws SubmissionException {
		return false;
	}

	@Override
	public boolean remove (String id, ContainerRoot model) throws SubmissionException {
		return false;
	}

	@Override
	public boolean merge (String id, ContainerRoot model) throws SubmissionException {
		return false;
	}

	@Override
	public boolean release (String id) throws SubmissionException {
		return false;
	}
}*/
