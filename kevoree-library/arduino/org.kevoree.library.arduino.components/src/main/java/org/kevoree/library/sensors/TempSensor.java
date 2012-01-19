package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;


@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5"})
})
@Requires({
        @RequiredPort(name = "temp", type = PortType.MESSAGE, needCheckDependency = false)
})
public class TempSensor extends AbstractPeriodicArduinoComponent {
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

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        getGenerator().appendNativeStatement("    long Resistance;  \n"
                + "    int RawADC = analogRead(atoi(pin));\n"
                + "    Resistance=((1024 * thermr / RawADC) - pad); \n"
                + "    tempValue = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later\n"
                + "    tempValue = 1 / (0.001129148 + (0.000234125 * tempValue) + (0.0000000876741 * tempValue * tempValue * tempValue));\n"
                + "    tempValue = tempValue - 273.15;  // Convert Kelvin to Celsius  \n"
                + "    //send to output port\n");


        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",int(tempValue));\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"c\";");
        getGenerator().appendNativeStatement("temp_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");
    }
}
