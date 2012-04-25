package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
/*
@DictionaryType({
@DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})  */
@Provides({
    @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
public class LCDDisplay extends AbstractArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("#include <LiquidCrystal.h> \n");
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("LiquidCrystal * lcd;\n");
    }

    @Override
    public void generateInit(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("lcd = (LiquidCrystal*) malloc(sizeof(LiquidCrystal));\n");
        getGenerator().appendNativeStatement("if (lcd){memset(lcd, 0, sizeof(LiquidCrystal));}");
        getGenerator().appendNativeStatement("LiquidCrystal lcdObj(10, 11, 12, 13, 14, 15, 16);");
        getGenerator().appendNativeStatement("memcpy (lcd,&lcdObj,sizeof(LiquidCrystal));");
        getGenerator().appendNativeStatement("lcd->begin(16, 2);");
    }

    @Port(name = "input")
    public void inputPort(Object o) {
        getGenerator().appendNativeStatement("lcd->clear();\n");
        getGenerator().appendNativeStatement("lcd->print(String(msg->value)+String(\":\")+String(msg->metric));\n");
    }
}
