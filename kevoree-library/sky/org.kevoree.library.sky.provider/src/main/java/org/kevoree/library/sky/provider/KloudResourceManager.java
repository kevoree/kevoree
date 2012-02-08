package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
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
public class KloudResourceManager extends ParentAbstractPage {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void requestHandler (Object param) {
		final Object params = param;
		new Thread() {
			@Override
			public void run () {
				KloudResourceManager.super.requestHandler(params);
			}
		}.start();
	}

	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request != null) {
			if (request.getResolvedParams().get("model") != null && request.getResolvedParams().get("login") != null
					&& request.getResolvedParams().get("password") != null && request.getResolvedParams().get("ssh_key") != null) {
				// check authentication information
				if (InriaLdap.testLogin(request.getResolvedParams().get("login"), request.getResolvedParams().get("password"))) {

					String result = process(request.getResolvedParams().get("model"), request.getResolvedParams().get("login"), request.getResolvedParams().get("ssh_key"));
					if (result.startsWith("http")) {
						response.setContent(HTMLHelper.generateValidSubmissionPageHtml(request.getUrl(), request.getResolvedParams().get("login"), result));
					} else {
						response.setContent(HTMLHelper.generateUnvalidSubmissionPageHtml(request.getUrl(), request.getResolvedParams().get("login"), result));
					}
				} else {
					response.setContent(HTMLHelper.generateFailToLoginPageHtml(request.getUrl()));
				}
			} else {
				response.setContent(HTMLHelper.generateSimpleSubmissionFormHtml(request.getUrl(), this.getDictionary().get("urlpattern").toString()));
			}
		} else {
			response.setContent("Bad Request");
		}
		return response;
	}

	private String process (String modelStream, String login, String sshKey) {// try to get the user model
		ContainerRoot model = KevoreeXmiHelper.loadString(modelStream);
		// looking for current configuration to check if user has already submitted something
		if (KloudHelper.lookForAGroup(login, this.getModelService().getLastModel())) {

			// if the user has already submitted something, we return the access point to this previous configuration
			Option<String> accessPointOption = KloudHelper.lookForAccessPoint(login, this.getNodeName(), this.getModelService().getLastModel());
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
			Option<ContainerRoot> newKloudModelOption = KloudHelper.createGroup(login, this.getNodeName(), uuidModel.getModel(), getKevScriptEngineFactory(), sshKey);
			if (newKloudModelOption.isDefined()) {
				// create proxy to the group
				newKloudModelOption = KloudHelper.createProxy(login, this.getNodeName(), "/" + login, newKloudModelOption.get(), getKevScriptEngineFactory());

				if (newKloudModelOption.isDefined()) {

					Option<Group> groupOption = KloudHelper.getGroup(login, newKloudModelOption.get());
					if (groupOption.isDefined()) {
						try {
							// update the kloud model by adding the group (the nodes are not added)
							this.getModelService().atomicCompareAndSwapModel(uuidModel, newKloudModelOption.get());

							// push the user model to this group
							KloudHelper.localPush(model, login, newKloudModelOption.get());

							Option<String> accessPointOption = KloudHelper.lookForAccessPoint(login, this.getNodeName(), this.getModelService().getLastModel());
							if (accessPointOption.isDefined()) {
								return accessPointOption.get();
							} else {
								return "Unable to give you access to your nodes.<br />Please contact the administrators.";
							}
						} catch (Exception e) {
							logger.debug(
									"Unable to swap model, maybe because the new model is based on a too old configuration",
									e);
							return "Unable to swap model, maybe because the new model is based on a too old configuration:\n"
									+ e.getMessage();
						}
					} else {
						// TODO
						return "";
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
}