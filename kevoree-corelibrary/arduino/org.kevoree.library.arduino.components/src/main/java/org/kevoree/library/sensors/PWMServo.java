package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 21/12/11
 * Time: 13:37
 */
@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "", optional = false)
})
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})

public class PWMServo extends AbstractArduinoComponent {


    @Override
    public void generateHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("#include <Servo.h>  \n");
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("Servo myservo;\n");
        getGenerator().appendNativeStatement("int pos; \n");
        getGenerator().appendNativeStatement("pos=90; \n");
    }

    @Override
    public void generateInit(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("myservo.attach(pin); \n");
        getGenerator().appendNativeStatement("myservo.write(pos); \n");
    }

    @Port(name = "input")
    public void inputPort(Object o) {
        getGenerator().appendNativeStatement("pos = atoi(msg->value); \n");
        getGenerator().appendNativeStatement("myservo.write(pos); \n");
    }
}
