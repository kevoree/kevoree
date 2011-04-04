#include <math.h> 
#include <Metro.h> 
#define ThermistorPIN 0 
float vcc = 4.91;
float pad = 9850;
float thermr = 10000;
#include <Metro.h>
Metro metroTimerTimer172103160= Metro(100);
void setup(){
Serial.begin(9600);
}
void loop(){
if (metroTimerTimer172103160.check() == 1) {
component_Timer172103160_requiredPort_tick("tick");
}
}
void channel_hub1495367792_dispatch(String param){
component_TempSensor40911417_providedPort_trigger(param);
}
void channel_hub259067590_dispatch(String param){
Serial.println(param);
}
void component_TempSensor40911417_providedPort_trigger (String param){
    long Resistance;  
    float temp;  // Dual-Purpose variable to save space.
    int RawADC = analogRead(ThermistorPIN);
    Resistance=((1024 * thermr / RawADC) - pad); 
    temp = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later
    temp = 1 / (0.001129148 + (0.000234125 * temp) + (0.0000000876741 * temp * temp * temp));
    temp = temp - 273.15;  // Convert Kelvin to Celsius  
    //send to output port
    String result = String(int(temp*100));component_TempSensor40911417_requiredPort_temp(result);

}
void component_TempSensor40911417_requiredPort_temp (String param){
channel_hub259067590_dispatch(param);

}
void component_Timer172103160_requiredPort_tick (String param){
channel_hub1495367792_dispatch(param);

}


