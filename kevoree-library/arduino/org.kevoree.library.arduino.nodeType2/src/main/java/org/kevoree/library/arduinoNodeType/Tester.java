package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;

public class Tester {

    public static void main(String[] args){
               
        System.setProperty("arduino.home", "/Applications/Arduino.app/Contents/Resources/Java");
        System.setProperty("avr.bin","/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/bin");
        System.setProperty("avrdude.config.path", "/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/etc/avrdude.conf");
        System.setProperty("serial.port", "/dev/tty.usbmodem621");
        
        String modelString = "/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-library/arduino/org.kevoree.library.arduino.nodeType2/src/test/resources/models/LEDSimple.kev";

        ContainerRoot model = KevoreeXmiHelper.load(modelString);

        ArduinoNode node = new ArduinoNode();
        node.getDictionary().put("boardTypeName","atmega328");
        node.getDictionary().put("boardPortName","/dev/tty.usbserial-A400g2AP");

        node.push("KEVOREEDefaultNodeName",model,null);
           
    }

}
