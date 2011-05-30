package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})
@Provides({
    @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "light", type = PortType.MESSAGE)
})
public class LightSensor extends AbstractComponentType {

    @Start
    @Stop
    public void start() {
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("int photocellReading = analogRead(atoi(pin));\n");
        
        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        context.append("char buf[255];\n");
        context.append("sprintf(buf,\"%d\",photocellReading);\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"alux\";");
        context.append("light_rport(smsg);");
        context.append("free(smsg);");

    }
}