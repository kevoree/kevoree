package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

@Library(name = "KevoreeArduino")
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
public class DualMotor extends AbstractComponentType {

    @Generate("classheader")
    public void generatePeriodic(StringBuffer context) {
        context.append("boolean state ;\n");
    }

    @Start
    @Stop
    public void dummy() {
    }

    @Port(name = "goFront")
    public void goFront(Object o) {
        StringBuffer context = (StringBuffer) o;
        /* Set Pin Mode & Speed */
        context.append("pinMode(atoi(MA), OUTPUT);");
        context.append("pinMode(atoi(MAB), OUTPUT);");
        context.append("pinMode(atoi(I1pin), OUTPUT);");
        context.append("pinMode(atoi(I2pin), OUTPUT);");
         context.append("pinMode(atoi(I1Bpin), OUTPUT);");
        context.append("pinMode(atoi(I2Bpin), OUTPUT);");
        //SET SPEED
        context.append("analogWrite(atoi(MA), 100);\n");
        context.append("analogWrite(atoi(MAB), 100);\n");
        // Both DC motor rotates clockwise
        context.append("digitalWrite(atoi(I1pin),HIGH);\n");
        context.append("digitalWrite(atoi(I2pin),LOW);\n");
        context.append("digitalWrite(atoi(I1Bpin),HIGH);\n");
        context.append("digitalWrite(atoi(I2Bpin),LOW);\n");
    }

    @Port(name = "goBack")
    public void goBack(Object o) {
        StringBuffer context = (StringBuffer) o;
        /* Set Pin Mode & Speed */
        context.append("pinMode(atoi(MA), OUTPUT);");
        context.append("pinMode(atoi(MAB), OUTPUT);");
        context.append("pinMode(atoi(I1pin), OUTPUT);");
        context.append("pinMode(atoi(I2pin), OUTPUT);");
         context.append("pinMode(atoi(I1Bpin), OUTPUT);");
        context.append("pinMode(atoi(I2Bpin), OUTPUT);");
        //SET SPEED
        context.append("analogWrite(atoi(MA), 100);\n");
        context.append("analogWrite(atoi(MAB), 100);\n");
        // Both DC motor rotates clockwise
        context.append("digitalWrite(atoi(I1pin),LOW);\n");
        context.append("digitalWrite(atoi(I2pin),HIGH);\n");
        context.append("digitalWrite(atoi(I1Bpin),LOW);\n");
        context.append("digitalWrite(atoi(I2Bpin),HIGH);\n");
    }

    @Port(name = "stop")
    public void stopPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        /* Set Pin Mode & Speed */
        context.append("pinMode(atoi(MA), OUTPUT);");
        context.append("pinMode(atoi(MAB), OUTPUT);");
        context.append("pinMode(atoi(I1pin), OUTPUT);");
        context.append("pinMode(atoi(I2pin), OUTPUT);");
         context.append("pinMode(atoi(I1Bpin), OUTPUT);");
        context.append("pinMode(atoi(I2Bpin), OUTPUT);");
        //SET SPEED
        context.append("analogWrite(atoi(MA), 100);\n");
        context.append("analogWrite(atoi(MAB), 100);\n");
        // Both DC motor rotates clockwise
        context.append("digitalWrite(atoi(I1pin),HIGH);\n");
        context.append("digitalWrite(atoi(I2pin),HIGH);\n");
        context.append("digitalWrite(atoi(I1Bpin),HIGH);\n");
        context.append("digitalWrite(atoi(I2Bpin),HIGH);\n");
    }

    @Port(name = "turnLeft")
    public void turnLeft(Object o) {

    }

    @Port(name = "turnRight")
    public void turnRight(Object o) {

    }
}
