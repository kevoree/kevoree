package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

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
	public void modelUpdated () {
		UUIDModel uuidModel = this.getModelService().getLastUUIDModel();
		Option<ContainerRoot> modelOption = KloudReasoner.configureChildNodes(uuidModel.getModel(), this.getKevScriptEngineFactory());
		if (modelOption.isDefined()) {
			try {
				this.getModelService().unregisterModelListener(this);
				this.getModelService().atomicCompareAndSwapModel(uuidModel, modelOption.get());
				this.getModelService().registerModelListener(this);
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				modelUpdated();
			}
		} else {
			logger.debug("Unable to configure child nodes");
		}
	}
}