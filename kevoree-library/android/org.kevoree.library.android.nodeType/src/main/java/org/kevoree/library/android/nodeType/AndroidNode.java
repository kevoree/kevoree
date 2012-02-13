package org.kevoree.library.android.nodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.android.nodeType.deploy.CommandMapper;
import org.kevoree.library.android.nodeType.deploy.context.KevoreeDeployManager;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
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
        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty", "RemoveThirdParty"}, value = {})
public class AndroidNode extends AbstractNodeType {

    private static final Logger logger = LoggerFactory.getLogger(AndroidNode.class);

        private KevoreeKompareBean kompareBean = null;
        private CommandMapper mapper = null;
    	private boolean isRunning;

        @Start
        @Override
        public void startNode() {
    		isRunning = true;
            kompareBean = new KevoreeKompareBean();
            mapper = new CommandMapper();
            mapper.setNodeType(this);
        }


        @Stop
        @Override
        public void stopNode() {
            kompareBean = null;
            mapper = null;
    		isRunning = false;
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
