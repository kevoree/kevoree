package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
@ComponentType
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "norm", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "inverted", defaultValue = "0", optional = true)
})
public class AutoNormalizer extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {
    }

    @Generate("classheader")
    public void generateHeader(StringBuffer context) {
        context.append("char buf[10];\n");
        context.append("int minValue;\n");
        context.append("int maxValue;\n");
    }

    @Generate("classinit")
    public void generateClassInit(StringBuffer context) {
        context.append("minValue = 1024;\n");
        context.append("maxValue = 0;\n");
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
