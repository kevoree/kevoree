package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
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
        @DictionaryAttribute(name = "trigger_alarme", defaultValue = "35",dataType = Long.class)
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
        gen.appendNativeStatement("int last_roll;");
        gen.appendNativeStatement("int last_yaw;");
        gen.appendNativeStatement("int last_pitch;");
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
        getGenerator().appendNativeStatement("int tmp;");
        getGenerator().appendNativeStatement("tmp = atoi(msg->value);");
        getGenerator().appendNativeStatement(" if(tmp != 0){ ");
        getGenerator().appendNativeStatement("if(abs(last_yaw-tmp) > 45){ last_yaw  = tmp;");
        getGenerator().appendNativeStatement("prealarme_nbMotion++;");
        getGenerator().appendNativeStatement("alarme_nbMotion++;");
        getGenerator().appendNativeStatement("}");
        getGenerator().appendNativeStatement("}");

    }
    @Port(name = "roll")
    public void roll(Object o)
    {
        getGenerator().appendNativeStatement("int tmp;");
        getGenerator().appendNativeStatement("tmp = atoi(msg->value);");
        getGenerator().appendNativeStatement(" if(tmp != 0){ ");
        getGenerator().appendNativeStatement("if(abs(last_roll-tmp) > 20){ last_roll  = tmp;");
        getGenerator().appendNativeStatement("prealarme_nbMotion++;");
        getGenerator().appendNativeStatement("alarme_nbMotion++;");
        getGenerator().appendNativeStatement("}");
        getGenerator().appendNativeStatement("}");
    }

    @Port(name = "pitch")
    public void pitch(Object o)
    {
        getGenerator().appendNativeStatement("int tmp;");
        getGenerator().appendNativeStatement("tmp = atoi(msg->value);");
        getGenerator().appendNativeStatement(" if(tmp != 0){ ");
        getGenerator().appendNativeStatement("if(abs(last_pitch-tmp) > 20){ last_pitch  = tmp;");
        getGenerator().appendNativeStatement("prealarme_nbMotion++;");
        getGenerator().appendNativeStatement("alarme_nbMotion++;");
        getGenerator().appendNativeStatement("}");
        getGenerator().appendNativeStatement("}");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("kmessage * smsg;");
        gen.appendNativeStatement("currentMillis = millis();");

        gen.appendNativeStatement("if((currentMillis - prealarme_previousMillis) > (trigger_prealarme*1000))");
        gen.appendNativeStatement("{");

        gen.appendNativeStatement(" prealarme_previousMillis = currentMillis;");

        gen.appendNativeStatement(" if(prealarme_nbMotion < 8){    ");

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

        gen.appendNativeStatement(" if(alarme_nbMotion < 8){    ");

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
