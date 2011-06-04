package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

@Library(name = "KevoreeArduinoJava")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "TX_PIN", optional = false, defaultValue = "6"),
        @DictionaryAttribute(name = "RX_PIN", optional = false, defaultValue = "5")
})
public class SRF433 extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle() {
        //NOOP
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {

        context.append("" +
                "#include <VirtualWire.h>\n" +
                "#undef int\n" +
                "#undef abs\n" +
                "#undef double\n" +
                "#undef float\n" +
                "#undef round\n");

    }

    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("vw_set_rx_pin(" + this.getDictionary().get("RX_PIN").toString() + ");\n");
        context.append("vw_set_tx_pin(" + this.getDictionary().get("TX_PIN").toString() + ");\n");
        context.append("vw_set_ptt_pin(-1);");
        context.append("vw_setup(1200);\n");
        context.append("vw_rx_start();\n");

        //TO REMOVE
        context.append("Serial.begin(9600);\n");

    }

    @Generate("loop")
    public void generateLoop(StringBuffer context) {

        context.append("uint8_t buf[VW_MAX_MESSAGE_LEN];\n");
        context.append("uint8_t buflen = VW_MAX_MESSAGE_LEN;\n");

        context.append("if (vw_get_message(buf, &buflen)){\n");

        context.append("int i;\n");
        context.append("char msg[buflen+1];\n");
        context.append("for (i = 0; i < buflen; i++){\n");
        context.append("msg[i] = buf[i];\n");
        context.append("}\n");
        context.append("msg[i]='\\0';\n");

        context.append("String msgString =  String(msg);\n");
        context.append("if(msgString.startsWith(\"" + this.getName() + ":\")){\n");
        context.append("String contentString = String(\"r:\")+ msgString.substring(" + (this.getName().length()+1) + ") ;\n");
        context.append(ArduinoMethodHelper.generateMethodNameChannelDispatch(this.getName()) + "(contentString);\n");
        context.append("}\n");


        context.append("}\n");

    }


    @Override
    public Object dispatch(Message msg) {
        StringBuffer context = (StringBuffer) msg.getContent();
        //DISPATCH LOCALLY


        context.append("if(!param.startsWith(\"r:\")){\n");


        //COPY PARAM TO CHAR*

        context.append("String toSendMsg =  String(\"" + this.getName() + ":\")+param;\n");
        context.append("char msgContent[toSendMsg.length()+10];\n");
        context.append("toSendMsg.toCharArray(msgContent, toSendMsg.length()+1);\n");

        //SEND TO RF 433
        context.append("vw_send((uint8_t *)msgContent, strlen(msgContent));\n");
        context.append("vw_wait_tx();\n");

        context.append("} else {\n");
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(p.getComponentName(), p.getName(), PortUsage.provided()));
            context.append("(param.substring(2));\n");
        }
        context.append("} \n");

        return null;
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {

        return new NoopChannelFragmentSender();

    }

}
