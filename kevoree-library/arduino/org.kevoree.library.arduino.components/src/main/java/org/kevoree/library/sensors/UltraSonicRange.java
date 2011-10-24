package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * User: ffouquet
 * Date: 15/09/11
 * Time: 20:41
 */

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "dpin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "period", defaultValue = "200", optional = true)
})
@Requires({
        @RequiredPort(name = "range", type = PortType.MESSAGE, needCheckDependency = false)
})
public class UltraSonicRange extends AbstractComponentType {

    @Start
    @Stop
    public void dummy() {
    }

    @Generate("classheader")
    public void generateHeader(StringBuffer context) {
        context.append("long duration,cm;\n");
        context.append("char buf[4];\n");
    }

    @Generate("periodic")
    public void generateSetup(StringBuffer context) {
        //CLEAN SIGNAL
        context.append("" +
                " pinMode(atoi(dpin), OUTPUT);\n" +
                " digitalWrite(atoi(dpin), LOW);\n" +
                " delayMicroseconds(2);\n" +
                " digitalWrite(atoi(dpin), HIGH);\n" +
                " delayMicroseconds(5);\n" +
                " digitalWrite(atoi(dpin), LOW);\n");
        //MESURE RANGE
        context.append("" +
                " pinMode(atoi(dpin), INPUT);\n" +
                " duration = pulseIn(atoi(dpin), HIGH);\n");
        // The speed of sound is 340 m/s or 29 microseconds per centimeter.
        // The ping travels out and back, so to find the distance of the
        // object we take half of the distance travelled.
        context.append("cm = ( duration / 29 / 2 );\n");

        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        context.append("sprintf(buf,\"%d\",int(cm));\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"cm\";");
        context.append("range_rport(smsg);");
        context.append("free(smsg);");


    }
}
