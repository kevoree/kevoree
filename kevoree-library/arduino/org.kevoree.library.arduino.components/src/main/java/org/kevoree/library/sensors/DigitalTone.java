package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "freq", dataType = java.lang.Integer.class,defaultValue = "500", optional = true)
})
@Provides({
        @ProvidedPort(name = "on", type = PortType.MESSAGE),
        @ProvidedPort(name = "off", type = PortType.MESSAGE),
        @ProvidedPort(name = "toggle", type = PortType.MESSAGE)
})
public class DigitalTone extends AbstractArduinoComponent {


    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("boolean state ;");
    }


    @Port(name = "on")
    public void triggerOn(Object gen) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");
        getGenerator().appendNativeStatement("tone(atoi(pin), freq);\n");
    }

    @Port(name = "off")
    public void triggerOff(Object gen) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");
        getGenerator().appendNativeStatement("noTone(atoi(pin));\n");
    }

    @Port(name = "toggle")
    public void triggerToggle(Object gen) {
        getGenerator().appendNativeStatement("int newState = 0;\n");
        getGenerator().appendNativeStatement("if(state){ newState = LOW; } else { newState=HIGH; }");
        getGenerator().appendNativeStatement("state = ! state; ");
        getGenerator().appendNativeStatement("pinMode(atoi(pin), OUTPUT);");

        getGenerator().appendNativeStatement("if(state){");
        getGenerator().appendNativeStatement("tone(atoi(pin), freq);\n");
        getGenerator().appendNativeStatement("} else {");
        getGenerator().appendNativeStatement("noTone(atoi(pin));\n");
        getGenerator().appendNativeStatement("}");
    }
}
