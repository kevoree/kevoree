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
public class IaaSKloudResourceManager extends AbstractComponentType implements ModelListener {

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
		if (KloudReasoner.configureChildNodes(getModelService().getLastModel(), kengine)) {
			this.getModelService().unregisterModelListener(this);
			updateKloudConfiguration(kengine);
			this.getModelService().registerModelListener(this);
		}
	}

	private void updateKloudConfiguration (KevScriptEngine kengine) {
		try {
			kengine.atomicInterpretDeploy();
		} catch (Exception e) {
			logger.error("Unable to apply script", e);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
			}
			updateKloudConfiguration(kengine);
		}
	}
}