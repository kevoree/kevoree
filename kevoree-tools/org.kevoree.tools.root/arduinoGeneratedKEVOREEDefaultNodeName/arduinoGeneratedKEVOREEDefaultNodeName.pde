#include <LiquidCrystal.h> 
LiquidCrystal lcd(10, 11, 12, 13, 14, 15, 16);
void setup(){
lcd.begin(16, 2);
}
void loop(){
}
void component_LCDDisplay263385554_providedPort_input (String param){
lcd.clear();
lcd.print(param);

}


