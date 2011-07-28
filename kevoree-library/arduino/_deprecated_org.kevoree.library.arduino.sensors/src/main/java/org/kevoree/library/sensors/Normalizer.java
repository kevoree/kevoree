package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "minValue", defaultValue = "0", optional = true),
        @DictionaryAttribute(name = "maxValue", defaultValue = "255", optional = true)
})
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "norm", type = PortType.MESSAGE)
})
public class Normalizer extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }


    @Port(name = "input")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;

        context.append("" +
                "" +
                "  char msg[param.length()+10];\n" +
                "  param.toCharArray(msg, param.length()+1);\n" +
                "  int value = atoi(msg);\n" +
                " String result = String(abs((value*100) / (" + this.getDictionary().get("max") + " - " + this.getDictionary().get("minValue") + ")));\n");


        //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "norm", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(result);\n");

    }

}
