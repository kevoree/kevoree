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
        @DictionaryAttribute(name = "prealarme", defaultValue = "23"),
        @DictionaryAttribute(name = "alarme", defaultValue = "35")

})

@Requires({
        @RequiredPort(name = "risk", type = PortType.MESSAGE)

})

@Provides({
        @ProvidedPort(name = "temp", type = PortType.MESSAGE),
        @ProvidedPort(name = "motion", type = PortType.MESSAGE)
})


public class BaliseHommeMort  extends AbstractPeriodicArduinoComponent {


    @Override
    public void generateClassHeader(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("unsigned long currentMillis;");
        gen.appendNativeStatement("long previousMillis = 0;");
        gen.appendNativeStatement("long interval_prealarme;");
        gen.appendNativeStatement("long interval_alarme;");
        gen.appendNativeStatement("int nbMotion;");
    }

    @Override
    public void generateInit(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("nbMotion=0;");
        gen.appendNativeStatement("currentMillis=0;");
        gen.appendNativeStatement("interval_prealarme = atoi(prealarme);");
        gen.appendNativeStatement("interval_alarme=atoi(alarme);");
    }

    @Port(name = "temp")
    public void temp(Object o)
    {


    }

    @Port(name = "motion")
    public void motion(Object o)
    {


    }


    @Override
    public void generatePeriodic(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("kmessage * smsg;");
        gen.appendNativeStatement("currentMillis = millis();");
        gen.appendNativeStatement("if(currentMillis - previousMillis > interval_prealarme)");
        gen.appendNativeStatement("{");
        gen.appendNativeStatement(" previousMillis = currentMillis;");
        gen.appendNativeStatement(" if(nbMotion <= 0){       }  ");

        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("smsg->value = 1;\n");
        gen.appendNativeStatement("smsg->metric=\"\";");
        gen.appendNativeStatement("risk_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");



        gen.appendNativeStatement("}else {");

        gen.appendNativeStatement("if((int)atoi(motion) > 50){ ");

        gen.appendNativeStatement(" nbMotion++;");
        gen.appendNativeStatement("  }  ");

        gen.appendNativeStatement("}");

    }

}
