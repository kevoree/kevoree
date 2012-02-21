package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 15/02/12
 * Time: 14:50
 */
@Library(name = "Arduino")
@ComponentType
@Requires({
        @RequiredPort(name = "roll", type = PortType.MESSAGE, optional = true),
        @RequiredPort(name = "pitch", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "yaw", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "acceleration", type = PortType.MESSAGE,optional = true)
})


public class IMUSensor  extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen){

        gen.appendNativeStatement("char buf [(7*4)];\n" +
                "volatile byte pos;\n" +
                "volatile boolean process_it;");

        gen.appendNativeStatement("// SPI interrupt routine\n" +
                "ISR (SPI_STC_vect)\n" +
                "{\n" +
                "byte c = SPDR;  // grab byte from SPI Data Register\n" +
                "  \n" +
                "  // add to buffer if room\n" +
                "  if (pos < sizeof buf)\n" +
                "    {\n" +
                "    buf [pos++] = c;\n" +
                "    \n" +
                "    if (c == '\\n')\n" +
                "      process_it = true;\n" +
                "      \n" +
                "    }  // end of room available\n" +
                "} ");

    }
    @Override
    public void generateClassHeader(ArduinoGenerator gen)
    {

        gen.appendNativeStatement("char buff_roll[7];");
        gen.appendNativeStatement("char buff_pitch[7];");
        gen.appendNativeStatement("char buff_yaw[7];");
        gen.appendNativeStatement("char buff_acceleration[7];");

        gen.appendNativeStatement("void razorINIT(){\n" +
                "  \n" +
                "  pinMode(MISO, OUTPUT);\n" +
                "  \n" +
                "  // turn on SPI in slave mode\n" +
                "  SPCR |= _BV(SPE);\n" +
                "  \n" +
                "\n" +
                "    // ready for an interrupt \n" +
                "  pos = 0;   // buffer empty\n" +
                "  process_it = false;\n" +
                "  \n" +
                "  // now turn on interrupts\n" +
                "  SPCR |= _BV(SPIE);\n" +
                "  \n" +
                "\n" +
                "}");
    }

    @Override
    public void generateInit(ArduinoGenerator gen) {

        gen.appendNativeStatement("razorINIT();");
    }


    @Override
    public void generatePeriodic(ArduinoGenerator gen)
    {

        gen.appendNativeStatement("  if (process_it)\n" +
                "    {\n" +
                "    buf [pos] = 0;  ");

        // data is available in the buffer
        gen.appendNativeStatement("kmessage * smsg;");

        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("sprintf(buff_roll,\"%d,%d\",(int)buf[0],(int)abs(buf[1]));");
        gen.appendNativeStatement("smsg->value = buff_roll;\n");
        gen.appendNativeStatement("smsg->metric=\"r\";");
        gen.appendNativeStatement("roll_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");


        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("sprintf(buff_pitch,\"%d,%d\",(int)buf[2],(int)abs(buf[3]));\n");
        gen.appendNativeStatement("smsg->value = buff_pitch;\n");
        gen.appendNativeStatement("smsg->metric=\"p\";");
        gen.appendNativeStatement("pitch_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");


        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("sprintf(buff_yaw,\"%d,%d\",(int)buf[4],(int)abs(buf[5]));\n");
        gen.appendNativeStatement("smsg->value = buff_yaw;\n");
        gen.appendNativeStatement("smsg->metric=\"y\";");
        gen.appendNativeStatement("yaw_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");



        gen.appendNativeStatement("smsg = (kmessage*) malloc(sizeof(kmessage));");
        gen.appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        gen.appendNativeStatement("sprintf(buff_acceleration,\"%d,%d\",(int)buf[6],(int)abs(buf[7]));\n");
        gen.appendNativeStatement("smsg->value = buff_acceleration;\n");
        gen.appendNativeStatement("smsg->metric=\"a\";");
        gen.appendNativeStatement("acceleration_rport(smsg);");
        gen.appendNativeStatement("free(smsg);");

        gen.appendNativeStatement("}");

    }
}
