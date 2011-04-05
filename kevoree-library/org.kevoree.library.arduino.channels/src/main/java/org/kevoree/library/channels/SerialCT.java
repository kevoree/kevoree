/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

/**
 *
 * @author ffouquet
 */
@Library(name = "KevoreeArduinoJava")
@ChannelTypeFragment
public class SerialCT extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle() {
    }

    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("Serial.begin(9600);\n");
    }

    @Override
    public Object dispatch(Message msg) {
        StringBuffer context = (StringBuffer) msg.getContent();
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(p.getComponentName(), p.getName(), PortUsage.provided()));
            context.append("(param);\n");
        }
       // if (getOtherFragments().size() > 0) {
            context.append("Serial.println(param);\n");
       // }
        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
