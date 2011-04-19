package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;

public class Tester {

    public static void main(String[] args){
               
        System.setProperty("arduino.home", "/Applications/Arduino.app/Contents/Resources/Java");
        System.setProperty("avr.bin","/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/bin");
        System.setProperty("avrdude.config.path", "/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/etc/avrdude.conf");
        System.setProperty("serial.port", "/dev/tty.usbmodem621");
        
        String modelString = "/Users/ffouquet/Desktop/infiniteLoop.kev";

        ContainerRoot model = KevoreeXmiHelper.load(modelString);

        ArduinoNode node = new ArduinoNode();
        node.getDictionary().put("boardTypeName","uno");
        node.getDictionary().put("boardPortName","/dev/tty.usbmodem411");

        node.push("dukeSensor1",model,null);
           
    }

}
