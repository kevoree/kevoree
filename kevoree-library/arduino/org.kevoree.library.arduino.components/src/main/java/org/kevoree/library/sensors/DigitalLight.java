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
    @ProvidedPort(name = "off", type = PortType.MESSAGE),
    @ProvidedPort(name = "toggle", type = PortType.MESSAGE)
})
public class DigitalLight extends AbstractComponentType {

    @Generate("classheader")
    public void generatePeriodic(StringBuffer context) {
        context.append("boolean state ;\n");
    }

    @Start
    @Stop
    public void dummy() {
    }

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

    @Port(name = "toggle")
    public void triggerToggle(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("int newState = 0;\n");
        context.append("if(state){ newState = LOW; } else { newState=HIGH; }");
        context.append("state = ! state; ");
        context.append("pinMode(atoi(pin), OUTPUT);");
        context.append("digitalWrite(atoi(pin), newState);\n");
    }
}
