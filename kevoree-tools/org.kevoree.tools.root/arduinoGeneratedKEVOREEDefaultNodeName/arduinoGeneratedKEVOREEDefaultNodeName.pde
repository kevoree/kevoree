#define BUFFERSIZE 100
char inBytes[BUFFERSIZE];
int serialIndex = 0;
int minValueAutoNormalizer216882710= 1024;
int maxValueAutoNormalizer216882710= 0;
#include <Metro.h>
Metro metroTimerTimer397711199= Metro(1000);
void setup(){
Serial.begin(9600);
}
void loop(){
while(Serial.available() && serialIndex < BUFFERSIZE) {
    inBytes[serialIndex] = Serial.read();   
    if (inBytes[serialIndex] == '\n' || inBytes[serialIndex] == ';' || inBytes[serialIndex] == '>') { //Use ; when using Serial Monitor
       inBytes[serialIndex] = '\0'; //end of string char
       String result = String(inBytes);
channel_hub1368903331_dispatch(result);
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
if (metroTimerTimer397711199.check() == 1) {
component_Timer397711199_requiredPort_tick("tick");
}
}
void channel_hub1266981408_dispatch(String param){
component_LightSensor2113439111_providedPort_trigger(param);
}
void channel_hub1068260605_dispatch(String param){
component_AutoNormalizer216882710_providedPort_input(param);
}
void channel_hub1368903331_dispatch(String param){
Serial.println(param);
}
void component_AutoNormalizer216882710_providedPort_input (String param){
  char msg[param.length()+10];
  param.toCharArray(msg, param.length()+1);
  float value = atof(msg);
if(value < minValueAutoNormalizer216882710){ minValueAutoNormalizer216882710 = value; }
if(value > maxValueAutoNormalizer216882710){ maxValueAutoNormalizer216882710 = value; }
float result= ((value-minValueAutoNormalizer216882710) / (maxValueAutoNormalizer216882710-minValueAutoNormalizer216882710))*100;
component_AutoNormalizer216882710_requiredPort_norm(String(int(result)));

}
void component_AutoNormalizer216882710_requiredPort_norm (String param){
channel_hub1368903331_dispatch(param);

}
void component_LightSensor2113439111_providedPort_trigger (String param){
int photocellReading = analogRead(2);
component_LightSensor2113439111_requiredPort_light(String(photocellReading));

}
void component_LightSensor2113439111_requiredPort_light (String param){
channel_hub1068260605_dispatch(param);

}
void component_Timer397711199_requiredPort_tick (String param){
channel_hub1266981408_dispatch(param);

}


