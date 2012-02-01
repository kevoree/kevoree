package org.kevoree.library.sky.minicloud.group;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractGroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 11/10/11
 * Time: 19:29
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@GroupType
public class MiniCloudGroup extends AbstractGroupType {
	private static final Logger logger = LoggerFactory.getLogger(MiniCloudGroup.class);
	//private ServiceRegistration sendModel;
	//private ServiceRegistration backupModel;

	@Start
	public void startMiniCloudGroup () {
		logger.debug("starting MiniCloud group");
        /*
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		// Register the command service for felix shell
		sendModel = bundle.getBundleContext()
				.registerService(org.apache.felix.shell.Command.class.getName(),
						new SendModelFelixCommand(this.getModelService()), null);
		backupModel = bundle.getBundleContext()
				.registerService(org.apache.felix.shell.Command.class.getName(),
						new BackupModelFelixCommand(this.getModelService()), null);*/
		logger.debug("MiniCloud group is started");
	}

	@Stop
	public void stopMiniCloudGroup () {
		//sendModel.unregister();
		//backupModel.unregister();
	}

	@Override
	public void triggerModelUpdate () {
	}

	@Override
	public void push (ContainerRoot containerRoot, String s) {
	}

	@Override
	public ContainerRoot pull (String s) {
		return null;
	}
}
