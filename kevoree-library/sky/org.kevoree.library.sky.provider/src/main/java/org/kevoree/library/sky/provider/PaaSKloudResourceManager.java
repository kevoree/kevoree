package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

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
@Provides({
		@ProvidedPort(name = "deploy", type = PortType.MESSAGE), // TODO define a message type
		@ProvidedPort(name = "release", type = PortType.MESSAGE) // TODO define a message type
})

public class PaaSKloudResourceManager extends AbstractComponentType {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Port(name = "deploy")
	public void deploy (Object message) {
		if (message instanceof StdKevoreeMessage) {
			StdKevoreeMessage stdMessage = (StdKevoreeMessage) message;
			if (stdMessage.getValue("login").isDefined() && stdMessage.getValue("model").isDefined()) {
				final String login = (String) stdMessage.getValue("login").get();
				final ContainerRoot model = KevoreeXmiHelper.loadString((String) stdMessage.getValue("model").get());
				final String sshKey = (String) stdMessage.getValue("sshKey").get();
				// check if a previous deploy has already done for this login
				if (!KloudHelper.lookForAGroup(login, this.getModelService().getLastModel())) {
					processNew(model, login, sshKey);

				}
			}
		}
	}

	@Port(name = "release")
	public void release (Object message) {
		if (message instanceof StdKevoreeMessage) {
			StdKevoreeMessage stdMessage = (StdKevoreeMessage) message;
			if (stdMessage.getValue("login").isDefined()) {
				String login = (String) stdMessage.getValue("login").get();
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
		}
	}

	private void processNew (ContainerRoot model, String login, String sshKey) {
		logger.debug("starting processNew");
		UUIDModel uuidModel = this.getModelService().getLastUUIDModel();

		// we create a group with the login of the user
		Option<ContainerRoot> newKloudModelOption = KloudReasoner
				.createGroup(login, this.getNodeName(), uuidModel.getModel(), getKevScriptEngineFactory(), sshKey, this.getDictionary().get("displayIP").toString());
		if (newKloudModelOption.isDefined()) {

			Option<Group> groupOption = KloudHelper.getGroup(login, newKloudModelOption.get());
			// check if the group is now well defined
			if (groupOption.isDefined()) {
				try {
					// update the kloud model by adding the group (the nodes are not added)
					this.getModelService().atomicCompareAndSwapModel(uuidModel, newKloudModelOption.get());
				} catch (Exception e) {
					// if it fails, it try again
					processNew(model, login, sshKey);
				}
				// update user configuration to allow him to use the group as access point their nodes
				Option<ContainerRoot> newUserModelOption = KloudReasoner.updateUserConfiguration(login, model, this.getModelService(), this.getKevScriptEngineFactory());
				if (newUserModelOption.isDefined()) {
					Option<String> masterNodeOption = KevoreePropertyHelper.getStringPropertyForGroup(newUserModelOption.get(), login, "masterNode", false, "");
					if (masterNodeOption.isDefined()) {
						// push the user model to this group on the master fragment
						/*Option<String> addressOption = */
						KloudHelper.pushOnMaster(newUserModelOption.get(), login, masterNodeOption.get());
						/*if (addressOption.isDefined()) {
							if (createProxy(login, 5)) {
//								return addressOption.get(); // TODO maybe find the proxy address
							} else {
								logger.error("Unable to add the proxy for the user {}", login);
//								return "Unable to add the proxy for the user " + login;
							}
						} else {
							logger.debug("Unable to commit the user model on nodes");
//							return "Unable to commit the user model on nodes";
						}*/
					} else {
						logger.debug("Unable to find masterNode property on group configuration");
//						return "Unable to find masterNode property on group configuration"; // must never appear
					}
				} else {
					logger.debug("Unable to update the user configuration by setting Kloud specific properties");
//					return "Unable to update the user configuration by setting Kloud specific properties";
				}
			} else {
				logger.debug("Unable to find the user group for {}", login);
//				return "Unable to find the user group for {}" + login;
			}
		} else {
			logger.debug("Unable to create the needed user group to give access the nodes to the user.");
//			return "Unable to create the needed user group to give access the nodes to the user.";
		}
	}
}