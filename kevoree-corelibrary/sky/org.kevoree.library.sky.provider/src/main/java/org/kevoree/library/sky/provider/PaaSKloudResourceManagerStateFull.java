/*
package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.sky.api.helper.KloudModelHelper;
import org.kevoree.library.sky.helper.KloudProviderHelper;
import scala.Option;

import java.io.File;

*/
/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 14:51
 *
 * @author Erwan Daubert
 * @version 1.0
 *//*

@Library(name = "SKY")
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "storage", optional = false)

})
public class PaaSKloudResourceManagerStateFull extends PaaSKloudResourceManager implements ModelListener {

	private boolean isStarting;

	@Start
	public void start () throws Exception {
		super.start();
		isStarting = true;
		getModelService().registerModelListener(this);
	}

	@Stop
	public void stop () {
		getModelService().unregisterModelListener(this);
	}

	public String getStorage () {
		return this.getDictionary().get("storage").toString();
	}

	@Port(name = "submit", method = "release")
	public void release (Object message) {
		if (message instanceof StdKevoreeMessage) {
			StdKevoreeMessage stdMessage = (StdKevoreeMessage) message;
			if (stdMessage.getValue("login").isDefined()) {
				release((String) stdMessage.getValue("login").get());
			}
		}
	}

	public boolean release (String login) {
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		KloudReasoner.appendScriptToCleanupIaaSModelFromUser(kengine, login, getModelService().getLastModel());
		for (int i = 0; i < 5; i++) {
			try {
				getModelService().unregisterModelListener(this);
				kengine.atomicInterpretDeploy();
				getModelService().registerModelListener(this);
				deleteStoredModel(login);
				return true;
			} catch (Exception e) {
				logger.warn("Error while cleanup user, try number " + i);
			}
		}
		return false;
	}

	protected void processNew (ContainerRoot userModel, String login, String sshKey) {
		logger.debug("starting processNew");
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		KloudProviderHelper.appendCreateGroupScript(getModelService().getLastModel(), login, this.getNodeName(), kengine, sshKey, true);
		for (int i = 0; i < 5; i++) {
			try {
				getModelService().unregisterModelListener(this);
				kengine.atomicInterpretDeploy();
				getModelService().registerModelListener(this);
				break;
			} catch (Exception e) {
				logger.warn("Error while adding user master group, try number " + i);
			}
		}
		//ADD GROUP to user model
		logger.debug("update user configuration when user model must be forwarded");
		Option<ContainerRoot> userModelUpdated = KloudReasoner.updateUserConfiguration(login, userModel, getModelService().getLastModel(), getKevScriptEngineFactory());
		if (userModelUpdated.isDefined()) {
			logger.debug("Send blindly the model to the core , PaaS Group are in charge to trigger this request , reply false and forward to Master interested node");
			*/
/* Send blindly the model to the core , PaaS Group are in charge to trigger this request , reply false and forward to Master interested node  *//*

			getModelService().unregisterModelListener(this);
			getModelService().checkModel(userModelUpdated.get());
			getModelService().registerModelListener(this);
//			storeModel(userModelUpdated.get(), login);
		} else {
			//TODO CALL RELEASE

		}
	}

	@Override
	public boolean preUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		logger.debug("Trigger pre update");

		if (KloudModelHelper.isIaaSNode(currentModel, getNodeName()) && KloudModelHelper.isPaaSModel(proposedModel)) {
			logger.debug("A new user model is received, storing it.");
			Option<String> groupOption = KloudModelHelper.getPaaSKloudGroup(proposedModel);
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
				logger.debug("New model store for {} on {}.", login, userModelFile.getAbsolutePath());
			} else {
				logger.warn("Unable to store user model for {}.", login);
			}
		} catch (Exception e) {
			logger.warn("Unable to store user model for {}.", login, e);
		}


	}

	private void deleteStoredModel (String login) {
		try {
			File userModelFile = new File(getStorage() + File.separator + login);
			if (userModelFile.exists() && userModelFile.delete()) {
				logger.debug("stored user model for {} has been deleted.", login);
			} else {
				logger.warn("Unable to delete the stored user model for {} on {}.", login, userModelFile.getAbsolutePath());
			}
		} catch (Exception e) {
			logger.warn("Unable to delete stored user model for {}.", login, e);
		}
	}

	@Override
	public boolean initUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public boolean afterLocalUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public void modelUpdated () {
		if (isStarting) {
			isStarting = false;
			File f = new File(getStorage());
			if (!f.exists()) {
				if (!f.mkdirs()) {
					logger.warn("Unable to mkdirs the storage folder: " + f.getAbsolutePath());
				}
			} else if (f.isDirectory()) {
				for (final File modelFile : f.listFiles()) {
					try {
						final ContainerRoot model = KevoreeXmiHelper.load(modelFile.getAbsolutePath());
						new Thread() {
							@Override
							public void run () {
								processNew(model, modelFile.getName(), "");
							}
						}.start();
					} catch (Exception ignored) {
					}
				}
			}
		}
	}

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) { // TODO maybe something to do
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}
}
*/
