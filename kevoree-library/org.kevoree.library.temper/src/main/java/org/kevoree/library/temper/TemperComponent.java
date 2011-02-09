package org.kevoree.library.temper;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "Kevoree-Lib")
@ComponentType
public class TemperComponent extends AbstractComponentType {

    @Start
    public void startHello() {
        System.out.println("Hello Channel");
    }

    @Stop
    public void stopHello() {
        System.out.println("Bye Channel");
    }
}
