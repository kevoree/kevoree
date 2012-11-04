package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;
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
public class IaaSKloudManager extends AbstractComponentType implements ModelListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Start
	public void start () {
		this.getModelService().registerModelListener(this);
	}

	@Stop
	public void stop () {
		this.getModelService().unregisterModelListener(this);
	}

	@Update
	public void update () {
		stop();
		start();
	}


	@Override
	public boolean preUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
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
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		if (IaaSKloudReasoner.configureChildNodes(getModelService().getLastModel(), kengine)) {
			this.getModelService().unregisterModelListener(this);
			updateIaaSConfiguration(kengine);
			this.getModelService().registerModelListener(this);
		}
	}

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
	}

	private void updateIaaSConfiguration (KevScriptEngine kengine) {
		Boolean created = false;
		for (int i = 0; i < 20; i++) {
			try {
				kengine.atomicInterpretDeploy();
				created = true;
				break;
			} catch (Exception e) {
				logger.warn("Error while try to update the iaas configuration, try number {}", i);
			}
		}
		if (!created) {
			logger.error("After 20 attempt, it was not able to update the IaaS configuration");
		}
	}
}