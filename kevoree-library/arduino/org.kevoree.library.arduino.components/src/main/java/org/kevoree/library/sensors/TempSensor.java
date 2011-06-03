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
    @Stop
    public void dummy() {
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <math.h> \n");
    }

    @Generate("classheader")
    public void generateClassHeader(StringBuffer context) {
        context.append("float vcc;\n");
        context.append("float pad;\n");
        context.append("float thermr;\n");
    }

    @Generate("classinit")
    public void generateClassInit(StringBuffer context) {
        context.append("vcc = 5.05;\n");
        context.append("pad = 9850;\n");
        context.append("thermr = 10000;\n");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("    long Resistance;  \n"
                + "    float temp;  // Dual-Purpose variable to save space.\n"
                + "    int RawADC = analogRead(atoi(pin));\n"
                + "    Resistance=((1024 * thermr / RawADC) - pad); \n"
                + "    temp = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later\n"
                + "    temp = 1 / (0.001129148 + (0.000234125 * temp) + (0.0000000876741 * temp * temp * temp));\n"
                + "    temp = temp - 273.15;  // Convert Kelvin to Celsius  \n"
                + "    //send to output port\n");


        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        context.append("char buf[255];\n");
        context.append("sprintf(buf,\"%d\",int(temp));\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"c\";");
        context.append("temp_rport(smsg);");
        context.append("free(smsg);");


    }
}
