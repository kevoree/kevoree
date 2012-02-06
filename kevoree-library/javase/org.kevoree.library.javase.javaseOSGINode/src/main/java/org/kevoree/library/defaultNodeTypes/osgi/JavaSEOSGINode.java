/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes.osgi;

import org.apache.felix.framework.Felix;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager;
import org.kevoree.library.defaultNodeTypes.osgi.deploy.BaseDeployOSGi;
import org.kevoree.library.defaultNodeTypes.osgi.deploy.OSGIKevoreeDeployManager;
import org.kevoree.library.defaultNodeTypes.osgi.deploy.runtime.EmbeddedFelix;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@NodeType
@PrimitiveCommands(
        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty","RemoveThirdParty"}, value = {})
public class JavaSEOSGINode extends JavaSENode {
    private static final Logger logger = LoggerFactory.getLogger(JavaSEOSGINode.class);

    private KevoreeKompareBean kompareBean = null;
    private Felix fwk = null;
    private BaseDeployOSGi deployBean = null;

    @Override
    public void startNode() {
        EmbeddedFelix emFelix = new EmbeddedFelix();
        emFelix.run();
        fwk = emFelix.getM_fwk();
        OSGIKevoreeDeployManager.setBundle(fwk);
        kompareBean = new KevoreeKompareBean();
        deployBean = new BaseDeployOSGi(fwk,this);
    }

    @Override
    public void stopNode() {
        kompareBean = null;
        deployBean = null;
        //Cleanup the local runtime
        OSGIKevoreeDeployManager.clearAll();
        KevoreeDeployManager.clearAll(this);

        try {
            fwk.stop();
        } catch (BundleException e) {
            logger.debug("Error while stopping node ",e);
        }
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current, target, this.getNodeName());
    }

    @Override
    public org.kevoree.api.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return deployBean.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
    }

}
