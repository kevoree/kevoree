package org.kevoree.library.sky.provider;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sky.api.helper.KloudModelHelper;
import org.kevoree.library.sky.provider.api.PaaSService;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
		@ProvidedPort(name = "submit", type = PortType.SERVICE, className = PaaSService.class)
})
@ComponentType
public class PaaSManager extends AbstractComponentType implements PaaSService {
	Logger logger = LoggerFactory.getLogger(this.getClass());


	@Start
	public void start () throws Exception {
	}

	@Stop
	public void stop () {
	}

	@Override
	@Port(name = "submit", method = "add")
	public void add (ContainerRoot model) throws SubmissionException {
		// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// TODO this must be removed when we will be able to use filter service channel
		List<Group> groups = KloudModelHelper.getPaaSKloudGroups(model);
		for (Group group : groups) {
			if (getName().contains(group.getName())) {
				// add all nodes available on the model
				KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
				PaaSKloudReasoner.addNodes(model.getNodesForJ(), getModelService().getLastModel(), kengine);
				Boolean created = false;
				for (int i = 0; i < 5; i++) {
					try {
						kengine.atomicInterpretDeploy();
						created = true;
						break;
					} catch (Exception e) {
						logger.warn("Error while try to remove some nodes for the PaaS {}, try number {}", group.getName(), i);
					}
				}
				if (created) {
					sendModelToPaaS(model);
					return;
				} else {
					throw new SubmissionException("Unble to add some nodes for the PaaS " + group.getName());
				}
			}
		}
		throw new SubmissionException("Unble to find the corresponding PaaS !");
	}

	@Override
	@Port(name = "submit", method = "remove")
	public void remove (ContainerRoot model) throws SubmissionException {
		// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// TODO this must be removed when we will be able to use filter service channel
		List<Group> groups = KloudModelHelper.getPaaSKloudGroups(model);
		for (Group group : groups) {
			if (getName().contains(group.getName())) {
				// remove all nodes available on the model
				KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
				PaaSKloudReasoner.removeNodes(model.getNodesForJ(), getModelService().getLastModel(), kengine);
				Boolean created = false;
				for (int i = 0; i < 5; i++) {
					try {
						kengine.atomicInterpretDeploy();
						created = true;
						break;
					} catch (Exception e) {
						logger.warn("Error while try to remove some nodes for the PaaS {}, try number {}", group.getName(), i);
					}
				}
				if (created) {
					sendModelToPaaS(model);
					return;
				} else {
					throw new SubmissionException("Unble to remove some nodes for the PaaS " + group.getName());
				}
			}
		}
		throw new SubmissionException("Unble to find the corresponding PaaS !");
	}

	@Override
	@Port(name = "submit", method = "merge")
	public void merge (ContainerRoot model) throws SubmissionException {
		// looking for the group on the model that precise the PaaS id
		// if the id is contains by the name of the instance of PaaSManager then this instance will used it
		// TODO this must be removed when we will be able to use filter service channel
		List<Group> groups = KloudModelHelper.getPaaSKloudGroups(model);
		for (Group group : groups) {
			if (getName().contains(group.getName())) {
				// merge the model (add and remove nodes)
				List<ContainerNode> nodesToAdd = PaaSKloudReasoner.getNodesToAdd(getModelService().getLastModel(), model);
				List<ContainerNode> nodesToRemove = PaaSKloudReasoner.getNodesToRemove(getModelService().getLastModel(), model);
				KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
				PaaSKloudReasoner.removeNodes(nodesToRemove, getModelService().getLastModel(), kengine);
				PaaSKloudReasoner.addNodes(nodesToAdd, getModelService().getLastModel(), kengine);
				Boolean created = false;
				for (int i = 0; i < 5; i++) {
					try {
						kengine.atomicInterpretDeploy();
						created = true;
						break;
					} catch (Exception e) {
						logger.warn("Error while try to merge a new model on the PaaS {}, try number {}", group.getName(), i);
					}
				}
				if (created) {
					sendModelToPaaS(model);
					return;
				} else {
					throw new SubmissionException("Unble to merge the new model with the one of the PaaS " + group.getName());
				}
			}
		}
		throw new SubmissionException("Unble to find the corresponding PaaS !");
	}

	private void sendModelToPaaS (ContainerRoot model) {
		/* Send blindly the model to the core , PaaS Group are in charge to trigger this request, reply false and forward to the PaaS nodes*/
		getModelService().checkModel(model);
	}
}
