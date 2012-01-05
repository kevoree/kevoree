package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@DictionaryType({
		@DictionaryAttribute(name = "EndPoint", defaultValue = "http://kloud.kevoree.org", optional = false)
})
public class KloudResourceManager extends AbstractPage {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request != null) {
			if (request.getResolvedParams().get("model") != null && request.getResolvedParams().get("login") != null
					&& request.getResolvedParams().get("password") != null) {
				// TODO check authentication information

				String result = processDeployment(request.getResolvedParams().get("model"),
						request.getResolvedParams().get("login"));
				if (result.startsWith("http")) {
					response.setContent(HTMLHelper
							.generateValidSubmissionPageHtml(request.getUrl(), request.getResolvedParams().get("login"),
									""/*TODO specify an address*/));
				} else {
					response.setContent(HTMLHelper.generateUnvalidSubmissionPageHtml(request.getUrl(),
							request.getResolvedParams().get("login"), result));
				}
			} else {
				response.setContent(HTMLHelper.generateSimpleSubmissionFormHtml(request.getUrl()));
			}
		} else {
			response.setContent("Bad Request");
		}

		return response;
	}

	private String processDeployment (String modelStream, String login) {
		ContainerRoot model = KevoreeXmiHelper.loadString(modelStream);
		String result = KloudResourceProvider.check(model);
		if (result.equals("")) {
			ContainerRoot newModel = KloudResourceProvider.setForKloud(model, this.getKevScriptEngineFactory());
			if (newModel != null) {
				UUIDModel uuidModel = this.getModelService().getLastUUIDModel();
				newModel = KloudResourceProvider.distribute(model, login, uuidModel);
				if (newModel != null) {
					boolean ok = KloudResourceProvider.update(uuidModel, newModel, this.getModelService());
					if (ok) {
						// TODO add port forwarding to allow user to have access to their nodes
						ok = true;
						if (ok) {
							// TODO send the user model to the user group to configure the software
							ok = true;
							if (ok) {
								// TODO keep a pointer to the model that has been sent to the group which represent the access point of the user software
								return "";// return the http address to have an access to the configured nodes
							} else {
								return "";
							}
						} else {
							return "Model has been deployed but we are unable to configure the cloud to give you access to your nodes.";
						}
					} else {
						return "Unable to update the system to deploy your software.";
					}
				} else {
					return "Unable to deploy your nodes on the Kloud.";
				}
			} else {
				return "Unable to apply KevScript to add a group that manage your software.";
			}
		} else {
			return result;
		}
	}
}
