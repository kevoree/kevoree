package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 02/04/12
 * Time: 16:30
 */

@Library(name = "Arduino")
@ComponentType
@Requires({
        @RequiredPort(name = "gpsADR", type = PortType.MESSAGE, needCheckDependency = false)     //0x42
})
public class GpsI2C extends AbstractPeriodicArduinoComponent  {


    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement("#define WireSend(args) Wire.write(args)\n" +
                "#define WireRead(args) Wire.read(args)\n" +
                "#define printByte(args) Serial.write(args)\n" +
                "#define printlnByte(args)  Serial.write(args),Serial.println()\n" +
                "#define BUFFER_LENGTH 10//Define the buffer length");

        gen.appendNativeStatement("#include <Wire.h>");
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {

    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
