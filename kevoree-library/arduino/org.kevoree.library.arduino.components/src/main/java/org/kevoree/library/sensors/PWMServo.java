package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 21/12/11
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "", optional = false)
})
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})

public class PWMServo extends AbstractComponentType {

    @Start
    @Stop
    public void dummy() {
    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("#include <Servo.h>  \n");
    }

    @Generate("classheader")
    public void generateClassHeader(StringBuffer context) {
        context.append("Servo myservo;\n");
        context.append("int pos; \n");
         context.append("pos=90; \n");
    }

    @Generate("classinit")
    public void generateClassInit(StringBuffer context) {
        context.append("myservo.attach(pin); \n");
        context.append("myservo.write(pos); \n");
    }

    @Port(name = "input")
    public void inputPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("pos = atoi(msg->value); \n");
        context.append("myservo.write(pos); \n");
    }
}
