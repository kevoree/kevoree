package org.kevoree.library.sky.api.nodeType;

import org.kevoree.annotation.NodeFragment;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/10/12
 * Time: 17:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@NodeFragment
public abstract class AbstractIaaSNode extends AbstractHostNode implements IaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(AbstractIaaSNode.class);

	@Override
	public void startNode () {
		super.startNode();
	}

	protected void updateModel (KevScriptEngine kengine) {
		Boolean created = false;
		for (int i = 0; i < 20; i++) {
			try {
				kengine.atomicInterpretDeploy();
				created = true;
				break;
			} catch (Exception e) {
				logger.warn("Error while try to update the configuration of node {}, try number {}", new String[]{getName(), i + ""}, e);
			}
		}
		if (!created) {
			logger.error("After 20 attempt, it was not able to update the configuration of {}", getName());
		}
	}
}
