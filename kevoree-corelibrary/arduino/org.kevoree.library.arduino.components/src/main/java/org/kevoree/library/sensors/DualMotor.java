package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "I1pin", defaultValue = "7", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "I2pin", defaultValue = "6", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "I1Bpin", defaultValue = "4", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "I2Bpin", defaultValue = "3", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "MA", defaultValue = "10", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"}),
        @DictionaryAttribute(name = "MAB", defaultValue = "9", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"})
})
@Provides({
        @ProvidedPort(name = "goFront", type = PortType.MESSAGE),
        @ProvidedPort(name = "goBack", type = PortType.MESSAGE),
        @ProvidedPort(name = "stop", type = PortType.MESSAGE),
        @ProvidedPort(name = "turnLeft", type = PortType.MESSAGE),
        @ProvidedPort(name = "turnRight", type = PortType.MESSAGE)
})
public class DualMotor extends AbstractArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("boolean state ;\n");
    }


    @Port(name = "goFront")
    public void goFront(Object o) {
        /* Set Pin Mode & Speed */
        getGenerator().appendNativeStatement("pinMode(atoi(MA), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(MAB), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I1pin), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I2pin), OUTPUT);");
         getGenerator().appendNativeStatement("pinMode(atoi(I1Bpin), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I2Bpin), OUTPUT);");
        //SET SPEED
        getGenerator().appendNativeStatement("analogWrite(atoi(MA), 100);\n");
        getGenerator().appendNativeStatement("analogWrite(atoi(MAB), 100);\n");
        // Both DC motor rotates clockwise
        getGenerator().appendNativeStatement("digitalWrite(atoi(I1pin),HIGH);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I2pin),LOW);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I1Bpin),HIGH);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I2Bpin),LOW);\n");
    }

    @Port(name = "goBack")
    public void goBack(Object o) {
        /* Set Pin Mode & Speed */
        getGenerator().appendNativeStatement("pinMode(atoi(MA), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(MAB), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I1pin), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I2pin), OUTPUT);");
         getGenerator().appendNativeStatement("pinMode(atoi(I1Bpin), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I2Bpin), OUTPUT);");
        //SET SPEED
        getGenerator().appendNativeStatement("analogWrite(atoi(MA), 100);\n");
        getGenerator().appendNativeStatement("analogWrite(atoi(MAB), 100);\n");
        // Both DC motor rotates clockwise
        getGenerator().appendNativeStatement("digitalWrite(atoi(I1pin),LOW);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I2pin),HIGH);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I1Bpin),LOW);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I2Bpin),HIGH);\n");
    }

    @Port(name = "stop")
    public void stopPort(Object o) {
        /* Set Pin Mode & Speed */
        getGenerator().appendNativeStatement("pinMode(atoi(MA), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(MAB), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I1pin), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I2pin), OUTPUT);");
         getGenerator().appendNativeStatement("pinMode(atoi(I1Bpin), OUTPUT);");
        getGenerator().appendNativeStatement("pinMode(atoi(I2Bpin), OUTPUT);");
        //SET SPEED
        getGenerator().appendNativeStatement("analogWrite(atoi(MA), 100);\n");
        getGenerator().appendNativeStatement("analogWrite(atoi(MAB), 100);\n");
        // Both DC motor rotates clockwise
        getGenerator().appendNativeStatement("digitalWrite(atoi(I1pin),HIGH);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I2pin),HIGH);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I1Bpin),HIGH);\n");
        getGenerator().appendNativeStatement("digitalWrite(atoi(I2Bpin),HIGH);\n");
    }

    @Port(name = "turnLeft")
    public void turnLeft(Object o) {

    }

    @Port(name = "turnRight")
    public void turnRight(Object o) {

    }
}
