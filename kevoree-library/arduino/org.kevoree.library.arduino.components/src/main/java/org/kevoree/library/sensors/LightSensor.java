package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true,vals={"0","1","2","3","4","5"})
})
@Provides({
        @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "light", type = PortType.MESSAGE, needCheckDependency = false)
})
public class LightSensor extends AbstractComponentType {

    @Start
    @Stop
    public void start() {
    }

    @Generate("classheader")
    public void generateClassHeader(StringBuffer context) {
        context.append("int photocellReading;\n");
        context.append("char buf[10];\n");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("photocellReading = analogRead(atoi(pin));\n");

        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        context.append("sprintf(buf,\"%d\",photocellReading);\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"alux\";");
        context.append("light_rport(smsg);");
        context.append("free(smsg);");

    }
}