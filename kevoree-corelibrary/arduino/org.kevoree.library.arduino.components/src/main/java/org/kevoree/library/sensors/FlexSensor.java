package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true,vals={"0","1","2","3","4","5"})
})
@Requires({
        @RequiredPort(name = "out", type = PortType.MESSAGE, needCheckDependency = false)
})
public class FlexSensor extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("char buf[10];\n");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), INPUT);\n");
        getGenerator().appendNativeStatement("int value = analogRead(atoi(pin));\n");
        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));\n");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}\n");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",value);\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"flex\";");
        getGenerator().appendNativeStatement("out_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");
    }
}