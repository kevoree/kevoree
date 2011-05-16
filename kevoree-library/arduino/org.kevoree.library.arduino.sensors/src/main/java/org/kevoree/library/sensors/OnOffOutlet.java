package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})

@Provides({
        @ProvidedPort(name = "on", type = PortType.MESSAGE),
        @ProvidedPort(name = "off", type = PortType.MESSAGE)
})
public class OnOffOutlet extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }


    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("pinMode("+this.getDictionary().get("pin").toString()+", OUTPUT);\n");
    }

    @Port(name = "on")
    public void triggerOn(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("digitalWrite("+this.getDictionary().get("pin").toString()+", HIGH);\n");
    }

    @Port(name = "off")
    public void triggerOff(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("digitalWrite("+this.getDictionary().get("pin").toString()+", LOW);\n");
    }

}
