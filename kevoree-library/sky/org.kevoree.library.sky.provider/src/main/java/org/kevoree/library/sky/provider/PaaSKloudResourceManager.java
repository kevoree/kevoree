package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
		@DictionaryAttribute(name = "displayIP", optional = false, defaultValue = "true", vals = {"true", "false"})
})
public class PaaSKloudResourceManager extends ParentAbstractPage {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void requestHandler (Object param) {
		final Object params = param;
		new Thread() {
			@Override
			public void run () {
				PaaSKloudResourceManager.super.requestHandler(params);
			}
		}.start();
	}

	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		if (request != null) {
			if (!request.getUrl().endsWith("/css/bootstrap.min.css") && !request.getUrl().endsWith("/release")) {
				if (request.getResolvedParams().get("model") != null && request.getResolvedParams().get("login") != null
						&& request.getResolvedParams().get("password") != null && request.getResolvedParams().get("ssh_key") != null) {
					// check authentication information
					if (InriaLdap.testLogin(request.getResolvedParams().get("login"), request.getResolvedParams().get("password"))) {

						String result = process(request.getResolvedParams().get("model"), request.getResolvedParams().get("login"), request.getResolvedParams().get("ssh_key"));
						if (result.startsWith("http")) {
							response.setContent(
									HTMLHelper.generateValidSubmissionPageHtml(request.getUrl(), request.getResolvedParams().get("login"), result, this.getDictionary().get("urlpattern").toString()));
						} else {
							response.setContent(
									HTMLHelper
											.generateUnvalidSubmissionPageHtml(request.getUrl(), request.getResolvedParams().get("login"), result, this.getDictionary().get("urlpattern").toString()));
						}
					} else {
						response.setContent(HTMLHelper.generateFailToLoginPageHtml(request.getResolvedParams().get("login"), this.getDictionary().get("urlpattern").toString()));
					}
				} else {
					response.setContent(HTMLHelper.generateSimpleSubmissionFormHtml(request.getUrl(), this.getDictionary().get("urlpattern").toString()));
				}
			} else if (request.getUrl().endsWith("/css/bootstrap.min.css")) {
				try {
					InputStream ins = this.getClass().getClassLoader().getResourceAsStream("css/bootstrap.min.css");
					response.setContent(new String(convertStream(ins), "UTF-8"));
					response.getHeaders().put("Content-Type", "text/css");
					ins.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			} else if (request.getUrl().endsWith("/release")) {
				if (request.getResolvedParams().get("login") != null && request.getResolvedParams().get("password") != null) {
					// check authentication information
					if (InriaLdap.testLogin(request.getResolvedParams().get("login"), request.getResolvedParams().get("password"))) {
						release(request.getResolvedParams().get("login"));
						response.setContent(HTMLHelper.generateReleaseResponse(request.getResolvedParams().get("login"), this.getDictionary().get("urlpattern").toString()));
					} else {
						response.setContent(HTMLHelper.generateFailToLoginPageHtml(request.getResolvedParams().get("login"), this.getDictionary().get("urlpattern").toString()));
					}
				} else {
					response.setContent(HTMLHelper.generateReleaseForm(request.getUrl(), this.getDictionary().get("urlpattern").toString()));
				}
			}
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
			return processNew(model, login, sshKey);
		}
	}

	private void release (String login) {
		// send an empty model on the group => all nodes will be removed
		Option<String> masterNodeOption = KevoreePropertyHelper.getStringPropertyForGroup(this.getModelService().getLastModel(), login, "masterNode", false, "");
		if (masterNodeOption.isDefined()) {
			try {
				URL url = new URL(masterNodeOption.get() + "/release");
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(2000);
				InputStream stream = conn.getInputStream();
				while (stream.read() != -1) {
				}
				stream.close();
				logger.debug("Try to stop the group");
				if (!KloudReasoner.removeGroup(login, getModelService(), getKevScriptEngineFactory(), 5)) {
					logger.debug("Unable to remove {}", login);
				} else if (!KloudReasoner.removeProxy(login, getNodeName(), getModelService(), getKevScriptEngineFactory(), 5)) {
					logger.debug("Unable to remove {}_proxy", login);
				}

			} catch (MalformedURLException ignored) {
			} catch (IOException ignored) {
			}
		} else {
			logger.debug("Unable enough information about {} configuration to remove it", login);
		}

	}

	private String processNew (ContainerRoot model, String login, String sshKey) {
		UUIDModel uuidModel = this.getModelService().getLastUUIDModel();

		// we create a group with the login of the user
		Option<ContainerRoot> newKloudModelOption = KloudReasoner.createGroup(login, this.getNodeName(), uuidModel.getModel(), getKevScriptEngineFactory(), sshKey, this.getDictionary().get("displayIP").toString());
		if (newKloudModelOption.isDefined()) {

			Option<Group> groupOption = KloudHelper.getGroup(login, newKloudModelOption.get());
			if (groupOption.isDefined()) {
				try {
					// update the kloud model by adding the group (the nodes are not added)
					this.getModelService().atomicCompareAndSwapModel(uuidModel, newKloudModelOption.get());
				} catch (Exception e) {
					return processNew(model, login, sshKey);
				}
				// push the user model to this group on the master fragment
				Option<ContainerRoot> newUserModelOption = KloudReasoner.updateUserConfiguration(login, model, this.getModelService(), this.getKevScriptEngineFactory());

				if (newUserModelOption.isDefined()) {
					Option<String> masterNodeOption = KevoreePropertyHelper.getStringPropertyForGroup(newKloudModelOption.get(), login, "masterNode", false, "");
					if (masterNodeOption.isDefined()) {
						Option<String> addressOption = KloudHelper.pushOnMaster(newUserModelOption.get(), login, masterNodeOption.get());
						if (addressOption.isDefined()) {
							if (createProxy(login, 5)) {
								return addressOption.get(); // TODO maybe find the proxy address
							} else {
								logger.error("Unable to add the proxy for the user {}", login);
								return "Unable to add the proxy for the user " + login;
							}
						} else {
							logger.debug("Unable to commit the user model on nodes");
							return "Unable to commit the user model on nodes";
						}
					} else {
						logger.debug("Unable to find masterNode property on group configuration");
						return "Unable to find masterNode property on group configuration"; // must never appear
					}
				} else {
					logger.debug("Unable to update the user configuration by setting Kloud specific properties");
					return "Unable to update the user configuration by setting Kloud specific properties";
				}
			} else {
				logger.debug("Unable to find the user group for {}", login);
				return "Unable to find the user group for {}" + login;
			}
		} else {
			logger.debug("Unable to create the needed user group to give access the nodes to the user.");
			return "Unable to create the needed user group to give access the nodes to the user.";
		}
	}

	private boolean createProxy (String login, int nbTry) {
		UUIDModel uuidModel = this.getModelService().getLastUUIDModel();
		Option<ContainerRoot> kloudModelOption = KloudReasoner.createProxy(login, this.getNodeName(), "/" + login, uuidModel.getModel(), getKevScriptEngineFactory());
		if (kloudModelOption.isDefined()) {
		}
		try {
			this.getModelService().atomicCompareAndSwapModel(uuidModel, kloudModelOption.get());
			return true;
		} catch (Exception e) {
		}
		return nbTry > 0 && createProxy(login, nbTry - 1);
	}

	private byte[] convertStream (InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int l;
		do {
			l = (in.read(buffer));
			if (l > 0) {
				out.write(buffer, 0, l);
			}
		} while (l > 0);
		out.flush();
		out.close();
		return out.toByteArray();
	}
}