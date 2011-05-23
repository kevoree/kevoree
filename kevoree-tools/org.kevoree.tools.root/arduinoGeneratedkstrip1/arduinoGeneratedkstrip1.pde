#include <VirtualWire.h>
#undef int
#undef abs
#undef double
#undef float
#undef round
#include <DmxSimple.h>
void setup(){
vw_set_rx_pin(5);
vw_set_tx_pin(11);
vw_set_ptt_pin(-1);vw_setup(1200);
vw_rx_start();
DmxSimple.usePin(2);
DmxSimple.maxChannel(4);
DmxSimple.write(2,255);
DmxSimple.write(3,255);
DmxSimple.write(4,255);
DmxSimple.write(1,127);
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
}
void channel_hub1138934433_dispatch(String param){
if(!param.startsWith("r:")){
String toSendMsg =  String("hub1138934433:")+param;
char msgContent[toSendMsg.length()+10];
toSendMsg.toCharArray(msgContent, toSendMsg.length()+1);
vw_send((uint8_t *)msgContent, strlen(msgContent));
vw_wait_tx();
} else {
component_DMXLight308087821_providedPort_intensity(param.substring(2));
} 
}
void component_DMXLight308087821_providedPort_intensity (String param){
  char msg[param.length()+10];
param.toCharArray(msg, param.length()+1);
int value = atoi(msg);
int outputIntensity = (127 * value) / 100 ; 
 DmxSimple.write(1,outputIntensity);

}
void component_DMXLight308087821_providedPort_color (String param){

}


