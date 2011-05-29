/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;

/**
 *
 * @author ffouquet
 */
@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "period", defaultValue = "1000", optional = true)
})
@Requires({
    @RequiredPort(name = "tick", type = PortType.MESSAGE)
})
public class Timer extends AbstractComponentType {

    @Start
    public void start() {}

    @Stop
    public void stop() {}

    @Generate("periodic")
    public void generatePeriodic(StringBuffer context) {
        context.append("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (msg){memset(msg, 0, sizeof(kmessage));}");  
        context.append("tick_rport(msg);");  
        context.append("free(msg);");
    }

}
