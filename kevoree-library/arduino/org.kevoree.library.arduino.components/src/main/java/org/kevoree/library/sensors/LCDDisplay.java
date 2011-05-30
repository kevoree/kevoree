package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
/*
@DictionaryType({
@DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})  */
@Provides({
    @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
public class LCDDisplay extends AbstractComponentType {

    @Start
    @Stop
    public void dummy() {
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <LiquidCrystal.h> \n");
    }

    @Generate("classheader")
    public void generateClassHeader(StringBuffer context) {
        context.append("LiquidCrystal * lcd;\n");
    }

    @Generate("classinit")
    public void generateClassInit(StringBuffer context) {     
        context.append("lcd = (LiquidCrystal*) malloc(sizeof(LiquidCrystal));\n");
        context.append("if (lcd){memset(lcd, 0, sizeof(LiquidCrystal));}");
        context.append("LiquidCrystal lcdObj(10, 11, 12, 13, 14, 15, 16);");
        context.append("memcpy (lcd,&lcdObj,sizeof(LiquidCrystal));");
        context.append("lcd->begin(16, 2);");
    }

    @Port(name = "input")
    public void inputPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("lcd->clear();\n");
        context.append("lcd->print(String(msg->value)+String(\":\")+String(msg->metric));\n");
    }
}
