package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})

@Provides({
        @ProvidedPort(name = "intensity", type = PortType.MESSAGE),
        @ProvidedPort(name = "color", type = PortType.MESSAGE)
})
public class DMXLight extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <DmxSimple.h>\n");
    }


    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("DmxSimple.usePin(" + this.getDictionary().get("pin").toString() + ");\n");
        context.append("DmxSimple.maxChannel(4);\n");

        context.append("DmxSimple.write(2,255);\n");
        context.append("DmxSimple.write(3,255);\n");
        context.append("DmxSimple.write(4,255);\n");

        context.append("DmxSimple.write(1,127);\n");

    }

    @Port(name = "intensity")
    public void triggerOn(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("DmxSimple.write(1,int(param));\n");
    }
    @Port(name = "color")
    public void triggerColor(Object o) {
        StringBuffer context = (StringBuffer) o;
        //context.append("DmxSimple.write(1,int(param));\n");
    }


}
