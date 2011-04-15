int buttonState = LOW;
void setup(){
pinMode(12, OUTPUT);
pinMode(10, INPUT);
}
void loop(){
  int newButtonState = digitalRead(10);
  
  if (newButtonState == HIGH) { 
    if(buttonState == LOW){
      buttonState = HIGH;
      //DO ACTION
component_PushButton1954851656_requiredPort_click("click");
    }
  } else {
    if(buttonState == HIGH){
      buttonState = LOW;
      //DO ACTION UNRELEASE ACTION
component_PushButton1954851656_requiredPort_release("release");

    }
  }}
void channel_hub90679549_dispatch(String param){
component_LED328918496_providedPort_on(param);
}
void channel_hub2117615026_dispatch(String param){
component_LED328918496_providedPort_off(param);
}
void component_LED328918496_providedPort_on (String param){
digitalWrite(12, HIGH);

}
void component_LED328918496_providedPort_off (String param){
digitalWrite(12, LOW);

}
void component_PushButton1954851656_requiredPort_click (String param){
channel_hub90679549_dispatch(param);

}
void component_PushButton1954851656_requiredPort_release (String param){
channel_hub2117615026_dispatch(param);

}


