package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * User: ffouquet
 * Date: 15/09/11
 * Time: 20:41
 */

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "dpin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"})
})
@Requires({
        @RequiredPort(name = "range", type = PortType.MESSAGE, needCheckDependency = false)
})
public class UltraSonicRange extends AbstractPeriodicArduinoComponent {


    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("long duration,cm;\n");
        gen.appendNativeStatement("char buf[4];\n");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator gen) {
        //CLEAN SIGNAL
        gen.appendNativeStatement("" +
                " pinMode(atoi(dpin), OUTPUT);\n" +
                " digitalWrite(atoi(dpin), LOW);\n" +
                " delayMicroseconds(2);\n" +
                " digitalWrite(atoi(dpin), HIGH);\n" +
                " delayMicroseconds(5);\n" +
                " digitalWrite(atoi(dpin), LOW);");
        //MESURE RANGE
        gen.appendNativeStatement("" +
                " pinMode(atoi(dpin), INPUT);\n" +
                " duration = pulseIn(atoi(dpin), HIGH);\n");
        // The speed of sound is 340 m/s or 29 microseconds per centimeter.
        // The ping travels out and back, so to find the distance of the
        // object we take half of the distance travelled.
        gen.appendNativeStatement("cm = ( duration / 29 / 2 );");

        gen.appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("sprintf(buf,\"%d\",int(cm));\n");
        gen.appendNativeStatement("smsg->value = buf;\n");
        gen.appendNativeStatement("smsg->metric=\"cm\";");
        gen.appendNativeStatement("range_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");
    }
}
