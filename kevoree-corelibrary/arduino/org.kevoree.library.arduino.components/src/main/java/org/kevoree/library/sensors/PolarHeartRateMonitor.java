package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 02/03/12
 * Time: 15:08
 */

@Library(name = "Arduino")
@ComponentType
@Requires({
        @RequiredPort(name = "heartRate", type = PortType.MESSAGE, needCheckDependency = false)
})
public class PolarHeartRateMonitor extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("#include <Wire.h>");
        gen.appendNativeStatement(" #define HRMI_I2C_ADDR      127");
        gen.appendNativeStatement(" #define HRMI_HR_ALG        1   // 1= average sample, 0 = raw sample");

    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("char buf[10];");
        gen.appendNativeStatement("void setupHeartMonitor(int type){\n" +
                "  //setup the heartrate monitor\n" +
                "  Wire.begin();\n" +
                "  \n" +
                "  writeRegister(HRMI_I2C_ADDR, 0x53, type); \n" +
                "}\n" +
                "\n" +
                "int getHeartRate(){\n" +
                "  //get and return heart rate\n" +
                "  //returns 0 if we couldnt get the heart rate\n" +
                "  byte i2cRspArray[3]; \n" +
                "  i2cRspArray[2] = 0;\n" +
                "\n" +
                "  writeRegister(HRMI_I2C_ADDR,  0x47, 0x1); \n" +
                "\n" +
                "  if (hrmiGetData(127, 3, i2cRspArray)) {\n" +
                "    return i2cRspArray[2];\n" +
                "  }\n" +
                "  else{\n" +
                "    return 0;\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "void writeRegister(int deviceAddress, byte address, byte val) {\n" +
                "  //I2C command to send data to a specific address on the device\n" +
                "  Wire.beginTransmission(deviceAddress); // start transmission to device \n" +
                "  Wire.write(address);       // send register address\n" +
                "  Wire.write(val);         // send value to write\n" +
                "  Wire.endTransmission();     // end transmission\n" +
                "}\n" +
                "\n" +
                "boolean hrmiGetData(byte addr, byte numBytes, byte* dataArray){\n" +
                "  //Get data from heart rate monitor and fill dataArray byte with responce\n" +
                "  //Returns true if it was able to get it, false if not\n" +
                "  Wire.requestFrom(addr, numBytes);\n" +
                "  if (Wire.available()) {\n" +
                "\n" +
                "    for (int i=0; i<numBytes; i++){\n" +
                "      dataArray[i] = Wire.read();\n" +
                "    }\n" +
                "\n" +
                "    return true;\n" +
                "  }\n" +
                "  else{\n" +
                "    return false;\n" +
                "  }\n" +
                "}");

    }


    @Override
    public void generateInit(ArduinoGenerator gen)
    {
        gen.appendNativeStatement("setupHeartMonitor(HRMI_HR_ALG);");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator gen) {
        gen.appendNativeStatement("int heartRate = getHeartRate();");
        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",heartRate);\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"c\";");
        getGenerator().appendNativeStatement("heartRate_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");

    }
}
