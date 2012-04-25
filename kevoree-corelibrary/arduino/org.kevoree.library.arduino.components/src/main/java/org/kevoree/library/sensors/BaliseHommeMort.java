package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 20/02/12
 * Time: 16:29
 */

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "trigger_prealarme", defaultValue = "23",dataType = Long.class),
        @DictionaryAttribute(name = "trigger_alarme", defaultValue = "35",dataType = Long.class),
        @DictionaryAttribute(name = "sensibilite", defaultValue = "5",dataType = Long.class)
})

@Requires({
        @RequiredPort(name = "alarme", type = PortType.MESSAGE),
        @RequiredPort(name = "prealarme", type = PortType.MESSAGE)

})

@Provides({
        @ProvidedPort(name = "temperature", type = PortType.MESSAGE),
        @ProvidedPort(name = "yaw", type = PortType.MESSAGE) ,
        @ProvidedPort(name = "pitch", type = PortType.MESSAGE),
        @ProvidedPort(name = "roll", type = PortType.MESSAGE)
})


public class BaliseHommeMort  extends AbstractPeriodicArduinoComponent {


    @Override
    public void generateClassHeader(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("unsigned long currentMillis;");
        gen.appendNativeStatement("unsigned long prealarme_previousMillis;");
        gen.appendNativeStatement("unsigned long alarme_previousMillis;");
        gen.appendNativeStatement("int prealarme_nbMotion;");
        gen.appendNativeStatement("int alarme_nbMotion;");
        gen.appendNativeStatement("float last_roll;");
        gen.appendNativeStatement("float last_yaw;");
        gen.appendNativeStatement("float last_pitch;");
    }

    @Override
    public void generateInit(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("alarme_nbMotion=0;");
        gen.appendNativeStatement("prealarme_nbMotion=0;");
        gen.appendNativeStatement("currentMillis=0;");
        gen.appendNativeStatement("prealarme_previousMillis=0;");
    }

    @Port(name = "temperature")
    public void temp(Object o)
    {


    }

    @Port(name = "yaw")
    public void yaw(Object o)
    {
        getGenerator().appendNativeStatement("float tmp;\n" +
                "tmp = atof(msg->value);\n" +
                "if(abs(tmp-last_yaw) > sensibilite && tmp != 0){");
        getGenerator().appendNativeStatement("prealarme_nbMotion++;");
        getGenerator().appendNativeStatement("alarme_nbMotion++;");
        getGenerator().appendNativeStatement(" last_yaw = tmp; \n" +
                "}");


    }
    @Port(name = "roll")
    public void roll(Object o)
    {
        getGenerator().appendNativeStatement("float tmp;\n" +
                "tmp = atof(msg->value);\n" +
                "if(abs(tmp-last_roll) > sensibilite && tmp != 0){");
        getGenerator().appendNativeStatement("prealarme_nbMotion++;");
        getGenerator().appendNativeStatement("alarme_nbMotion++;");

        getGenerator().appendNativeStatement(" last_roll = tmp; \n" +
                "}");
    }

    @Port(name = "pitch")
    public void pitch(Object o)
    {
        getGenerator().appendNativeStatement("float tmp;\n" +
                "tmp = atof(msg->value);\n" +
                "if(abs(tmp-last_pitch) > sensibilite && tmp != 0){");
        getGenerator().appendNativeStatement("prealarme_nbMotion++;");
        getGenerator().appendNativeStatement("alarme_nbMotion++;");

        getGenerator().appendNativeStatement(" last_pitch = tmp; \n" +
                "}");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("kmessage * smsg;");
        gen.appendNativeStatement("currentMillis = millis();");

        gen.appendNativeStatement("if((currentMillis - prealarme_previousMillis) > (trigger_prealarme*1000))");
        gen.appendNativeStatement("{");

        gen.appendNativeStatement(" prealarme_previousMillis = currentMillis;");

        gen.appendNativeStatement(" if(prealarme_nbMotion < 2){    ");

        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("smsg->value = \"tick\";");
        gen.appendNativeStatement("smsg->metric=\"\";");
        gen.appendNativeStatement("prealarme_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");
        gen.appendNativeStatement("prealarme_nbMotion=0;");

        gen.appendNativeStatement("}else{");

        gen.appendNativeStatement("prealarme_nbMotion=0;");
        // RAS
        gen.appendNativeStatement("}");

        gen.appendNativeStatement("}");




        gen.appendNativeStatement("if((currentMillis - alarme_previousMillis) > (trigger_alarme*1000))");
        gen.appendNativeStatement("{");

        gen.appendNativeStatement(" alarme_previousMillis = currentMillis;");

        gen.appendNativeStatement(" if(alarme_nbMotion < 2){    ");

        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("smsg->value = \"tick\";");
        gen.appendNativeStatement("smsg->metric=\"\";");
        gen.appendNativeStatement("alarme_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");
        gen.appendNativeStatement("alarme_nbMotion=0;");

        gen.appendNativeStatement("}else{");

        gen.appendNativeStatement("alarme_nbMotion=0;");
        // RAS
        gen.appendNativeStatement("}");

        gen.appendNativeStatement("}");


    }

}
