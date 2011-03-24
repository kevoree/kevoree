package org.kevoree.library.sensors;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

@Library(name="KevoreeArduino")
@ComponentType
public class LightSensor extends AbstractComponentType {

    @Start
    public void start(){}

    @Stop
    public void stop(){}

}