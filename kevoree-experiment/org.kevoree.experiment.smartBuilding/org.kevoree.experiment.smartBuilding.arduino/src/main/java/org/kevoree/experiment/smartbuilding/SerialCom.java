/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.experiment.smartbuilding;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;

/**
 *
 * @author ffouquet
 */
@Library(name = "SmartBuilding")
@ChannelTypeFragment
public class SerialCom extends AbstractChannelFragment {

    @Override
    public Object dispatch(Message msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Start
    @Stop
    public void lifeCycle() {
    }
}
