package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.arduinoNodeType.utils.ArduinoHomeFinder;
import org.kevoree.library.arduinoNodeType.utils.ExecutableFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Tester {

    public static void main(String[] args){

        ArduinoHomeFinder.checkArduinoHome();

        //System.out.println(ArduinoToolChainExecutables.getAVR_GCC());

        //System.setProperty("arduino.home", "/Applications/Arduino.app/Contents/Resources/Java");
        //System.setProperty("avr.bin","/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/bin");
        //System.setProperty("avrdude.config.path", "/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/etc/avrdude.conf");
       // System.setProperty("serial.port", "/dev/tty.usbmodem621");


        String modelString = "/Users/ffouquet/Desktop/ksensor1_1.kev";

        ContainerRoot model = KevoreeXmiHelper.load(modelString);

        ArduinoNode node = new ArduinoNode();
        node.getDictionary().put("boardTypeName","atmega328");
        node.getDictionary().put("boardPortName","/dev/tty.usbserial-A400g2se");
        node.getDictionary().put("pmem","sd");
       // node.getDictionary().put("boardPortName","/dev/tty.usbserial-A400g2se");


        node.getDictionary().put("incremental","false");
        node.push("ksensor1",model,null);

    }

}
