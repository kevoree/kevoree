package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true,vals={"0","1","2","3","4","5","6","7","8","9","10","11","12","13"})
})
@Requires({
    @RequiredPort(name = "click", type = PortType.MESSAGE, needCheckDependency = false),
    @RequiredPort(name = "release", type = PortType.MESSAGE, needCheckDependency = false)
})
public class PushButton extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("int buttonState;");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        getGenerator().appendNativeStatement("pinMode(atoi(pin), INPUT);\n");
                getGenerator().appendNativeStatement("int newButtonState = digitalRead(atoi(pin));\n"
                        + "  if (newButtonState == HIGH) { \n"
                        + "    if(buttonState == LOW){\n"
                        + "      buttonState = HIGH;\n");

                getGenerator().appendNativeStatement("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
                getGenerator().appendNativeStatement("if (msg){memset(msg, 0, sizeof(kmessage));}");
                getGenerator().appendNativeStatement("msg->value = \"click\";");
                getGenerator().appendNativeStatement("msg->metric = \"event\";");
                getGenerator().appendNativeStatement("click_rport(msg);");
                getGenerator().appendNativeStatement("free(msg);");

                getGenerator().appendNativeStatement("    "
                        + "}\n"
                        + "  } else {\n"
                        + "    if(buttonState == HIGH){\n"
                        + "      buttonState = LOW;\n"
                        + "      //DO ACTION UNRELEASE ACTION\n");

                getGenerator().appendNativeStatement("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
                getGenerator().appendNativeStatement("if (msg){memset(msg, 0, sizeof(kmessage));}");
                getGenerator().appendNativeStatement("msg->value = \"release\";");
                getGenerator().appendNativeStatement("msg->metric = \"event\";");
                getGenerator().appendNativeStatement("release_rport(msg);");
                getGenerator().appendNativeStatement("free(msg);");

                getGenerator().appendNativeStatement("\n"
                        + "    }\n"
                        + "  }");
    }
}
