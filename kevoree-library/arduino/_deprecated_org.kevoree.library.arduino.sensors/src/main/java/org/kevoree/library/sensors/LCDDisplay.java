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
    public void stub() {
    }


    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <LiquidCrystal.h> \n");
        context.append("LiquidCrystal lcd(10, 11, 12, 13, 14, 15, 16);\n");
    }

    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("lcd.begin(16, 2);\n");
    }


    @Port(name = "input")
    public void inputPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("lcd.clear();\n");
        context.append("lcd.print(param);\n");
    }


}
