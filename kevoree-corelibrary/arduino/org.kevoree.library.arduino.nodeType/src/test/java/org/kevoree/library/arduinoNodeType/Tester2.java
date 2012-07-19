package org.kevoree.library.arduinoNodeType;


import org.kevoree.ContainerRoot;
import org.kevoree.extra.kserial.SerialPort.*;
import org.kevoree.extra.kserial.jna.NativeLoader;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper;

import java.io.IOException;

public class Tester2 {

    public static void main(String[] args) throws Exception {



        try {
            ContainerRoot   model = KevoreeXmiHelper.load("/home/jed/testWireless") ;

             SerialPort serial = new SerialPort("*", 19200);

            serial.open();




            serial = null;




            ArduinoWirelessNode node = new ArduinoWirelessNode();
            node.setNodeName("node0");

            //FOR TEST
            NodeTypeBootstrapHelper bs = new NodeTypeBootstrapHelper();
            node.setBootStrapperService(bs);

            node.getDictionary().put("boardTypeName", "uno");

            node.getDictionary().put("incremental", "false");
            node.startNode();
            node.push("node0", model, "*");

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
