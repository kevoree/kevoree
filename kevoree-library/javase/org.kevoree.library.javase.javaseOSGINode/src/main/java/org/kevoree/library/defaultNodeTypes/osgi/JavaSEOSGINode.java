/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes.osgi;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.framework.context.KevoreeDeployManager;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@NodeType
@PrimitiveCommands(
        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty"}, value = {})
public class JavaSEOSGINode extends AbstractNodeType {
    private static final Logger logger = LoggerFactory.getLogger(JavaSEOSGINode.class);

    private KevoreeKompareBean kompareBean = null;
    private BaseDeployOSGi deployBean = null;

    @Start
    @Override
    public void startNode() {
     //   Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
       // KevoreeDeployManager.setBundle(bundle);
        kompareBean = new KevoreeKompareBean();
        deployBean = new BaseDeployOSGi(bundle);
        deployBean.setModelHandlerService(getModelService());
        deployBean.setKscripEngineFactory(getKevScriptEngineFactory());
    }


    @Stop
    @Override
    public void stopNode() {
        kompareBean = null;
        deployBean = null;
        //Cleanup the local runtime
        KevoreeDeployManager.clearAll();
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current, target, this.getNodeName());
    }

    @Override
    public org.kevoree.framework.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return deployBean.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
    }

}
