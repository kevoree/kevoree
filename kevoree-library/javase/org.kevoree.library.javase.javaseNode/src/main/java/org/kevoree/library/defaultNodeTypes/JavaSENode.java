/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ffouquet
 */
@NodeType
@PrimitiveCommands(values = {"UpdateType","UpdateDeployUnit","AddType","AddDeployUnit","AddThirdParty","RemoveType","RemoveDeployUnit","UpdateInstance","UpdateBinding","UpdateDictionaryInstance","AddInstance","RemoveInstance","AddBinding","RemoveBinding","AddFragmentBinding","RemoveFragmentBinding","StartInstance","StopInstance"},value={})
public class JavaSENode extends AbstractNodeType {
    private static final Logger logger = LoggerFactory.getLogger(JavaSENode.class);

    private KevoreeKompareBean kompareBean = null;
    private BaseDeployOSGi deployBean = null;


    @Start
    @Override
    public void startNode() {
        kompareBean = new KevoreeKompareBean();
        deployBean = new BaseDeployOSGi((Bundle)this.getDictionary().get("osgi.bundle"));
    }

    @Stop
    @Override
    public void stopNode() {
        kompareBean = null;
        deployBean = null;
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current,target,this.getNodeName());
    }

    @Override
    public org.kevoree.framework.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return deployBean.buildPrimitiveCommand(adaptationPrimitive,this.getNodeName());
    }

    @Override
    public void push(String targetNodeName, ContainerRoot root) {

        logger.error("JavaSE have no strategy to deploy model !!! ");

    }

}
