package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})
@Provides({
    @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "temp", type = PortType.MESSAGE)
})
public class TempSensor extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <math.h> \n");
        context.append("#include <Metro.h> \n");

        context.append("#define ThermistorPIN ");
        context.append(this.getDictionary().get("pin"));
        context.append(" \n");

        //GENERATE DEFAULT VALUES
        context.append("float vcc = 4.91;\n");
        context.append("float pad = 9850;\n");
        context.append("float thermr = 10000;\n");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        
        /* Generate code for port */

        context.append("    long Resistance;  \n" +
                "    float temp;  // Dual-Purpose variable to save space.\n" +
                "    int RawADC = analogRead(ThermistorPIN);\n" +
                "    Resistance=((1024 * thermr / RawADC) - pad); \n" +
                "    temp = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later\n" +
                "    temp = 1 / (0.001129148 + (0.000234125 * temp) + (0.0000000876741 * temp * temp * temp));\n" +
                "    temp = temp - 273.15;  // Convert Kelvin to Celsius  \n" +
                "    //send to output port\n" +
                "    String result = String(int(temp*100));");


          //TODO DO CALL TO OUTPUT PORT

        //context.append("tempOut(Temp+\"\");\n");

    }



    /* ARDUINO CODE */


}
