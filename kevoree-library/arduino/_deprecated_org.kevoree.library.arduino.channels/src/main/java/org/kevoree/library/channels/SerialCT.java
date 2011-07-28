/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.channels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.Message;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

/**
 * @author ffouquet
 */
@Library(name = "KevoreeArduinoJava")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "PORT", optional = false, defaultValue = "/dev/ttyS0")
})
public class SerialCT extends AbstractChannelFragment {

    @Start
    @Stop
    public void lifeCycle() {
    }


    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#define BUFFERSIZE 100\n");
        context.append("char inBytes[BUFFERSIZE];\n");
        context.append("int serialIndex = 0;\n");
    }

    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("Serial.begin(9600);\n");
    }

    @Generate("loop")
    public void generateLoop(StringBuffer context) {
        context.append("" +
                "while(Serial.available() && serialIndex < BUFFERSIZE) {\n" +
                "    inBytes[serialIndex] = Serial.read();   \n" +
                "    if (inBytes[serialIndex] == '\\n' || inBytes[serialIndex] == ';' || inBytes[serialIndex] == '>') { //Use ; when using Serial Monitor\n" +
                "       inBytes[serialIndex] = '\\0'; //end of string char\n" +
                "       String result = String(inBytes);\n" +ArduinoMethodHelper.generateMethodNameChannelDispatch(this.getName())+"(result);\n" +
                "       serialIndex = 0;\n" +
                "    }\n" +
                "    else{\n" +
                "      serialIndex++;\n" +
                "    }\n" +
                "  }\n" +
                "  \n" +
                "  if(serialIndex >= BUFFERSIZE){\n" +
                "    //buffer overflow, reset the buffer and do nothing\n" +
                "    //TODO: perhaps some sort of feedback to the user?\n" +
                "    for(int j=0; j < BUFFERSIZE; j++){\n" +
                "      inBytes[j] = 0;\n" +
                "      serialIndex = 0;\n" +
                "    }\n" +
                "  }\n");
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

        return new NoopChannelFragmentSender();

    }
}
