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
    @RequiredPort(name = "level", type = PortType.MESSAGE)
})
public class AnalogSlider extends AbstractComponentType {


    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }


    @Generate("setup")
    public void generateHeader(StringBuffer context) {
        context.append("pinMode("+this.getDictionary().get("pin")+", INPUT);\n");
        context.append("digitalWrite("+this.getDictionary().get("pin")+",HIGH); \n");

    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;

        /* Generate code for port */
        context.append(
                "int sensorValue = 1024-analogRead("+this.getDictionary().get("pin")+");\n");

        context.append("\n" +
                "    if(sensorValue < 80){\n" +
                "     sensorValue = 0; \n" +
                "    } else {\n" +
                "     sensorValue = sensorValue - 80; \n" +
                "    }\n");


        //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "level", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(String(sensorValue));\n");

    }

}