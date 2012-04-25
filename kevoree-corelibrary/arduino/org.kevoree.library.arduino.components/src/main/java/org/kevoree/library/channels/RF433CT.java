package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;


/**
 * User: ffouquet
 * Date: 08/06/11
 * Time: 08:57
 */
@Library(name = "Arduino")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "rxpin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "txpin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"})
})
public class RF433CT extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle() {
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <VirtualWire.h>\n");
    }

    @Generate("classheader")
    public void generateClassHeader(StringBuffer context) {

    }

    @Generate("classinit")
    public void generateClassInit(StringBuffer context) {
        context.append("vw_set_rx_pin(" + this.getDictionary().get("rxpin").toString() + ");\n");
        context.append("vw_set_tx_pin(" + this.getDictionary().get("txpin").toString() + ");\n");
        context.append("vw_setup(1200);\n");
        context.append("vw_rx_start();\n");
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
