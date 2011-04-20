#define BUFFERSIZE 100
char inBytes[BUFFERSIZE];
int serialIndex = 0;
#include <Metro.h>
Metro metroTimerTimer1393826676= Metro(1000);
void setup(){
Serial.begin(9600);
}
void loop(){
while(Serial.available() && serialIndex < BUFFERSIZE) {
    inBytes[serialIndex] = Serial.read();   
    if (inBytes[serialIndex] == '\n' || inBytes[serialIndex] == ';' || inBytes[serialIndex] == '>') { //Use ; when using Serial Monitor
       inBytes[serialIndex] = '\0'; //end of string char
       String result = String(inBytes);
channel_hub847052608_dispatch(result);
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
if (metroTimerTimer1393826676.check() == 1) {
component_Timer1393826676_requiredPort_tick("tick");
}
}
void channel_hub847052608_dispatch(String param){
Serial.println(param);
}
void component_Timer1393826676_requiredPort_tick (String param){
channel_hub847052608_dispatch(param);

}


