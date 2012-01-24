package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

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
public class KloudResourceManager extends AbstractPage {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request != null) {
			if (request.getResolvedParams().get("model") != null && request.getResolvedParams().get("login") != null
					&& request.getResolvedParams().get("password") != null) {
				// check authentication information
				if (InriaLdap.testLogin(request.getResolvedParams().get("login"),
						request.getResolvedParams().get("password"))) {

					String result = process(request.getResolvedParams().get("model"),
							request.getResolvedParams().get("login"));
					if (result.startsWith("http")) {
						response.setContent(HTMLHelper
								.generateValidSubmissionPageHtml(request.getUrl(),
										request.getResolvedParams().get("login"),
										""/*TODO specify an address*/));
					} else {
						response.setContent(HTMLHelper.generateUnvalidSubmissionPageHtml(request.getUrl(),
								request.getResolvedParams().get("login"), result));
					}
				} else {
					response.setContent(HTMLHelper.generateFailToLoginPageHtml(request.getUrl()));
				}
			} else {
				response.setContent(HTMLHelper.generateSimpleSubmissionFormHtml(request.getUrl()));
			}
		} else {
			response.setContent("Bad Request");
		}
		return response;
	}

	private String process (String modelStream, String login) {// try to get the user model
		ContainerRoot model = KevoreeXmiHelper.loadString(modelStream);
		// looking for current configuration to check if user has already submitted something
		if (KloudHelper.lookForAGroup(login, this.getModelService().getLastModel())) {

			// if the user has already submitted something, we return the access point to this previous configuration
			Option<String> accessPointOption = KloudHelper
					.lookForAccessPoint(login, this.getNodeName(), this.getModelService().getLastModel());
			if (accessPointOption.isDefined()) {
				return "A previous configuration has already submitted.<br/>Please use this access point to reconfigure it: "
						+ accessPointOption.get()
						+ "<br />This access point allow you to access to a Kevoree group that allows you to send a model to it."
						+ "<br />This model will be used to reconfigure your nodes and add or remove some of them if necessary.";
			} else {
				return "A previous configuration has already submitted but we are not able to find the corresponding access point.<br/>Please contact the administrator.";
			}
		} else {
			// else we create this new one
			UUIDModel uuidModel = this.getModelService().getLastUUIDModel();

			// we create a group with the login of the user
			Option<ContainerRoot> newKloudModelOption = KloudHelper
					.createGroup(login, this.getNodeName(), uuidModel.getModel());
			if (newKloudModelOption.isDefined()) {
				// create proxy to the group
				newKloudModelOption = KloudHelper.createProxy(login, this.getNodeName(), "/" + login,
						newKloudModelOption.get());

				if (newKloudModelOption.isDefined()) {

					KloudHelper.getGroup(login, newKloudModelOption.get());

					try {
						// update the kloud model by adding the group (the nodes are not added)
						this.getModelService().atomicCompareAndSwapModel(uuidModel, newKloudModelOption.get());

						// push the user model to this group
						KloudHelper.localPush(model, login);

						Option<String> accessPointOption = KloudHelper
								.lookForAccessPoint(login, this.getNodeName(), this.getModelService().getLastModel());
						if (accessPointOption.isDefined()) {
							return accessPointOption.get();
						} else {
							return "Unable to give you access to your nodes.<br/>Please contact the administrators.";
						}
					} catch (Exception e) {
						logger.debug(
								"Unable to swap model, maybe because the new model is based on a too old configuration",
								e);
						return "Unable to swap model, maybe because the new model is based on a too old configuration:\n"
								+ e.getMessage();
					}
				} else {
					logger.debug("Unable to add the proxy for the user {}", login);
					return "Unable to add the proxy for the user " + login;
				}
			} else {
				return "Unable to create the needed user group to give access the nodes to the user.";
			}
		}
	}

	/*private String processDeployment (String modelStream, String login) {
		// try to get the user model
		ContainerRoot model = KevoreeXmiHelper.loadString(modelStream);
		// check if the model is valid
		Option<String> result = KloudResourceProvider.check(model);
		if (result.isEmpty()) {
			// try to configure the model to be applied on the Kloud
			Option<ContainerRoot> cleanModelOption = KloudResourceProvider.cleanUserModel(model);
			if (cleanModelOption.isDefined()) {
				ContainerRoot cleanModel = cleanModelOption.get();
				UUIDModel uuidModel = this.getModelService().getLastUUIDModel();
				// try to distribute all user nodes on the Kloud
				Option<ContainerRoot> newGlobalModelOption = KloudResourceProvider
						.distribute(model, uuidModel.getModel());
				if (newGlobalModelOption.isDefined()) {
					ContainerRoot newGlobalModel = newGlobalModelOption.get();
					// push the user model to the Kloud
					boolean ok = KloudResourceProvider.update(uuidModel, newGlobalModel, this.getModelService());
					if (ok) {
						// add port forwarding to allow user to have access to their nodes
						ok = KloudResourceProvider.addProxy(newGlobalModel, cleanModel, this.getModelService(),
								this.getKevScriptEngineFactory(), this.getNodeName(), login);
						if (ok) {
							// send the user model to the user group to configure the software
							ok = KloudResourceProvider.updateUserConfiguration(cleanModel, model, this.getModelService());
							if (ok) {
								// TODO keep a pointer to the model that has been sent to the group which represent the access point of the user software
								return "";// return the http address to have an access to the configured nodes
							} else {
								return "";
							}
						} else {
							logger.error(
									"Model has been deployed but we are unable to configure the cloud to give you access to your nodes.");
							return "Model has been deployed but we are unable to configure the cloud to give you access to your nodes.";
						}
					} else {
						logger.error("Unable to update the system to deploy your software.");
						return "Unable to update the system to deploy your software.";
					}
				} else {
					logger.error("Unable to deploy your nodes on the Kloud.");
					return "Unable to deploy your nodes on the Kloud.";
				}
			} else {
				logger.error("Unable to apply KevScript to add a group that manage the overall software.");
				return "Unable to apply KevScript to add a group that manage your software.";
			}
		} else {
			return result.get();
		}
	}*/
}