package org.kevoree.library.sky.logging;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;

import java.io.File;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/08/12
 * Time: 22:07
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class LoggingReasoner extends AbstractComponentType implements ModelListener {

	@Start
	public void start() {
		getModelService().registerModelListener(this);
	}

	@Stop
	public void stop() {
		getModelService().registerModelListener(this);
	}

	@Override
	public boolean preUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		return true;
	}

	@Override
	public boolean initUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		return true;
	}

	@Override
	public boolean afterLocalUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		return true;
	}

	@Override
	public void modelUpdated () {
		KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
		ContainerNode localNode = ((ContainerNode)getModelElement().eContainer());
		kengine.addVariable("nodeName", localNode.getName());
		for (ContainerNode child : localNode.getHostsForJ()) {
			boolean isAlreadyMonitored = false;
			for (ComponentInstance component : localNode.getComponentsForJ()) {
				if (component.getTypeDefinition().getName().equals("HelloWorldNodeJS") && component.getName().equals(child.getName())) {
					isAlreadyMonitored = true;
				}
			}
			if (!isAlreadyMonitored && new File(System.getProperty("java.io.tmpdir") + File.separator + child.getName() + ".log.out").exists()
					&& new File(System.getProperty("java.io.tmpdir") + File.separator + child.getName() + ".log.err").exists()) {
				kengine.addVariable("componentName", child.getName());
				kengine.append("addComponent {componentName}@{nodeName} : HelloWorldNodeJS");
				//System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log.out"
				//System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log.err"
			}
		}
		// FIXME must be completed
	}
}
