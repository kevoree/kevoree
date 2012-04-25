package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true,vals={"0","1","2","3","4","5"})
})
@Requires({
        @RequiredPort(name = "light", type = PortType.MESSAGE, needCheckDependency = false)
})
public class LightSensor extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("int photocellReading;\n");
        getGenerator().appendNativeStatement("char buf[10];\n");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        getGenerator().appendNativeStatement("photocellReading = analogRead(atoi(pin));\n");
        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",photocellReading);\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"alux\";");
        getGenerator().appendNativeStatement("light_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");
    }
}