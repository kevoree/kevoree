#include <Metro.h>
Metro metroTimerTimer1847782100= Metro(1000);
void setup(){
Serial.begin(9600);
}
void loop(){
if (metroTimerTimer1847782100.check() == 1) {
component_Timer1847782100_requiredPort_tick("tick");
}
}
void channel_hub1301978630_dispatch(String param){
Serial.println(param);
}
void component_Timer1847782100_requiredPort_tick (String param){
channel_hub1301978630_dispatch(param);

}


