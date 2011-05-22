#include <VirtualWire.h>
#undef int
#undef abs
#undef double
#undef float
#undef round
#include <Metro.h>
Metro metroTimerTimer1496768335= Metro(200);
int minValueAutoNormalizer2000214989= 1024;
int maxValueAutoNormalizer2000214989= 0;
void setup(){
vw_set_rx_pin(5);
vw_set_tx_pin(11);
vw_set_ptt_pin(-1);vw_setup(1200);
vw_rx_start();
Serial.begin(9600);
pinMode(A0, INPUT);
digitalWrite(A0,HIGH); 
}
void loop(){
uint8_t buf[VW_MAX_MESSAGE_LEN];
uint8_t buflen = VW_MAX_MESSAGE_LEN;
if (vw_get_message(buf, &buflen)){
int i;
char msg[buflen+1];
for (i = 0; i < buflen; i++){
msg[i] = buf[i];
}
msg[i]='\0';
String msgString =  String(msg);
if(msgString.startsWith("hub1138934433:")){
String contentString = String("r:")+ msgString.substring(14) ;
channel_hub1138934433_dispatch(contentString);
}
}
if (metroTimerTimer1496768335.check() == 1) {
component_Timer1496768335_requiredPort_tick("tick");
}
}
void channel_hub950951946_dispatch(String param){
component_AnalogSlider1500292526_providedPort_trigger(param);
}
void channel_hub1696571387_dispatch(String param){
component_AutoNormalizer2000214989_providedPort_input(param);
}
void channel_hub1138934433_dispatch(String param){
if(!param.startsWith("r:")){
String toSendMsg =  String("hub1138934433:")+param;
char msgContent[toSendMsg.length()+10];
toSendMsg.toCharArray(msgContent, toSendMsg.length()+1);
vw_send((uint8_t *)msgContent, strlen(msgContent));
vw_wait_tx();
} else {
} 
}
void component_Timer1496768335_requiredPort_tick (String param){
channel_hub950951946_dispatch(param);

}
void component_AnalogSlider1500292526_providedPort_trigger (String param){
int sensorValue = 1024-analogRead(A0);

    if(sensorValue < 80){
     sensorValue = 0; 
    } else {
     sensorValue = sensorValue - 80; 
    }
component_AnalogSlider1500292526_requiredPort_level(String(sensorValue));

}
void component_AnalogSlider1500292526_requiredPort_level (String param){
channel_hub1696571387_dispatch(param);

}
void component_AutoNormalizer2000214989_providedPort_input (String param){
  char msg[param.length()+10];
  param.toCharArray(msg, param.length()+1);
  float value = atof(msg);
if(value < minValueAutoNormalizer2000214989){ minValueAutoNormalizer2000214989 = value; }
if(value > maxValueAutoNormalizer2000214989){ maxValueAutoNormalizer2000214989 = value; }
float result= ((value-minValueAutoNormalizer2000214989) / (maxValueAutoNormalizer2000214989-minValueAutoNormalizer2000214989))*100;
component_AutoNormalizer2000214989_requiredPort_norm(String(int(result)));

}
void component_AutoNormalizer2000214989_requiredPort_norm (String param){
channel_hub1138934433_dispatch(param);

}


