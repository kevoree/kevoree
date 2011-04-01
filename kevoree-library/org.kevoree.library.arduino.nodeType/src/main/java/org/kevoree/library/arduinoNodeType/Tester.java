package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;

public class Tester {

    public static void main(String[] args){
               
        String modelString = "/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-library/org.kevoree.library.arduino.nodeType/src/test/resources/models/TempSensorAlone.kev";

        ContainerRoot model = KevoreeXmiHelper.load(modelString);

        ArduinoNode node = new ArduinoNode();

        node.push("dukeSensor1",model);
           


    }

}
