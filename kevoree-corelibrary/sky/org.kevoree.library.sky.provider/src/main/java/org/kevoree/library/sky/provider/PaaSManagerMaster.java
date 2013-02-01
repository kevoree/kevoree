package org.kevoree.library.sky.provider;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sky.api.helper.KloudModelHelper;
import org.kevoree.library.sky.provider.api.PaaSService;
import org.kevoree.library.sky.provider.api.PaaSSlaveService;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/11/12
 * Time: 15:42
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@Provides({
		@ProvidedPort(name = "submit", type = PortType.SERVICE, className = PaaSService.class),
		@ProvidedPort(name = "slave", type = PortType.SERVICE, className = PaaSSlaveService.class)
})
@ComponentType
public class PaaSManagerMaster extends AbstractComponentType implements PaaSService, PaaSSlaveService, ModelListener {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContainerRoot paasModel;

	protected ExecutorService poolUpdate = null;


	@Start
	public void start () throws Exception {
		poolUpdate = Executors.newSingleThreadExecutor();
	}

	@Stop
	public void stop () {
		poolUpdate.shutdownNow();
		poolUpdate = null;
	}

	@Override
	@Port(name = "submit", method = "add")
	public void add (String id, ContainerRoot model) throws SubmissionException {
		// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// FIXME this must be removed when we will be able to use filter service channel
		if (getName().contains(id)) {
			KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
			PaaSKloudReasoner.addNodes(model.getNodes(), getModelService().getLastModel(), kengine, getName(), "PaaSManagerMaster", getNodeName(), "slave", "PaaSManagerSlave", "slave");
			Boolean created = false;
			for (int i = 0; i < 5; i++) {
				try {
					kengine.atomicInterpretDeploy();
					created = true;
					break;
				} catch (Exception e) {
					logger.warn("Error while try to remove some nodes for the PaaS {}, try number {}", id, i);
				}
			}
			notifyPaaS(id, created, model);
		}
	}

	@Override
	@Port(name = "submit", method = "remove")
	public void remove (String id, ContainerRoot model) throws SubmissionException {
		// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// FIXME this must be removed when we will be able to use filter service channel
		if (getName().contains(id)) {
			KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
			PaaSKloudReasoner.removeNodes(model.getNodes(), getModelService().getLastModel(), kengine);
			Boolean created = false;
			for (int i = 0; i < 5; i++) {
				try {
					kengine.atomicInterpretDeploy();
					created = true;
					break;
				} catch (Exception e) {
					logger.warn("Error while try to remove some nodes for the PaaS {}, try number {}", id, i);
				}
			}
			notifyPaaS(id, created, model);
		}
	}

	@Override
	@Port(name = "submit", method = "merge")
	public void merge (String id, ContainerRoot model) throws SubmissionException {
		// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// FIXME this must be removed when we will be able to use filtered service channel
		if (getName().contains(id)) {
			notifyPaaS(id, merge(paasModel, model), model);
		}
	}

	private boolean merge (ContainerRoot currentPaaSModel, ContainerRoot newPaaSModel) {
		List<ContainerNode> nodesToAdd = PaaSKloudReasoner.getNodesToAdd(currentPaaSModel, newPaaSModel);
		List<ContainerNode> nodesToRemove = PaaSKloudReasoner.getNodesToRemove(currentPaaSModel, newPaaSModel);
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		PaaSKloudReasoner.removeNodes(nodesToRemove, getModelService().getLastModel(), kengine);
		PaaSKloudReasoner.addNodes(nodesToAdd, getModelService().getLastModel(), kengine, getName(), "PaaSManagerMaster", getNodeName(), "slave", "PaaSManagerSlave", "slave");
		for (int i = 0; i < 5; i++) {
			try {
				kengine.atomicInterpretDeploy();
				return true;
			} catch (Exception e) {
				logger.warn("Error while try to merge a new model on the PaaS managed by {}, try number {}", getName(), i);
			}
		}
		return false;
	}

	private void notifyPaaS (String id, boolean created, ContainerRoot model) throws SubmissionException {
		if (created) {
			// TODO merge the current model of the PaaS with the new one
			// TODO store this merged model as the current of the PaaS
			/* Send blindly the model to the core , PaaS Group are in charge to trigger this request, reply false and forward to the PaaS nodes*/
			getModelService().checkModel(model);
		} else {
			throw new SubmissionException("Unable to execute the operation for the PaaS " + id);
		}

	}

	@Override
	@Port(name = "submit", method = "getModel")
	public ContainerRoot getModel (String id) throws SubmissionException {
// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// FIXME this must be removed when we will be able to use filter service channel
		if (getName().contains(id)) {
			// TODO remove from the user model the PaaSManagerSlave Component (and the other stuff the infrastructure have defined)
			// maybe there is nothing to do but it's better to check
			// maybe this is not needed
			return paasModel;
		}
		return null;
	}

	@Override
	@Port(name = "slave", method = "getModel")
	public ContainerRoot getModel () throws SubmissionException {
		// TODO merge the user model with the PaaSManagerSlave Component (and the other stuff the infrastructure have defined)
		// maybe this is not needed
		return null;
	}

	@Override
	public boolean preUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		logger.debug("Trigger pre update");

		if (KloudModelHelper.isIaaSNode(currentModel, getNodeName()) && KloudModelHelper.isPaaSModel(proposedModel, getName(), getNodeName())) {
			logger.debug("A new user model is received (sent through the core by a PaaS Group), adding a task to process a deployment");
			submitJob(proposedModel);
			// abort the update because the model is not for the IaaS but for the PaaS
			return false;
		} else {
			logger.debug("nothing specific, update can be done");
			return true;
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
	}

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	void submitJob (ContainerRoot model) {
		IaaSUpdate jobUpdate = new IaaSUpdate(paasModel, model);
		paasModel = model;
		poolUpdate.submit(jobUpdate);
	}

	private class IaaSUpdate implements Runnable {

		private ContainerRoot currentModel = null;
		private ContainerRoot proposedModel = null;

		public IaaSUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
			this.currentModel = currentModel;
			this.proposedModel = proposedModel;
		}

		@Override
		public void run () {
			merge(currentModel, proposedModel);
			// FIXME maybe we store the new model as the current one to earlier because the merge can fail

		}
	}
}
