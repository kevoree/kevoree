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
        getGenerator().appendNativeStatement("float value = atof(msg->value);\n");
        getGenerator().appendNativeStatement("if(value < minValue){ minValue = value; }\n");
        getGenerator().appendNativeStatement("if(value > maxValue){ maxValue = value; }\n");
        getGenerator().appendNativeStatement("float result=((value-minValue)/ (maxValue-minValue))*100;\n");
        getGenerator().appendNativeStatement("if(atoi(inverted) == 0){result = abs(100 - result  );}\n");
        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(msg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",int(result));\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric = \"percent\";");
        getGenerator().appendNativeStatement("norm_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");
    }

}
