package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

@Library(name = "KevoreeArduino")
@ComponentType
@Provides({
        @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "temp", type = PortType.MESSAGE)
})
public class InternalTemp extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }


    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;

        context.append("  " +
                "long result;\n" +
                "  // Read temperature sensor against 1.1V reference\n" +
                "  ADMUX = _BV(REFS1) | _BV(REFS0) | _BV(MUX3);\n" +
                "  delay(2); // Wait for Vref to settle\n" +
                "  ADCSRA |= _BV(ADSC); // Convert\n" +
                "  while (bit_is_set(ADCSRA,ADSC));\n" +
                "  result = ADCL;\n" +
                "  result |= ADCH<<8;\n" +
                "  result = (result - 125) * 1075;\n" +
                "  ");
        context.append("String sresult = String(result);\n");

        //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "temp", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(sresult);\n");

    }

}
