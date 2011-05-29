
package org.kevoree.library.sensors;

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
public class LocalChannel extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle(){}
    
    @Override
    public Object dispatch(Message msg) {
        
        StringBuffer context = (StringBuffer) msg.getContent();
        context.append("for(int i=0;i<bindings->nbBindings;i++){");
        context.append("    bindings->bindings[i]->port->push(*msg);");
        context.append("}");
        return null;
    }

    
    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}