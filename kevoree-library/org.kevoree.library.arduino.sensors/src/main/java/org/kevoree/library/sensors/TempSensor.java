package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})
@ProvidedPort(name = "trigger", type = PortType.MESSAGE)
public class TempSensor extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {
    }

    @Generate("header")
    public void generateHeader(StringBuilder context) {
        context.append("#include <math.h> \n");
        context.append("#define ThermistorPIN ");
        context.append(this.getDictionary().get("pin"));
        context.append(" \n");

        //GENERATE DEFAULT VALUES
        context.append("float vcc = 4.91;\n");
        context.append("float pad = 9850;\n");
        context.append("float thermr = 10000;\n");
    }
    /*
@Generate("globalVar")
public void generateGlobalVar(StringBuilder context){

}       */

    @Port(name = "trigger")
    public void triggerPort(Object o) {

        /* Generate code for port */
        StringBuilder context = new StringBuilder();
        context.append("long Resistance; \n");
        context.append("float Temp; \n");
        context.append("int RawADC; \n");

        context.append("RawADC = analogRead(ThermistorPIN); \n");
        context.append(
                "Resistance=((1024 * thermr / RawADC) - pad); \n" +
                        "Temp = log(Resistance);\n" +
                        "Temp = 1 / (0.001129148 + (0.000234125 * Temp) + (0.0000000876741 * Temp * Temp * Temp));\n" +
                        "Temp = Temp - 273.15;\n");

        //GENERATE REQUIRED PORT CALL
        context.append("tempOut(Temp+\"\");\n");

    }


    /* ARDUINO CODE */


}
