package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;


@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5"})
})
@Provides({
        @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "temp", type = PortType.MESSAGE, needCheckDependency = false)
})
public class TempSensor extends AbstractArduinoComponent {
    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("#include <math.h> \n");
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("float vcc;");
        gen.appendNativeStatement("float pad;");
        gen.appendNativeStatement("float thermr;");
        gen.appendNativeStatement("float tempValue;");
        gen.appendNativeStatement("char buf[10];");
    }

    @Override
    public void generateInit(ArduinoGenerator gen) {
        gen.appendNativeStatement("vcc = 5.05;\n");
        gen.appendNativeStatement("pad = 9850;\n");
        gen.appendNativeStatement("thermr = 10000;\n");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("    long Resistance;  \n"
                + "    int RawADC = analogRead(atoi(pin));\n"
                + "    Resistance=((1024 * thermr / RawADC) - pad); \n"
                + "    tempValue = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later\n"
                + "    tempValue = 1 / (0.001129148 + (0.000234125 * tempValue) + (0.0000000876741 * tempValue * tempValue * tempValue));\n"
                + "    tempValue = tempValue - 273.15;  // Convert Kelvin to Celsius  \n"
                + "    //send to output port\n");


        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        context.append("sprintf(buf,\"%d\",int(tempValue));\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"c\";");
        context.append("temp_rport(smsg);");
        context.append("free(smsg);");
    }
}
