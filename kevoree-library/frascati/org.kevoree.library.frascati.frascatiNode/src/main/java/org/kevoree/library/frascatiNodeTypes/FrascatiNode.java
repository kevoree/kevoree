/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.frascatiNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.ow2.frascati.FraSCAti;
import org.ow2.frascati.util.FrascatiClassLoader;
import org.ow2.frascati.util.FrascatiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author obrais
 */
@Library(name="Frascati")
@NodeType
public class FrascatiNode extends JavaSENode {
	private static final Logger logger = LoggerFactory.getLogger(FrascatiNode.class);


	FraSCAti frascati;

	@Start
	@Override
	public void startNode () {
		super.startNode();
		try {
		frascati = FraSCAti.newFraSCAti();
		 org.ow2.frascati.util.FrascatiClassLoader f = new FrascatiClassLoader(Thread.currentThread().getContextClassLoader());
		 frascati.setClassLoader(f );
		} catch (FrascatiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Stop
	@Override
	public void stopNode () {
		super.stopNode();
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		return super.kompare(current, target);
	
	}

	@Override
	public org.kevoree.framework.PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return null;
			
	}

}
