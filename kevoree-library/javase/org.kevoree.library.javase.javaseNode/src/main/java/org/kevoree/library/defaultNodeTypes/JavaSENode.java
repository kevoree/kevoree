/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.CommandMapper;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@NodeType
@PrimitiveCommands(
        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty","RemoveThirdParty"}, value = {})
public class JavaSENode extends AbstractNodeType {
    private static final Logger logger = LoggerFactory.getLogger(JavaSENode.class);

    private KevoreeKompareBean kompareBean = null;
    private CommandMapper mapper = null;

    @Start
    @Override
    public void startNode() {
        kompareBean = new KevoreeKompareBean();
        mapper = new CommandMapper();
        mapper.setNodeType(this);
    }


    @Stop
    @Override
    public void stopNode() {
        kompareBean = null;
        mapper = null;
        //Cleanup the local runtime
        KevoreeDeployManager.clearAll(this);
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current, target, this.getNodeName());
    }

    @Override
    public org.kevoree.api.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return mapper.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
    }

}
