#define BUFFERSIZE 100
char inBytes[BUFFERSIZE];
int serialIndex = 0;
#include <Metro.h>
Metro metroTimerTimer359100388= Metro(1000);
void setup(){
Serial.begin(9600);
}
void loop(){
while(Serial.available() && serialIndex < BUFFERSIZE) {
    inBytes[serialIndex] = Serial.read();   
    if (inBytes[serialIndex] == '\n' || inBytes[serialIndex] == ';' || inBytes[serialIndex] == '>') { //Use ; when using Serial Monitor
       inBytes[serialIndex] = '\0'; //end of string char
       String result = String(inBytes);
channel_hub722142755_dispatch(result);
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
if (metroTimerTimer359100388.check() == 1) {
component_Timer359100388_requiredPort_tick("tick");
}
}
void channel_hub1112877189_dispatch(String param){
component_InternalTemp1485120577_providedPort_trigger(param);
component_InternalVoltmeter594938378_providedPort_trigger(param);
}
void channel_hub722142755_dispatch(String param){
Serial.println(param);
}
void component_InternalTemp1485120577_providedPort_trigger (String param){
  long result;
  // Read temperature sensor against 1.1V reference
  ADMUX = _BV(REFS1) | _BV(REFS0) | _BV(MUX3);
  delay(2); // Wait for Vref to settle
  ADCSRA |= _BV(ADSC); // Convert
  while (bit_is_set(ADCSRA,ADSC));
  result = ADCL;
  result |= ADCH<<8;
  result = (result - 125) * 1075;
  String sresult = String(result);
component_InternalTemp1485120577_requiredPort_temp(sresult);

}
void component_InternalTemp1485120577_requiredPort_temp (String param){
channel_hub722142755_dispatch(param);

}
void component_Timer359100388_requiredPort_tick (String param){
channel_hub1112877189_dispatch(param);

}
void component_InternalVoltmeter594938378_providedPort_trigger (String param){
  long result;
  ADMUX = _BV(REFS0) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);
  delay(2); // Wait for Vref to settle
  ADCSRA |= _BV(ADSC); // Convert
  while (bit_is_set(ADCSRA,ADSC));
  result = ADCL;
  result |= ADCH<<8;
  result = 1126400L / result; // Back-calculate AVcc in mV
  
  String sresult = String(result);component_InternalVoltmeter594938378_requiredPort_mvolt(sresult);

}
void component_InternalVoltmeter594938378_requiredPort_mvolt (String param){
channel_hub722142755_dispatch(param);

}


