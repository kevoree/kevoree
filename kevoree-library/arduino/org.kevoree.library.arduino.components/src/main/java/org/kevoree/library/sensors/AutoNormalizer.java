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
        context.append("int ");
        context.append("minValue" + this.getName());
        context.append("= 1024;\n");
        context.append("int ");
        context.append("maxValue" + this.getName());
        context.append("= 0;\n");

        context.append("char * value");
    }


    @Port(name = "input")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;

        context.append("" +
                "" +
                "  char msg[param.length()+10];\n" +
                "  param.toCharArray(msg, param.length()+1);\n" +
                "  float value = atof(msg);\n");

        context.append("if(value < " + "minValue" + this.getName() + "){ " + "minValue" + this.getName() + " = value; }\n");
        context.append("if(value > " + "maxValue" + this.getName() + "){ " + "maxValue" + this.getName() + " = value; }\n");
            context.append("float result=");
            context.append(" ((value-" + "minValue" + this.getName() + ")");
            context.append(" / (" + "maxValue" + this.getName() + "-" + "minValue" + this.getName() + "))*100");
            context.append(";\n");

        if (this.getDictionary().get("inverted").equals("1")) {
            context.append(" result = abs(100 - result  );\n");
        }


        context.append("kmessage * msg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (msg){memset(msg, 0, sizeof(kmessage));}");
        context.append("msg->value = \"release\";");
        context.append("msg->metric = \"percent\";");
        context.append("release_rport(msg);");
        context.append("free(msg);");




        //GENERATE METHOD CALL
        //context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "norm", PortUsage.required()));
        //GENERATE PARAMETER
        //context.append("(String(int(result)));\n");

    }

}
