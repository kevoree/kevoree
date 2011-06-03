package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

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
    @Stop
    public void dummy() {
    }

    @Generate("classheader")
    public void generateHeader(StringBuffer context) {
        context.append(
                "   double SetV;\n"
                + "   int samplenumber;\n"
                + "   double ADCvoltsperdiv;\n"
                + "   double VDoffset; \n"
                + "   double factorA; \n"
                + "   double Ioffset;\n");
    }

    @Generate("classinit")
    public void generateInit(StringBuffer context) {
        context.append(
                "SetV = 230.0;\n"
                + "samplenumber = 2500;\n"
                + "ADCvoltsperdiv = 0.0048;\n"
                + "VDoffset = 2.4476; \n"
                + "factorA = 9.5; \n"
                + "Ioffset = -0.013;\n");
    }

    @Port(name = "trigger")
    public void triggerPort(Object o) {
        StringBuffer context = (StringBuffer) o;
        context.append("\n"
                + "  int i=0;\n"
                + "   double sumI=0.0;\n"
                + "   double Vadc,Vsens,Isens,Imains,sqI,Irms;\n"
                + "   double sumVadc=0.0;\n"
                + "   \n"
                + "   int sum1i=0;\n"
                + "  double apparentPower;\n"
                + "  while(i<samplenumber){\n"
                + "    double value = analogRead(atoi(pin));\n"
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


        context.append("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        context.append("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        context.append("char buf[255];\n");
        context.append("sprintf(buf,\"%d\",int(apparentPower));\n");
        context.append("smsg->value = buf;\n");
        context.append("smsg->metric=\"watt\";");
        context.append("currentW_rport(smsg);");
        context.append("free(smsg);");

        context.append("\n");



    }
}
