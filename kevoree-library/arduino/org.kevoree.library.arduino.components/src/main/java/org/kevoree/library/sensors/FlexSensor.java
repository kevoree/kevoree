package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true),
        @DictionaryAttribute(name = "period", defaultValue = "100", optional = true)
})
@Requires({
        @RequiredPort(name = "out", type = PortType.MESSAGE)
})
public class FlexSensor extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {
    }

    @Generate("classheader")
    public void generateHeader(StringBuffer context) {
        context.append("char buf[10];\n");
    }

    @Generate("periodic")
    public void generateSetup(StringBuffer context) {
        context.append("pinMode(atoi(pin), INPUT);\n");
        context.append("int value = analogRead(atoi(pin));\n");
        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));\n");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}\n");
        context.append("sprintf(buf,\"%d\",value);\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"flex\";");
        context.append("out_rport(smsg);");
        context.append("free(smsg);");
    }

}