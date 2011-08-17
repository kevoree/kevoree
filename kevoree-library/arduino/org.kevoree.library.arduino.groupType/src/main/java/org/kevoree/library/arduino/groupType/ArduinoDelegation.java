package org.kevoree.library.arduino.groupType;

import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.AbstractGroupType;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 11:07
 */
@Library(name = "Kevoree-Arduino-JavaSE")
@GroupType
public class ArduinoDelegation extends AbstractGroupType {

    protected ServiceReference sr;
    protected KevoreeModelHandlerService modelHandlerService = null;
    protected Logger logger = LoggerFactory.getLogger(ArduinoDelegation.class);

    ArduinoDelegationPush delegationPush = null;

    @Start
    public void startGroupDelegation() {
        Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
        modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);
        delegationPush = new ArduinoDelegationPush(modelHandlerService, this.getName(), bundle);
        //triggerModelUpdate();
    }

    @Stop
    public void stopGroupDelegation() {
        if (modelHandlerService != null) {
            Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
            if (bundle != null) {
                if (bundle.getBundleContext() != null) {
                    bundle.getBundleContext().ungetService(sr);
                    modelHandlerService = null;
                }
            }
        }
    }

    @Override
    public void triggerModelUpdate() {
        new Thread() {
            @Override
            public void run() {
                delegationPush.deploy();
            }
        }.start();
    }
}
