package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"})
})
@Requires({
        @RequiredPort(name = "currentW", type = PortType.MESSAGE, needCheckDependency = false)
})
public class CurrentSensor extends AbstractPeriodicArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement(
                "     char buf[7]; \n"
                        + "   double SetV;\n"
                        + "   int samplenumber;\n"
                        + "   double ADCvoltsperdiv;\n"
                        + "   double VDoffset; \n"
                        + "   double factorA; \n"
                        + "   double Ioffset;\n");
        getGenerator().appendNativeStatement("double Vadc,Vsens,Isens,Imains,sqI,Irms;\n");
        getGenerator().appendNativeStatement("double sumI;double value;\n");
        getGenerator().appendNativeStatement("double sumVadc;double apparentPower;\n");
        getGenerator().appendNativeStatement("int i;int sum1i;\n");
    }


    @Override
    public void generateInit(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement(
                "SetV = 230.0;\n"
                        + "samplenumber = 2500;\n"
                        + "ADCvoltsperdiv = 0.0048;\n"
                        + "VDoffset = 2.4476; \n"
                        + "factorA = 9.5; \n"
                        + "Ioffset = -0.013;\n");
    }

    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator) {
        getGenerator().appendNativeStatement("\n"
                + "   i=0;sumI=0.0;sumVadc=0.0;sum1i=0;\n"
                + "  while(i<samplenumber){\n"
                + "    value = analogRead(atoi(pin));\n"
                + "    i++;\n"
                + "    //Voltage at ADC\n"
                + "    Vadc = value * ADCvoltsperdiv;\n"
                + "    //Remove voltage divider offset\n"
                + "    Vsens = Vadc-VDoffset;\n"
                + "    //Current transformer scale to find Imains\n"
                + "    Imains = Vsens;          \n"
                + "    //Calculates Voltage divider offset.\n"
                + "    sum1i++; sumVadc = sumVadc + Vadc;\n"
                + "    if (sum1i>=1000) {VDoffset = sumVadc/sum1i; sum1i = 0; sumVadc=0.0;}\n"
                + "    //Root-mean-square method current\n"
                + "    //1) square current values\n"
                + "    sqI = Imains*Imains;\n"
                + "    //2) sum \n"
                + "    sumI=sumI+sqI;\n"
                + "  }\n"
                + "  Irms = factorA*sqrt(sumI/samplenumber)+Ioffset;\n"
                + "  apparentPower = Irms * SetV;\n"
                + "  if(apparentPower < 0){apparentPower = 0;}\n"
                + "  if(apparentPower > 3000){apparentPower = 0;}"
                + "\n");


        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",int(apparentPower));\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"watt\";");
        getGenerator().appendNativeStatement("currentW_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");

        getGenerator().appendNativeStatement("\n");
    }
}
