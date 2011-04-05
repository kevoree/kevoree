/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.channels;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

/**
 *
 * @author ffouquet
 */
@Library(name="KevoreeArduino")
@ChannelTypeFragment
public class LocalMsgArduino extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle(){}
    
    @Override
    public Object dispatch(Message msg) {
        StringBuffer context = (StringBuffer) msg.getContent();
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(p.getComponentName(), p.getName(), PortUsage.provided()));
            context.append("(param);\n");
        }
        return null;
    }

    
    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
