package org.kevoree.library.sky.provider;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;


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
		@DictionaryAttribute(name = "port", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "SSH_Public_Key", optional = true)
})
public class KloudPaaSNanoGroupStateFull extends KloudPaaSNanoGroup {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	void submitJob (ContainerRoot model) {
		IaaSUpdateStateFull jobUpdate = new IaaSUpdateStateFull(userModel, model);
		userModel = model;
		poolUpdate.submit(jobUpdate);
	}

	private class IaaSUpdateStateFull implements Runnable {

		private ContainerRoot currentModel = null;
		private ContainerRoot proposedModel = null;

		public IaaSUpdateStateFull (ContainerRoot currentModel, ContainerRoot proposedModel) {
			this.currentModel = currentModel;
			this.proposedModel = proposedModel;
		}

		@Override
		public void run () {
			if (KloudReasoner.needsNewDeployment(proposedModel, currentModel)) {
				logger.debug("A new Deployment must be done!");
				KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
				if (KloudReasoner.processDeployment(proposedModel, currentModel, getModelService().getLastModel(), kengine, getName())) {
					for (int i = 0; i < 5; i++) {
						try {
							kengine.atomicInterpretDeploy();
							break;
						} catch (Exception e) {
							logger.debug("Error while update user configuration, try number " + i);
						}
					}
				}

			}
			//ADD GROUP to user model
			logger.debug("update user configuration when user model must be forwarded");
			Option<ContainerRoot> userModelUpdated = KloudReasoner.updateUserConfiguration(getName(), proposedModel, getModelService().getLastModel(), getKevScriptEngineFactory());
			if (userModelUpdated.isDefined()) {
				for (ContainerNode userNode : currentModel.getNodesForJ()) {
					try {
						push(userModelUpdated.get(), userNode.getName());
					} catch (Exception ignored) {

					}
				}
				getModelService().unregisterModelListener(getModelListener());
				getModelService().checkModel(proposedModel);
				getModelService().registerModelListener(getModelListener());
			} else {
				logger.error("Unable to update user configuration, with user group");
			}
		}
	}


}