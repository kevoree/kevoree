package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})

@Provides({
        @ProvidedPort(name = "on", type = PortType.MESSAGE),
        @ProvidedPort(name = "off", type = PortType.MESSAGE)
})
public class LED extends AbstractComponentType {

    @Start
    public void start() {
         //context.append("pinMode(atoi(pin), OUTPUT);");
    }

    @Stop
    public void stop() {}

    @Port(name = "on")
    public void triggerOn(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("pinMode(atoi(pin), OUTPUT);");
        context.append("digitalWrite(atoi(pin), HIGH);\n");
    }

    @Port(name = "off")
    public void triggerOff(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("pinMode(atoi(pin), OUTPUT);");
        context.append("digitalWrite(atoi(pin), LOW);\n");
    }

}
