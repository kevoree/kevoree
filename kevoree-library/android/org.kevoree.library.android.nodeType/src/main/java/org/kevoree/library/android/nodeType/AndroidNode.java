package org.kevoree.library.android.nodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.annotation.*;
import org.kevoree.annotation.NodeType;
import org.kevoree.framework.*;
import org.kevoree.framework.context.KevoreeDeployManager;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: ffouquet
 * Date: 15/09/11
 * Time: 17:10
 */

@Library(name = "Android")
@NodeType
@PrimitiveCommands(
        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty"},
        value = {})
public class AndroidNode extends AbstractNodeType {

    private static final Logger logger = LoggerFactory.getLogger(AndroidNode.class);
    private KevoreeKompareBean kompareBean = null;
    private BaseDeployOSGi deployBean = null;

    @Start
    @Override
    public void startNode() {
        Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        KevoreeDeployManager.setBundle(bundle);
        kompareBean = new KevoreeKompareBean();
        deployBean = new BaseDeployOSGi(bundle);
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
