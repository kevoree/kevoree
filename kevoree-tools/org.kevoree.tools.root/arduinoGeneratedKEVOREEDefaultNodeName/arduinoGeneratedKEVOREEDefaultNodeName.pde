#include <VirtualWire.h>
#undef int
#undef abs
#undef double
#undef float
#undef round
#include <Metro.h>
Metro metroTimerTimer1652613859= Metro(10000);
#include <math.h> 
#include <Metro.h> 
float vcc = 4.91;
float pad = 9850;
float thermr = 10000;
void setup(){
vw_set_rx_pin(5);
vw_set_tx_pin(6);
vw_setup(1200);
vw_rx_start();
pinMode(13, OUTPUT);
}
void loop(){
uint8_t buf[VW_MAX_MESSAGE_LEN];
uint8_t buflen = VW_MAX_MESSAGE_LEN;
if (vw_get_message(buf, &buflen)){
int i;
char msg[buflen+10];
for (i = 0; i < buflen; i++){
msg[i] = buf[i];
}
String msgString =  String(msg);
if(msgString.startsWith("hub1902466538:")){
String contentString = String("r:")+ msgString ;
channel_hub1902466538_dispatch(contentString);
}
}
if (metroTimerTimer1652613859.check() == 1) {
component_Timer1652613859_requiredPort_tick("tick");
}
}
void channel_hub618561477_dispatch(String param){
component_LED1490340144_providedPort_off(param);
component_TempSensor1047252911_providedPort_trigger(param);
}
void channel_hub1902466538_dispatch(String param){
component_LED1490340144_providedPort_on(param);
if(!param.startsWith("r:")){
String toSendMsg =  String("hub1902466538:")+param;
char msgContent[toSendMsg.length()+10];
toSendMsg.toCharArray(msgContent, toSendMsg.length()+1);
vw_send((uint8_t *)msgContent, strlen(msgContent));
vw_wait_tx();
}
}
void component_Timer1652613859_requiredPort_tick (String param){
channel_hub618561477_dispatch(param);

}
void component_TempSensor1047252911_providedPort_trigger (String param){
    long Resistance;  
    float temp;  // Dual-Purpose variable to save space.
    int RawADC = analogRead(0);
    Resistance=((1024 * thermr / RawADC) - pad); 
    temp = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later
    temp = 1 / (0.001129148 + (0.000234125 * temp) + (0.0000000876741 * temp * temp * temp));
    temp = temp - 273.15;  // Convert Kelvin to Celsius  
    //send to output port
    String result = String(int(temp*100));component_TempSensor1047252911_requiredPort_temp(result);

}
void component_TempSensor1047252911_requiredPort_temp (String param){
channel_hub1902466538_dispatch(param);

}
void component_LED1490340144_providedPort_on (String param){
digitalWrite(13, HIGH);

}
void component_LED1490340144_providedPort_off (String param){
digitalWrite(13, LOW);

}


