package org.kevoree.library.channels;///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.kevoree.library.channels;
//
//import org.kevoree.annotation.*;
//import org.kevoree.framework.AbstractChannelFragment;
//import org.kevoree.framework.ChannelFragmentSender;
//import org.kevoree.framework.NoopChannelFragmentSender;
//import org.kevoree.framework.message.Message;
//import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
//import org.kevoree.library.arduinoNodeType.PortUsage;
//
///**
// * @author ffouquet
// */
//@Library(name = "KevoreeArduinoJava")
//@ChannelTypeFragment
//@DictionaryType({
//        @DictionaryAttribute(name = "TX_PIN", optional = false, defaultValue = "7"),
//        @DictionaryAttribute(name = "RX_PIN", optional = false, defaultValue = "8")
//})
//public class RF433 extends AbstractChannelFragment {
//
//    @Start
//    @Stop
//    public void lifeCycle() {
//        //NOOP
//    }
//
//    @Generate("header")
//    public void generateHeader(StringBuffer context) {
//        context.append("#include <VirtualWire.h>\n");
//        context.append("#undef int\n");
//        context.append("#undef abs\n");
//        context.append("#undef double\n");
//        context.append("#undef float\n");
//        context.append("#undef round\n");
//        context.append("#include <aJSON.h>\n");
//        context.append("#include <avr/pgmspace.h>\n");
//
//    }
//
//    @Generate("setup")
//    public void generateSetup(StringBuffer context) {
//        context.append("vw_set_rx_pin(" + this.getDictionary().get("RX_PIN").toString() + ");\n");
//        context.append("vw_set_tx_pin(" + this.getDictionary().get("TX_PIN").toString() + ");\n");
//        context.append("vw_setup(1200);\n");
//        context.append("vw_rx_start();\n");
//
//    }
//
//    @Generate("loop")
//    public void generateLoop(StringBuffer context) {
//
//        context.append("uint8_t buf[VW_MAX_MESSAGE_LEN];\n");
//        context.append("uint8_t buflen = VW_MAX_MESSAGE_LEN;\n");
//
//        context.append("if (vw_get_message(buf, &buflen)){\n");
//            context.append("int i;\n");
//            context.append("char msg[buflen+10];\n");
//            context.append("for (i = 0; i < buflen; i++){\n");
//                context.append("msg[i] = buf[i];\n");
//            context.append("}\n");
//
//            context.append("aJsonObject* jsonObject = aJson.parse(msg);\n");
//
//            context.append("aJsonObject* nodeSender = aJson.getObjectItem(jsonObject, \"nodeSender\");\n");
//
//            context.append("String nodeSenderString = String(nodeSender->valuestring) ;\n");
//            //CHECK NODE SENDER 8 FROM US
//            context.append("if(!nodeSenderString.compareTo(\""+this.getNodeName()+"\")){\n");
//                //CHECK CHANNEL NAME == US
//                context.append("aJsonObject* channelName = aJson.getObjectItem(jsonObject, \"channelName\");\n");
//                context.append("String channelNameString = String(channelName->valuestring) ;\n");
//                context.append("if(!channelNameString.compareTo(\""+this.getName()+"\")){\n");
//                    //BUILD CONTENT
//                    context.append("aJsonObject* content = aJson.getObjectItem(jsonObject, \"content\");\n");
//                    context.append("String contentString = String(content->valuestring) ;\n");
//                    context.append(ArduinoMethodHelper.generateMethodNameChannelDispatch(this.getName()) + "(contentString);\n");
//
//                context.append("}\n");
//            context.append("}\n");
//
//            context.append("aJson.deleteItem(jsonObject);\n");
//
//        context.append("}\n");
//
//    }
//
//
//    @Override
//    public Object dispatch(Message msg) {
//        StringBuffer context = (StringBuffer) msg.getContent();
//        //DISPATCH LOCALLY
//        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
//            context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(p.getComponentName(), p.getName(), PortUsage.provided()));
//            context.append("(param);\n");
//        }
//
//        //SERIALIZE MESSAGE
//        context.append("aJsonObject* root = aJson.createObject();\n");
//        //COPY PARAM TO CHAR*
//        context.append("char msgContent[param.length()+10];\n");
//        context.append("param.toCharArray(msgContent, param.length()+1);\n");
//        //ADD TO JSON OBJECT
//        context.append("if (root != NULL) {\n");
//        context.append("aJson.addStringToObject(root, \"nodeSender\", \"replaceNodeName\");\n");
//        context.append("aJson.addStringToObject(root, \"channelName\", \"replaceInstanceName\");\n");
//        context.append("aJson.addStringToObject(root, \"content\", msgContent);\n");
//        context.append("}\n");
//        //PRINT TO RESULT to CHAR *
//        context.append("char *json_String=aJson.print(root);\n");
//        //SEND TO RF 433
//        context.append("vw_send((uint8_t *)json_String, strlen(json_String));\n");
//        context.append("vw_wait_tx();\n");
//
//        context.append("aJson.deleteItem(root);\n");
//        return null;
//    }
//
//    @Override
//    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
//
//        return new NoopChannelFragmentSender();
//
//    }
//}
