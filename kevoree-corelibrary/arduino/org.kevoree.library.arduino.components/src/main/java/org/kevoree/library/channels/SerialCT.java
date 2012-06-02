package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;


/**
 * User: ffouquet
 * Date: 08/06/11
 * Time: 08:57
 */
@Library(name = "Arduino")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "serialport", fragmentDependant = true, optional = true, defaultValue = "*")
})
public class SerialCT extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle() {
    }

    @Override
    public Object dispatch(Message msg) {

        StringBuffer context = (StringBuffer) msg.getContent();
        context.append("for(int i=0;i<bindings->nbBindings;i++){");
        context.append("    bindings->bindings[i]->port->push(*msg);");
        context.append("}");
        //SEND BASICALLY TO SERIAL

        context.append("Serial.print(instanceName);");
        context.append("Serial.print(\":\");");
        context.append("Serial.print(\"");
        context.append(this.getNodeName());
        context.append("\");");
        //context.append("Serial.print(\""+this.getNodeName()+");");
        context.append("Serial.print(\"[\");");
        context.append("Serial.print(msg->metric);");
        context.append("Serial.print(\"/\");");
        context.append("Serial.print(msg->value);");
        context.append("Serial.println(\"]\");");
        return null;
    }


    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
