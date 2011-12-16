package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * User: ffouquet
 * Date: 29/08/11
 * Time: 16:00
 */


@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "apin", defaultValue = "0", optional = true, vals={"0","1","2","3","4","5"}),
        @DictionaryAttribute(name = "period", defaultValue = "100", optional = true)
})
@Requires({
        @RequiredPort(name = "value", type = PortType.MESSAGE)
})
public class TempBrickSensor extends AbstractComponentType {

    @Start
    @Stop
    public void dummy() {
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <math.h> \n");
    }

    @Generate("classheader")
    public void generateClassHeader(StringBuffer context) {
        context.append("char buf[5];\n");
        context.append("double Temp;\n");
    }


    @Generate("periodic")
    public void generateSetup(StringBuffer context) {
        context.append("pinMode(atoi(apin), INPUT);\n");
        context.append("int RawADC = analogRead(atoi(apin));");
        context.append("Temp = log(((10240000/RawADC) - 10000));\n");
        context.append("Temp = 1 / (0.001129148 + (0.000234125 * Temp) + (0.0000000876741 * Temp * Temp * Temp));\n");
        context.append("Temp = Temp - 273.15;\n");
        context.append("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));\n");
        context.append("if (msg){memset(msg, 0, sizeof(kmessage));}\n");
        context.append("sprintf(buf,\"%d\",int(Temp));\n");
        context.append("msg->value = buf;\n");
        context.append("msg->metric = \"c\";\n");
        context.append("value_rport(msg);\n");
        context.append("free(msg);\n");
    }


}
