package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 02/03/12
 * Time: 09:58
 */
@Library(name = "Arduino")
@ComponentType
@Requires({
        @RequiredPort(name = "temp", type = PortType.MESSAGE, needCheckDependency = false)
})
public class TempSensorInfrared extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.addLibrary("i2cmaster.h", this.getClass().getClassLoader().getResourceAsStream("I2Cmaster/i2cmaster.h"));
        gen.addLibrary("twimaster.cpp", this.getClass().getClassLoader().getResourceAsStream("I2Cmaster/twimaster.cpp"));
        gen.appendNativeStatement("#include <i2cmaster.h>");

    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("char buf[10];");
    }

    @Override
    public void generateInit(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("i2c_init(); ");
        gen.appendNativeStatement("PORTC = (1 << PORTC4) | (1 << PORTC5);");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator gen) {
        gen.appendNativeStatement("  int dev = 0x5A<<1;\n" +
                "  int data_low = 0;\n" +
                "  int data_high = 0;\n" +
                "  int pec = 0;\n" +
                "  \n" +
                "  i2c_start_wait(dev+I2C_WRITE);\n" +
                "  i2c_write(0x07);\n" +
                "  \n" +
                "  // read\n" +
                "  i2c_rep_start(dev+I2C_READ);\n" +
                "  data_low = i2c_readAck(); //Read 1 byte and then send ack\n" +
                "  data_high = i2c_readAck(); //Read 1 byte and then send ack\n" +
                "  pec = i2c_readNak();\n" +
                "  i2c_stop();\n" +
                "  \n" +
                "  //This converts high and low bytes together and processes temperature, MSB is a error bit and is ignored for temps\n" +
                "  double tempFactor = 0.02; // 0.02 degrees per LSB (measurement resolution of the MLX90614)\n" +
                "  double tempData = 0x0000; // zero out the data\n" +
                "  int frac; // data past the decimal point\n" +
                "  \n" +
                "  // This masks off the error bit of the high byte, then moves it left 8 bits and adds the low byte.\n" +
                "  tempData = (double)(((data_high & 0x007F) << 8) + data_low);\n" +
                "  tempData = (tempData * tempFactor)-0.01;\n" +
                "  \n" +
                "  float celcius = tempData - 273.15;\n" +
                "  float fahrenheit = (celcius*1.8) + 32;\n" +
                "");


        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",int(celcius));\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"c\";");
        getGenerator().appendNativeStatement("temp_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");

    }
}
