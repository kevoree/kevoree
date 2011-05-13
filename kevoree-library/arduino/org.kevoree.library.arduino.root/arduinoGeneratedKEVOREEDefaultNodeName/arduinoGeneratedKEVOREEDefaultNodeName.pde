#include <VirtualWire.h>
#undef int
#undef abs
#undef double
#undef float
#undef round
#include <Metro.h>
Metro metroTimerTimer1665635963= Metro(1000);
void setup(){
vw_set_rx_pin(5);
vw_set_tx_pin(6);
vw_setup(1200);
vw_rx_start();
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
if(msgString.startsWith("hub1064120763:")){
String contentString = String("r:")+ msgString ;
channel_hub1064120763_dispatch(contentString);
}
}
if (metroTimerTimer1665635963.check() == 1) {
component_Timer1665635963_requiredPort_tick("tick");
}
}
void channel_hub1064120763_dispatch(String param){
if(!param.startsWith("r:")){
String toSendMsg =  String("hub1064120763:")+param;
char msgContent[toSendMsg.length()+10];
toSendMsg.toCharArray(msgContent, toSendMsg.length()+1);
vw_send((uint8_t *)msgContent, strlen(msgContent));
vw_wait_tx();
}
}
void component_Timer1665635963_requiredPort_tick (String param){
channel_hub1064120763_dispatch(param);

}


