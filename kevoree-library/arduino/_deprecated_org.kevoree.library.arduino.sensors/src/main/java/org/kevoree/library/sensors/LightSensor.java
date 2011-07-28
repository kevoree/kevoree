package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

@Library(name="KevoreeArduino")
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
    public void start() {
    }

    @Stop
    public void stop() {

    }


    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;

        /* Generate code for port */
        context.append(
                "int photocellReading = analogRead("+this.getDictionary().get("pin")+");\n");

        //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "light", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(String(photocellReading));\n");

    }

}