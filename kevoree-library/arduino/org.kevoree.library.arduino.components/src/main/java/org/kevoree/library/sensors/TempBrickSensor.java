package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * User: ffouquet
 * Date: 29/08/11
 * Time: 16:00
 */


@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "apin", defaultValue = "0", optional = true, vals={"0","1","2","3","4","5"})
})
@Requires({
        @RequiredPort(name = "value", type = PortType.MESSAGE)
})
public class TempBrickSensor extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("#include <math.h>");
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("char buf[5];");
        gen.appendNativeStatement("double Temp;");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        arduinoGenerator.appendNativeStatement("pinMode(atoi(apin), INPUT);");
        arduinoGenerator.appendNativeStatement("int RawADC = analogRead(atoi(apin));");
        arduinoGenerator.appendNativeStatement("Temp = log(((10240000/RawADC) - 10000));");
        arduinoGenerator.appendNativeStatement("Temp = 1 / (0.001129148 + (0.000234125 * Temp) + (0.0000000876741 * Temp * Temp * Temp));");
        arduinoGenerator.appendNativeStatement("Temp = Temp - 273.15;");
        arduinoGenerator.appendNativeStatement("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        arduinoGenerator.appendNativeStatement("if (msg){memset(msg, 0, sizeof(kmessage));}");
        arduinoGenerator.appendNativeStatement("sprintf(buf,\"%d\",int(Temp));");
        arduinoGenerator.appendNativeStatement("msg->value = buf;");
        arduinoGenerator.appendNativeStatement("msg->metric = \"c\";");
        arduinoGenerator.appendNativeStatement("value_rport(msg);");
        arduinoGenerator.appendNativeStatement("free(msg);");
    }
}
