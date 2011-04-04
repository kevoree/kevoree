/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;

/**
 *
 * @author ffouquet
 */
@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "period", defaultValue = "1000", optional = true)
})
@Requires({
    @RequiredPort(name = "tick", type = PortType.MESSAGE)
})
public class Timer extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <Metro.h>\n");
        context.append("Metro ");
        context.append("metroTimer" + this.getName());
        context.append("= Metro(" + this.getDictionary().get("period") + ");\n");
    }

    @Generate("loop")
    public void generateLoop(StringBuffer context) {
        context.append("if (");
        context.append("metroTimer" + this.getName());
        context.append(".check() == 1) {\n");

        //TODO CALL REQUIRED PORT
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "tick", org.kevoree.library.arduinoNodeType.PortUsage.required()));
        context.append("(\"tick\");\n");
        context.append("}\n");
    }
}
