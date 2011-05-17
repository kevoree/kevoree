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
        context.append("" +
                "   //For analog read\n" +
                "   double value;\n" +
                "\n" +
                "   //Constants to convert ADC divisions into mains current values.\n" +
                "   double ADCvoltsperdiv = 0.0048;\n" +
                "   double VDoffset = 2.4476; //Initial value (corrected as program runs)\n" +
                "\n" +
                "   //Equation of the line calibration values\n" +
                "   double factorA = 15.2; //factorA = CT reduction factor / rsens\n" +
                "   double Ioffset = -0.08;\n" +
                "     \n" +
                "   //Constants set voltage waveform amplitude.\n" +
                "   double SetV = 230.0;\n" +
                "\n" +
                "   //Counter\n" +
                "   int i=0;\n" +
                "\n" +
                "   int samplenumber = 4000;\n" +
                " \n" +
                "   //Used for calculating real, apparent power, Irms and Vrms.\n" +
                "   double sumI=0.0;\n" +
                " \n" +
                "   int sum1i=0;\n" +
                "   double sumVadc=0.0;\n" +
                "\n" +
                "   double Vadc,Vsens,Isens,Imains,sqI,Irms;\n" +
                "   double apparentPower;" +
                "");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        
        /* Generate code for port */
        context.append("" +
                "value = analogRead(1);\n" +
                "   \n" +
                "   //Summing counter\n" +
                "   i++;\n" +
                "\n" +
                "   //Voltage at ADC\n" +
                "   Vadc = value * ADCvoltsperdiv;\n" +
                "\n" +
                "   //Remove voltage divider offset\n" +
                "   Vsens = Vadc-VDoffset;\n" +
                "\n" +
                "   //Current transformer scale to find Imains\n" +
                "   Imains = Vsens;\n" +
                "                  \n" +
                "   //Calculates Voltage divider offset.\n" +
                "   sum1i++; sumVadc = sumVadc + Vadc;\n" +
                "   if (sum1i>=1000) {VDoffset = sumVadc/sum1i; sum1i = 0; sumVadc=0.0;}\n" +
                "\n" +
                "   //Root-mean-square method current\n" +
                "   //1) square current values\n" +
                "   sqI = Imains*Imains;\n" +
                "   //2) sum \n" +
                "   sumI=sumI+sqI;\n" +
                "\n" +
                "   if (i>=samplenumber) \n" +
                "   {  \n" +
                "      i=0;\n" +
                "      //Calculation of the root of the mean of the current squared (rms)\n" +
                "      Irms = factorA*sqrt(sumI/samplenumber)+Ioffset;\n" +
                "\n" +
                "      //Calculation of the root of the mean of the voltage squared (rms)                     \n" +
                "      apparentPower = Irms * SetV;\n" +
                "\n");

        context.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(this.getName(), "currentW", PortUsage.required()));
        //GENERATE PARAMETER
        context.append("(String(Irms));\n");

        context.append(

                " \n" +
                "      //Reset values ready for next sample.\n" +
                "      sumI=0.0;\n" +
                " \n" +
                "   }");

        
        //GENERATE METHOD CALL


    }

}
