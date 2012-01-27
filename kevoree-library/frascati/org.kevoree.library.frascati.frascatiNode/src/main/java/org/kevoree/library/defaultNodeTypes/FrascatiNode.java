/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author obrais
 */
@Library(name="Frascati")
public class FrascatiNode extends JavaSENode {
	private static final Logger logger = LoggerFactory.getLogger(FrascatiNode.class);



	@Start
	@Override
	public void startNode () {
	}

	@Stop
	@Override
	public void stopNode () {
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		return null;
	
	}

	@Override
	public org.kevoree.framework.PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {

		return null;
	}

}
