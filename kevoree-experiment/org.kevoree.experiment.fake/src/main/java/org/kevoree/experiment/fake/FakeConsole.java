package org.kevoree.experiment.fake;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 01/06/11
 * Time: 16:47
 */
@Library(name = "KevoreeExperiment")
@ComponentType
public class FakeConsole  extends AbstractComponentType {


	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Start
	public void start() {
		logger.debug("Fake console is now running");
	}

	@Stop
	public void stop() {
		logger.debug("Fake console is now stopped");
	}

	@Update
	public void update() {
		logger.debug("Fake console is updating...");
		logger.debug("Fake console is updated");
	}
}
