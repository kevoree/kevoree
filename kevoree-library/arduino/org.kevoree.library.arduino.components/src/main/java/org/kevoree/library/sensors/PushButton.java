package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true,vals={"0","1","2","3","4","5","6","7","8","9","10","11","12","13"}),
    @DictionaryAttribute(name = "period", defaultValue = "100", optional = true)
})
@Requires({
    @RequiredPort(name = "click", type = PortType.MESSAGE),
    @RequiredPort(name = "release", type = PortType.MESSAGE)
})
public class PushButton extends AbstractComponentType {

    @Start
    @Stop
    public void dummy() {
    }

    @Generate("classheader")
    public void generateHeader(StringBuffer context) {
        context.append("int buttonState ;\n");
    }

    @Generate("periodic")
    public void generateSetup(StringBuffer context) {
        context.append("pinMode(atoi(pin), INPUT);\n");
        context.append("int newButtonState = digitalRead(atoi(pin));\n"
                + "  if (newButtonState == HIGH) { \n"
                + "    if(buttonState == LOW){\n"
                + "      buttonState = HIGH;\n");

        context.append("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (msg){memset(msg, 0, sizeof(kmessage));}");
        context.append("msg->value = \"click\";");
        context.append("msg->metric = \"event\";");
        context.append("click_rport(msg);");
        context.append("free(msg);");

        context.append("    "
                + "}\n"
                + "  } else {\n"
                + "    if(buttonState == HIGH){\n"
                + "      buttonState = LOW;\n"
                + "      //DO ACTION UNRELEASE ACTION\n");

        context.append("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (msg){memset(msg, 0, sizeof(kmessage));}");
        context.append("msg->value = \"release\";");
        context.append("msg->metric = \"event\";");
        context.append("release_rport(msg);");
        context.append("free(msg);");

        context.append("\n"
                + "    }\n"
                + "  }");


    }
}
