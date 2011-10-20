package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;

import java.io.IOException;

public class Tester {

    public static void main(String[] args) throws IOException {

//        ArduinoHomeFinder.checkArduinoHome();

        //System.out.println(ArduinoToolChainExecutables.getAVR_GCC());

        //System.setProperty("arduino.home", "/Applications/Arduino.app/Contents/Resources/Java");
        //System.setProperty("avr.bin","/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/bin");
        //System.setProperty("avrdude.config.path", "/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/etc/avrdude.conf");
       // System.setProperty("serial.port", "/dev/tty.usbmodem621");


        String modelString = "/Users/duke/Desktop/drop.kev";

        ContainerRoot model = KevoreeXmiHelper.load(modelString);

        ArduinoNode node = new ArduinoNode();
        node.getDictionary().put("boardTypeName","uno");
        //node.getDictionary().put("boardPortName","/dev/tty.usbserial-A400g2se");
//        node.getDictionary().put("pmem","EEPROM");
//        node.getDictionary().put("psize","16384");
       // node.getDictionary().put("boardPortName","/dev/tty.usbserial-A400g2se");


        node.getDictionary().put("incremental","false");
        node.push("node",model, "/dev/tty.usbmodem411");

    }

}
