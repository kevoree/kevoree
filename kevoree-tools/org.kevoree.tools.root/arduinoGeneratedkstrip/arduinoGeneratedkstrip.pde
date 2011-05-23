#define BUFFERSIZE 100
char inBytes[BUFFERSIZE];
int serialIndex = 0;

   double SetV = 230.0;
   int samplenumber = 2500;
   double ADCvoltsperdiv = 0.0048;
   double VDoffset = 2.4476; 
   double factorA = 10.5; 
   double Ioffset = -0.013;

#include <Metro.h>
Metro metroTimerTimer463017337= Metro(1000);
int minValueAutoNormalizer855711364= 0;
int maxValueAutoNormalizer855711364= 0;
void setup(){
Serial.begin(9600);
}
void loop(){
while(Serial.available() && serialIndex < BUFFERSIZE) {
    inBytes[serialIndex] = Serial.read();   
    if (inBytes[serialIndex] == '\n' || inBytes[serialIndex] == ';' || inBytes[serialIndex] == '>') { //Use ; when using Serial Monitor
       inBytes[serialIndex] = '\0'; //end of string char
       String result = String(inBytes);
channel_hub1357361681_dispatch(result);
       serialIndex = 0;
    }
    else{
      serialIndex++;
    }
  }
  
  if(serialIndex >= BUFFERSIZE){
    //buffer overflow, reset the buffer and do nothing
    //TODO: perhaps some sort of feedback to the user?
    for(int j=0; j < BUFFERSIZE; j++){
      inBytes[j] = 0;
      serialIndex = 0;
    }
  }
if (metroTimerTimer463017337.check() == 1) {
component_Timer463017337_requiredPort_tick("tick");
}
}
void channel_hub1723676850_dispatch(String param){
component_CurrentSensor686635544_providedPort_trigger(param);
component_LightSensor223290182_providedPort_trigger(param);
}
void channel_hub610086661_dispatch(String param){
component_AutoNormalizer855711364_providedPort_input(param);
}
void channel_hub1357361681_dispatch(String param){
Serial.println(param);
}
void component_CurrentSensor686635544_providedPort_trigger (String param){

  int i=0;
   double sumI=0.0;
   double Vadc,Vsens,Isens,Imains,sqI,Irms;
   double sumVadc=0.0;
   
   int sum1i=0;
  double apparentPower;
  while(i<samplenumber){
    double value = analogRead(1);
    i++;
    //Voltage at ADC
    Vadc = value * ADCvoltsperdiv;
    //Remove voltage divider offset
    Vsens = Vadc-VDoffset;
    //Current transformer scale to find Imains
    Imains = Vsens;          
    //Calculates Voltage divider offset.
    sum1i++; sumVadc = sumVadc + Vadc;
    if (sum1i>=1000) {VDoffset = sumVadc/sum1i; sum1i = 0; sumVadc=0.0;}
    //Root-mean-square method current
    //1) square current values
    sqI = Imains*Imains;
    //2) sum 
    sumI=sumI+sqI;
  }
  Irms = factorA*sqrt(sumI/samplenumber)+Ioffset;
  apparentPower = Irms * SetV;
  if(apparentPower < 0){apparentPower = 0;}
  if(apparentPower > 3000){apparentPower = 0;}
component_CurrentSensor686635544_requiredPort_currentW(String(int(apparentPower)));

}
void component_CurrentSensor686635544_requiredPort_currentW (String param){
}
void component_Timer463017337_requiredPort_tick (String param){
channel_hub1723676850_dispatch(param);

}


