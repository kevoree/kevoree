package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true,vals={"0","1","2","3","4","5","6","7","8","9","10","11","12","13"})
})
@Provides({
    @ProvidedPort(name = "on", type = PortType.MESSAGE),
    @ProvidedPort(name = "off", type = PortType.MESSAGE),
    @ProvidedPort(name = "toggle", type = PortType.MESSAGE),
    @ProvidedPort(name = "flash", type = PortType.MESSAGE)
})
public class DigitalLight extends AbstractArduinoComponent {


    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("boolean state ;");
    }


    @Port(name = "on")
    public void triggerOn(Object gen) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");
        getGenerator().appendNativeStatement("digitalWrite(atoi(pin), HIGH);\n");
    }

    @Port(name = "off")
    public void triggerOff(Object gen) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");
        getGenerator().appendNativeStatement("digitalWrite(atoi(pin), LOW);\n");
    }

    @Port(name = "toggle")
    public void triggerToggle(Object gen) {
        getGenerator().appendNativeStatement("int newState = 0;\n");
        getGenerator().appendNativeStatement("if(state){ newState = LOW; } else { newState=HIGH; }");
        getGenerator().appendNativeStatement("state = ! state; ");
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");
        getGenerator().appendNativeStatement("digitalWrite(atoi(pin), newState);\n");
    }

    @Port(name = "flash")
    public void triggerflashled(Object gen) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");
        getGenerator().appendNativeStatement("digitalWrite(atoi(pin), HIGH);\n");
        getGenerator().appendNativeStatement("delay(80);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(pin), LOW);\n");
    }
}
