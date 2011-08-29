package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * User: ffouquet
 * Date: 29/08/11
 * Time: 16:00
 */


@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "period", defaultValue = "100", optional = true)
})
@Requires({
        @RequiredPort(name = "value", type = PortType.MESSAGE)
})
public class RotationSensor extends AbstractComponentType {

    @Start
    @Stop
    public void dummy() {
    }

    @Generate("classheader")
    public void generateHeader(StringBuffer context) {
        context.append("int previousValue;\n");
        context.append("char buf[5];\n");
    }

    @Generate("periodic")
    public void generateSetup(StringBuffer context) {
        context.append("pinMode(atoi(pin), INPUT);\n");
        context.append("int newValue = analogRead(atoi(pin));\n"
                + "  if (newValue != previousValue) { \n");

        context.append("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (msg){memset(msg, 0, sizeof(kmessage));}");
        context.append("sprintf(buf,\"%d\",newValue);\n");
        context.append("msg->value = buf;");
        context.append("msg->metric = \"arot\";");
        context.append("value_rport(msg);");
        context.append("free(msg);");

        context.append("\n"
                + " previousValue = newValue;   }\n" );


    }


}
