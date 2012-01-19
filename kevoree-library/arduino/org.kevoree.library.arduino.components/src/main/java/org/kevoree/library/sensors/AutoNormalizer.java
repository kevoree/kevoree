package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "norm", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "inverted", defaultValue = "0", optional = true,vals={"0","1"})
})
public class AutoNormalizer extends AbstractArduinoComponent {

    @Override
    public void generateInit(ArduinoGenerator gen) {
        gen.appendNativeStatement("minValue = 1024;");
        gen.appendNativeStatement("maxValue = 0;");
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("char buf[10];");
        gen.appendNativeStatement("int minValue;");
        gen.appendNativeStatement("int maxValue;");
    }

    @Port(name = "input")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("float value = atof(msg->value);\n");
        context.append("if(value < minValue){ minValue = value; }\n");
        context.append("if(value > maxValue){ maxValue = value; }\n");
        context.append("float result=((value-minValue)/ (maxValue-minValue))*100;\n");
        context.append("if(atoi(inverted) == 0){result = abs(100 - result  );}\n");
        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(msg, 0, sizeof(kmessage));}");
        context.append("sprintf(buf,\"%d\",int(result));\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric = \"percent\";");
        context.append("norm_rport(smsg);");
        context.append("free(smsg);");
    }

}
