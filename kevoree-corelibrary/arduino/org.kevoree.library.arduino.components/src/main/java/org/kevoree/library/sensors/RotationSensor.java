package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
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
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5"})
})
@Requires({
        @RequiredPort(name = "value", type = PortType.MESSAGE)
})
public class RotationSensor extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("int previousValue;");
        gen.appendNativeStatement("char buf[5];");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        arduinoGenerator.appendNativeStatement("pinMode(atoi(pin), INPUT);\n");
        arduinoGenerator.appendNativeStatement("int newValue = analogRead(atoi(pin));\n"
                + "  if (newValue != previousValue) { \n");
        arduinoGenerator.appendNativeStatement("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        arduinoGenerator.appendNativeStatement("if (msg){memset(msg, 0, sizeof(kmessage));}");
        arduinoGenerator.appendNativeStatement("sprintf(buf,\"%d\",newValue);");
        arduinoGenerator.appendNativeStatement("msg->value = buf;");
        arduinoGenerator.appendNativeStatement("msg->metric = \"arot\";");
        arduinoGenerator.appendNativeStatement("value_rport(msg);");
        arduinoGenerator.appendNativeStatement("free(msg);");
        arduinoGenerator.appendNativeStatement("previousValue = newValue;}");
    }
}
