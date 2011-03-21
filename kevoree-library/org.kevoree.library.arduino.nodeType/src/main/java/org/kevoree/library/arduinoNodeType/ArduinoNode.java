package org.kevoree.library.arduinoNodeType;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.framework.AbstractNodeType;

@NodeType
@Library(name="KevoreeNodeType")
public class ArduinoNode extends AbstractNodeType {

    @Override
    public void deploy(String string, ContainerRoot cr) {
        System.out.println("I'm the arduino deployer");

        //STEP 0 : FOUND ARDUINO COMMUNICATION CHANNEL

        //STEP 1 : GENERATE FLAT CODE - MODEL SPECIFIQUE

        //STEP 2 : COMPILE to PDE Target

        //STEP 3 : Deploy by commnication channel



    }


}
