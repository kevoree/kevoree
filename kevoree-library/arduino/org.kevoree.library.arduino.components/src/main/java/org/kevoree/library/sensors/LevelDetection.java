package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "level")
})
@Provides({
    @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "over", type = PortType.MESSAGE, needCheckDependency = false),
    @RequiredPort(name = "std", type = PortType.MESSAGE, needCheckDependency = false)
})
public class LevelDetection extends AbstractComponentType {

    @Generate("classheader")
    public void generatePeriodic(StringBuffer context) {
        context.append("boolean preState ;\n");
    }

    @Start
    @Stop
    public void dummy() {
    }

    @Port(name = "input")
    public void triggerInput(Object o) {
        StringBuffer context = (StringBuffer) o;

        context.append("if(atoi(msg->value) > atoi(level)){\n");


        context.append("}\n");
        //


        context.append("int newState = 0;\n");
        context.append("if(state){ newState = LOW; } else { newState=HIGH; }");
        context.append("state = ! state; ");
        context.append("pinMode(atoi(pin), OUTPUT);");
        context.append("digitalWrite(atoi(pin), newState);\n");
    }
}
