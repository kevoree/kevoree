package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;


@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})
@Requires({
        @RequiredPort(name = "click", type = PortType.MESSAGE),
        @RequiredPort(name = "release", type = PortType.MESSAGE)
})
public class PushButton extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("int buttonState = LOW;\n");
    }

    @Generate("setup")
    public void generateSetup(StringBuffer context) {
        context.append("pinMode("+this.getDictionary().get("pin")+", INPUT);\n");
    }

    @Generate("loop")
    public void generateLoop(StringBuffer context) {
        context.append("int buttonState = 0;");

        context.append("  " +
                "int newButtonState = digitalRead("+this.getDictionary().get("pin")+");\n" +
                "  \n" +
                "  if (newButtonState == HIGH) { \n" +
                "    if(buttonState == LOW){\n" +
                "      buttonState = HIGH;\n" +
                "      //DO ACTION\n" );

                //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "click", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(\"click\");\n");


                //"      Serial.println(\"click\");\n"
                context.append("    " +
                        "}\n" +
                "  } else {\n" +
                "    if(buttonState == HIGH){\n" +
                "      buttonState = LOW;\n" +
                "      //DO ACTION UNRELEASE ACTION\n" );

                        //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "release", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(\"release\");\n");


               // "      Serial.println(\"unclick\");\n"
                context.append("\n" +
                "    }\n" +
                "  }");


    }


}
