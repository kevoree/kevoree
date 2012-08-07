package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.message.StdKevoreeMessage;
import scala.Option;

import java.io.File;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 14:51
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "storage", optional = false)

})
public class PaaSKloudResourceManagerStateFull extends PaaSKloudResourceManager implements ModelListener {

	@Start
	public void start () throws Exception {
		super.start();
		File f = new File(getStorage());
		if (!f.exists()) {
			if (!f.mkdirs()) {
				throw new Exception("Unable to mkdirs the storage folder: " + f.getAbsolutePath());
			}
		} else if (f.isDirectory()) {
			for (File modelFile : f.listFiles()) {
				try {
					ContainerRoot model = KevoreeXmiHelper.load(modelFile.getAbsolutePath());
					processNew(model, modelFile.getName(), "");
				} catch (Exception ignored) {
				}
			}
		}
	}

	@Stop
	public void stop () {
	}

	public String getStorage () {
		return this.getDictionary().get("storage").toString();
	}

	@Port(name = "release")
	public void release (Object message) {
		if (message instanceof StdKevoreeMessage) {
			StdKevoreeMessage stdMessage = (StdKevoreeMessage) message;
			if (stdMessage.getValue("login").isDefined()) {
				release((String) stdMessage.getValue("login").get());
			}
		}
	}

	private void release (String login) {
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		KloudReasoner.appendScriptToCleanupIaaSModelFromUser(kengine, login, getModelService().getLastModel());
		for (int i = 0; i < 5; i++) {
			try {
				kengine.atomicInterpretDeploy();
				// TODO remove stored model
				break;
			} catch (Exception e) {
				logger.warn("Error while cleanup user, try number " + i);
			}
		}
	}

	protected void processNew (ContainerRoot userModel, String login, String sshKey) {
		logger.debug("starting processNew");
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		KloudReasoner.appendCreateGroupScript(getModelService().getLastModel(), login, this.getNodeName(), kengine, sshKey, true);
		for (int i = 0; i < 5; i++) {
			try {
				kengine.atomicInterpretDeploy();
				break;
			} catch (Exception e) {
				logger.warn("Error while adding user master group, try number " + i);
			}
		}
		//ADD GROUP to user model
		logger.debug("update user configuration when user model must be forwarded");
		Option<ContainerRoot> userModelUpdated = KloudReasoner.updateUserConfiguration(login, userModel, getModelService().getLastModel(), getKevScriptEngineFactory());
		if (userModelUpdated.isDefined()) {
			/* Send blindly the model to the core , PaaS Group are in charge to trigger this request , reply false and forward to Master interested node  */
			getModelService().checkModel(userModelUpdated.get());
			storeModel(userModelUpdated.get(), login);
		} else {
			//TODO CALL RELEASE

		}
	}

	@Override
	public boolean preUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		logger.debug("Trigger pre update");

		if (KloudHelper.isIaaSNode(currentModel, getName(), getNodeName()) && KloudHelper.isUserModel(proposedModel)) {
			logger.debug("A new user model is received, storing it.");
			Option<String> groupOption = KloudHelper.getKloudUserGroup(proposedModel);
			if (groupOption.isDefined()) {
				storeModel(proposedModel, groupOption.get());
			}
			return false; // TODO check if we need to return true or false
		} else {
			logger.debug("Nothing specific, update can be done");
			return true;
		}
	}

	private void storeModel (ContainerRoot userModel, String login) {
		try {
			File userModelFile = new File(getStorage() + File.separator + login);
			if (userModelFile.exists() || userModelFile.createNewFile()) {
				logger.debug("Storing user model for {}.", login);
				KevoreeXmiHelper.save(userModelFile.getAbsolutePath(), userModel);
				logger.debug("New model store for {}.", login);
			} else {
				logger.warn("Unable to store user model for {}.", login);
			}
		} catch (Exception e) {
			logger.warn("Unable to store user model for {}.", login, e);
		}


	}

	@Override
	public boolean initUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public void modelUpdated () {
	}
}
