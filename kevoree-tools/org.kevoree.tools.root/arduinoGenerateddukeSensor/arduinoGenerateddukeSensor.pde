#include <LiquidCrystal.h> 
LiquidCrystal lcd(10, 11, 12, 13, 14, 15, 16);
#include <Metro.h>
Metro metroTimerTimer905692769= Metro(300);
void setup(){
lcd.begin(16, 2);
}
void loop(){
if (metroTimerTimer905692769.check() == 1) {
component_Timer905692769_requiredPort_tick("tick");
}
}
void channel_hub1033316929_dispatch(String param){
component_LightSensor910075105_providedPort_trigger(param);
}
void channel_hub739186673_dispatch(String param){
component_LCDDisplay1494335808_providedPort_input(param);
}
void component_LCDDisplay1494335808_providedPort_input (String param){
lcd.clear();
lcd.print(param);

}
void component_LightSensor910075105_providedPort_trigger (String param){
int photocellReading = analogRead(5);
component_LightSensor910075105_requiredPort_light(String(photocellReading)+" lux");

}
void component_LightSensor910075105_requiredPort_light (String param){
channel_hub739186673_dispatch(param);

}
void component_Timer905692769_requiredPort_tick (String param){
channel_hub1033316929_dispatch(param);

}


