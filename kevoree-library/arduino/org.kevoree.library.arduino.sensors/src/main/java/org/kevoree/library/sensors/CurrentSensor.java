package org.kevoree.library.sensors;


import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.arduinoNodeType.ArduinoMethodHelper;
import org.kevoree.library.arduinoNodeType.PortUsage;

@Library(name = "KevoreeArduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})
@Provides({
    @ProvidedPort(name = "trigger", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "currentW", type = PortType.MESSAGE)
})
public class CurrentSensor extends AbstractComponentType {

    @Start
    public void start() {
    }

    @Stop
    public void stop() {

    }

    @Generate("header")
    public void generateHeader(StringBuffer context) {
        context.append("\n" +
                "   double SetV = 230.0;\n" +
                "   int samplenumber = 2500;\n" +
                "   double ADCvoltsperdiv = 0.0048;\n" +
                "   double VDoffset = 2.4476; \n" +
                "   double factorA = 10.5; \n" +
                "   double Ioffset = -0.013;\n" +

                "\n");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        
        /* Generate code for port */
        context.append("\n" +
                "  int i=0;\n" +
                "   double sumI=0.0;\n" +
                "   double Vadc,Vsens,Isens,Imains,sqI,Irms;\n" +
                "   double sumVadc=0.0;\n" +
                "   \n" +
                "   int sum1i=0;\n" +
                "  double apparentPower;\n" +
                "  while(i<samplenumber){\n" +
                "    double value = analogRead(1);\n" +
                "    i++;\n" +
                "    //Voltage at ADC\n" +
                "    Vadc = value * ADCvoltsperdiv;\n" +
                "    //Remove voltage divider offset\n" +
                "    Vsens = Vadc-VDoffset;\n" +
                "    //Current transformer scale to find Imains\n" +
                "    Imains = Vsens;          \n" +
                "    //Calculates Voltage divider offset.\n" +
                "    sum1i++; sumVadc = sumVadc + Vadc;\n" +
                "    if (sum1i>=1000) {VDoffset = sumVadc/sum1i; sum1i = 0; sumVadc=0.0;}\n" +
                "    //Root-mean-square method current\n" +
                "    //1) square current values\n" +
                "    sqI = Imains*Imains;\n" +
                "    //2) sum \n" +
                "    sumI=sumI+sqI;\n" +
                "  }\n" +
                "  Irms = factorA*sqrt(sumI/samplenumber)+Ioffset;\n" +
                "  apparentPower = Irms * SetV;\n" +
                "  if(apparentPower < 0){apparentPower = 0;}\n" +
                "  if(apparentPower > 3000){apparentPower = 0;}" +

                "\n");

        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "currentW", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(String(int(apparentPower)));\n");



    }

}
