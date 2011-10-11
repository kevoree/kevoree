/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.annotation.NodeType;
import org.kevoree.annotation.PrimitiveCommands;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ffouquet
 */
@NodeType
@PrimitiveCommands(
		values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "StartInstance", "StopInstance"},
		value = {})
public class JavaSENode extends AbstractNodeType {
	private static final Logger logger = LoggerFactory.getLogger(JavaSENode.class);

	private KevoreeKompareBean kompareBean = null;
	private BaseDeployOSGi deployBean = null;
	private ServiceRegistration sendModel;
	private ServiceRegistration backupModel;


	@Start
	@Override
	public void startNode () {
		kompareBean = new KevoreeKompareBean();
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		deployBean = new BaseDeployOSGi(bundle);


		// Register the command service for felix shell
		sendModel = bundle.getBundleContext()
				.registerService(org.apache.felix.shell.Command.class.getName(),
						new SendModelFelixCommand(this.getModelService()), null);
		backupModel = bundle.getBundleContext()
				.registerService(org.apache.felix.shell.Command.class.getName(),
						new BackupModelFelixCommand(this.getModelService()), null);


	}

	@Stop
	@Override
	public void stopNode () {

		kompareBean = null;
		deployBean = null;
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		return kompareBean.kompare(current, target, this.getNodeName());
	}

	@Override
	public org.kevoree.framework.PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return deployBean.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
	}

	@Override
	public void push (String targetNodeName, ContainerRoot root) {

		logger.error("JavaSE have no strategy to deploy model !!! ");

	}
}
