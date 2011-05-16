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
        @RequiredPort(name = "mvolt", type = PortType.MESSAGE)
})
public class InternalVoltmeter extends AbstractComponentType {

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
                "  ADMUX = _BV(REFS0) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);\n" +
                "  delay(2); // Wait for Vref to settle\n" +
                "  ADCSRA |= _BV(ADSC); // Convert\n" +
                "  while (bit_is_set(ADCSRA,ADSC));\n" +
                "  result = ADCL;\n" +
                "  result |= ADCH<<8;\n" +
                "  result = 1126400L / result; // Back-calculate AVcc in mV\n" +
                "  \n" +
                "  String sresult = String(result);");

        //GENERATE METHOD CALL
        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "mvolt", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(sresult);\n");

    }

}
