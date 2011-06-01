#include <QueueList.h>
#include <avr/pgmspace.h>
//Global Kevoree Type Defintion declaration
prog_char digitallight[] PROGMEM = "DigitalLight";
prog_char timer[] PROGMEM = "Timer";
prog_char localchannel[] PROGMEM = "LocalChannel";
PROGMEM const char * typedefinition[] = { 
digitallight
,timer
,localchannel
};
//Global Kevoree Port Type Defintion declaration
prog_char on[] PROGMEM = "on";
prog_char off[] PROGMEM = "off";
prog_char toggle[] PROGMEM = "toggle";
prog_char tick[] PROGMEM = "tick";
PROGMEM const char * portdefinition[] = { 
on
,off
,toggle
,tick
};

const int nbPortType = 4;
int getIDFromPortName(char * portName){
  for(int i=0;i<nbPortType;i++){
   if(strcmp_P(portName, (char*)pgm_read_word(&(portdefinition[i]))  )==0) { return i; }
  }
  return -1;
}
const int nbTypeDef = 3;
int getIDFromType(char * typeName){
  for(int i=0;i<nbTypeDef;i++){
   if(strcmp_P(typeName, (char*)pgm_read_word(&(typedefinition[i]))  )==0) { return i; }
  }
  return -1;
}
struct kmessage {char* value;char* metric;};
class KevoreeType { public : int subTypeCode; char instanceName[50]; };
struct kbinding { char instanceName[50];int portCode; QueueList<kmessage> * port;   };
class kbindings {
 public:
 kbinding ** bindings;
 int nbBindings;
 void init(){ nbBindings = 0; }
 int addBinding( char * instanceName , int portCode , QueueList<kmessage> * port){
   kbinding * newBinding = (kbinding*) malloc(sizeof(kbinding));
   if(newBinding){
      memset(newBinding, 0, sizeof(kbinding));
   }
   strcpy(newBinding->instanceName,instanceName);
   newBinding->portCode,portCode;
   newBinding->port = port;
   kbinding ** newBindings =  (kbinding**) malloc(  (nbBindings+1) * sizeof(kbinding*) );
   if (!newBindings) { return -1;}
   for (int idx=0;idx < nbBindings ; idx++ ){ newBindings[idx] = bindings[idx]; }
   newBindings[nbBindings] = newBinding;
   if(bindings){ free(bindings); }
   bindings = newBindings;
   nbBindings ++;
   return nbBindings-1;
 }
 boolean removeBinding( char * instanceName , int portCode ){
   //SEARCH INDEX
   int indexToRemove = -1;
   for(int i=0;i<nbBindings;i++){
     kbinding * binding = bindings[i];
     if( String(binding->instanceName) == String(instanceName) && binding->portCode == portCode){ indexToRemove = i; }
   }
   if(indexToRemove == -1){return -1;} else { free(bindings[indexToRemove]); }
   kbinding** newBindings =  (kbinding**) malloc(  (nbBindings-1) *sizeof(kbinding*) );
   if (!newBindings) { return false;}
   for (int idx=0;idx < nbBindings ; idx++ ){ 
    if(idx < indexToRemove){ newBindings[idx] = bindings[idx]; }
    if(idx > indexToRemove){ newBindings[idx-1] = bindings[idx]; }
   }
   if(bindings){ free(bindings); }
   bindings = newBindings; 
  nbBindings--;
  return true;  
 } 
void destroy(){
 for(int i=0;i<nbBindings;i++){ free(bindings[i]); } 
 free(bindings);
}
};
class DigitalLight : public KevoreeType {
 public : 
boolean state ;

QueueList<kmessage> * on;
QueueList<kmessage> * off;
QueueList<kmessage> * toggle;
char pin[50];
void init(){
on = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(on){
   memset(on, 0, sizeof(QueueList<kmessage>));
}
off = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(off){
   memset(off, 0, sizeof(QueueList<kmessage>));
}
toggle = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(toggle){
   memset(toggle, 0, sizeof(QueueList<kmessage>));
}
}
void destroy(){
free(on);
free(off);
free(toggle);
}
void runInstance(){
if(!on->isEmpty()){
kmessage * msg = &(on->pop());
DigitalLight::on_pport(msg);
}
if(!off->isEmpty()){
kmessage * msg = &(off->pop());
DigitalLight::off_pport(msg);
}
if(!toggle->isEmpty()){
kmessage * msg = &(toggle->pop());
DigitalLight::toggle_pport(msg);
}
}
void on_pport(kmessage * msg){
pinMode(atoi(pin), OUTPUT);digitalWrite(atoi(pin), HIGH);

}
void off_pport(kmessage * msg){
pinMode(atoi(pin), OUTPUT);digitalWrite(atoi(pin), LOW);

}
void toggle_pport(kmessage * msg){
int newState = 0;
if(state){ newState = LOW; } else { newState=HIGH; }state = ! state; pinMode(atoi(pin), OUTPUT);digitalWrite(atoi(pin), newState);

}
};
class Timer : public KevoreeType {
 public : 
unsigned long nextExecution;
QueueList<kmessage> * tick;
char period[50];
void init(){
nextExecution = millis();
}
void destroy(){
}
void runInstance(){
kmessage * msg = (kmessage*) malloc(sizeof(kmessage));
if (msg){memset(msg, 0, sizeof(kmessage));}
msg->value = "tick";msg->metric = "t";tick_rport(msg);free(msg);
nextExecution += atol(period);
}
void tick_rport(kmessage * msg){
if(tick){
tick->push(*msg);
}
}
};
class LocalChannel : public KevoreeType {
 public : 
QueueList<kmessage> * input;
kbindings * bindings;
void init(){
input = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(input){
   memset(input, 0, sizeof(QueueList<kmessage>));
}
bindings = (kbindings *) malloc(sizeof(kbindings));
if(bindings){
   memset(bindings, 0, sizeof(kbindings));
}
}
void destroy(){
free(input);
bindings->destroy();
free(bindings);
}
void runInstance(){
if(!input->isEmpty()){
kmessage * msg = &(input->pop());
LocalChannel::dispatch(msg);
}
}
void dispatch(kmessage * msg){
for(int i=0;i<bindings->nbBindings;i++){    bindings->bindings[i]->port->push(*msg);}
}
};
KevoreeType ** instances; //GLOBAL INSTANCE DYNAMIC ARRAY
int nbInstances = 0; //GLOBAL NB INSTANCE
KevoreeType * tempInstance; //TEMP INSTANCE POINTER
int addInstance(){ //TECHNICAL HELPER ADD INSTANCE
  if(tempInstance){
    KevoreeType** newInstances =  (KevoreeType**) malloc(  (nbInstances+1) *sizeof(KevoreeType*) );
    if (!newInstances) { return -1;}
    for (int idx=0;idx < nbInstances ; idx++ ){ newInstances[idx] = instances[idx]; }
    newInstances[nbInstances] = tempInstance;
    if(instances){ free(instances); }
    instances = newInstances;
    tempInstance = NULL;
    nbInstances ++;
    return nbInstances-1;
  }
  return -1;
}
boolean removeInstance(int index){
destroyInstance(index);
KevoreeType** newInstances =  (KevoreeType**) malloc(  (nbInstances-1) *sizeof(KevoreeType*) );
if (!newInstances) { return false;}
for (int idx=0;idx < nbInstances ; idx++ ){ 
  if(idx < index){ newInstances[idx] = instances[idx]; }
  if(idx > index){ newInstances[idx-1] = instances[idx]; }
}
if(instances){ free(instances); }
instances = newInstances; 
nbInstances--;return true;
}
boolean destroyInstance(int index){
KevoreeType * instance = instances[index];
 switch(instance->subTypeCode){
case 1:{
((DigitalLight*) instances[index])->destroy();
break;}
case 2:{
((Timer*) instances[index])->destroy();
break;}
case 3:{
((LocalChannel*) instances[index])->destroy();
break;}
}
free(instance);
}
void updateParam(int index,int typeCode,char * key,char * val){
 switch(typeCode){
case 1:{
DigitalLight * instance = (DigitalLight*) instances[index];
if(String(key) == "pin"){
strcpy (instance->pin,val);
}
break;}
case 2:{
Timer * instance = (Timer*) instances[index];
if(String(key) == "period"){
strcpy (instance->period,val);
}
break;}
case 3:{
LocalChannel * instance = (LocalChannel*) instances[index];
break;}
}
}
void updateParams(int index,char* params){
  int typeCode = instances[index]->subTypeCode;
  char *p = params;
  char * str;
  while ((str = strtok_r(p, ",", &p)) != NULL){
    char * str2;
    char* keyval[2];
    int keyvalIndex = 0;
    while ((str2 = strtok_r(str, "=", &str)) != NULL){
      keyval[keyvalIndex] = str2;
      keyvalIndex ++;
    }
    updateParam(index,typeCode,keyval[0],keyval[1]);
  }
}
int createInstance(int typeCode,char* instanceName,char* params){
switch(typeCode){
case 0:{
  DigitalLight * newInstance = (DigitalLight*) malloc(sizeof(DigitalLight));
  if (newInstance){
    memset(newInstance, 0, sizeof(DigitalLight));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeCode = 0; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
break;}
case 1:{
  Timer * newInstance = (Timer*) malloc(sizeof(Timer));
  if (newInstance){
    memset(newInstance, 0, sizeof(Timer));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeCode = 1; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
break;}
case 2:{
  LocalChannel * newInstance = (LocalChannel*) malloc(sizeof(LocalChannel));
  if (newInstance){
    memset(newInstance, 0, sizeof(LocalChannel));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeCode = 2; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
break;}
}
 return -1;
}
void runInstance(int index){
int typeCode = instances[index]->subTypeCode;
 switch(typeCode){
case 1:{
DigitalLight * instance = (DigitalLight*) instances[index];
instance->runInstance();
break;}
case 2:{
Timer * instance = (Timer*) instances[index];
instance->runInstance();
break;}
case 3:{
LocalChannel * instance = (LocalChannel*) instances[index];
instance->runInstance();
break;}
}
}
void bind(int indexComponent,int indexChannel,int portCode){
QueueList<kmessage> * providedPort = 0;
QueueList<kmessage> ** requiredPort = 0;
char * componentInstanceName;
int componentTypeCode = instances[indexComponent]->subTypeCode;
int channelTypeCode = instances[indexChannel]->subTypeCode;
 switch(componentTypeCode){
case 1:{
DigitalLight * instance = (DigitalLight*) instances[indexComponent];
switch(portCode){
case 0:{
   providedPort=instance->on;
componentInstanceName=instance->instanceName;
break;}
case 1:{
   providedPort=instance->off;
componentInstanceName=instance->instanceName;
break;}
case 2:{
   providedPort=instance->toggle;
componentInstanceName=instance->instanceName;
break;}
}
break;}
case 2:{
Timer * instance = (Timer*) instances[indexComponent];
switch(portCode){
case 3:{
   requiredPort=&instance->tick;
break;}
}
break;}
}
 switch(channelTypeCode){
case 3:{
LocalChannel * instance = (LocalChannel*) instances[indexChannel];
if(providedPort){
instance->bindings->addBinding(componentInstanceName,portCode,providedPort);
}
if(requiredPort){
*requiredPort = instance->input;
}
break;}
}
}
void unbind(int indexComponent,int indexChannel,int portCode){
QueueList<kmessage> ** requiredPort = 0;
 switch(instances[indexComponent]->subTypeCode){
case 2:{
switch(portCode){
case 3:{
   requiredPort=&(((Timer*)instances[indexComponent])->tick);
break;}
}
break;}
}
if(requiredPort){
     *requiredPort=NULL;
} else {
 switch(instances[indexChannel]->subTypeCode){
case 3:{
((LocalChannel*)instances[indexChannel])->bindings->removeBinding(instances[indexComponent]->instanceName,portCode);
break;}
}
}
}
boolean periodicExecution(int index){
 switch(instances[index]->subTypeCode){
case 2:{
return millis() > (((Timer *) instances[index] )->nextExecution);
}
}
return false;
}
int getPortQueuesSize(int index){
 switch(instances[index]->subTypeCode){
case 1:{
return (((DigitalLight *)instances[index])->on->count())+(((DigitalLight *)instances[index])->off->count())+(((DigitalLight *)instances[index])->toggle->count());
}
case 3:{
return (((LocalChannel *)instances[index])->input->count());
}
}
return 0;
}
  int getIndexFromName(char * id){
 for(int i=0;i<nbInstances;i++){
  if(String(instances[i]->instanceName) == id){ return i; }
 } 
 return -1;
}
//ARDUINO SERIAL INPUT READ 
#define BUFFERSIZE 100      
int serialIndex = 0;        
char inBytes[BUFFERSIZE];   
void checkForAdminMsg(){     
  if(Serial.available()>0 && serialIndex < BUFFERSIZE) { 
    inBytes[serialIndex] = Serial.read();                 
    if (inBytes[serialIndex] == '\n' || inBytes[serialIndex] == ';') {  
              inBytes[serialIndex] = ' ';  
              parseForAdminMsg();                
               for(int i=0;i<serialIndex;i++){   
                    inBytes[serialIndex];       
                }                               
                serialIndex = 0;                
               Serial.println(freeRam());        
    } else {      
      serialIndex++;   
    }       
  }          
  if(serialIndex >= BUFFERSIZE){   
    Serial.println("Buffer overflow");  
      for(int i=0;i<serialIndex;i++){   
          inBytes[serialIndex];         
      }                                
      serialIndex = 0;                
  }                                   
}                                    
char * insID;  
char * typeID; 
char * params;   
char * chID;        
char * portID;            
const char delims[] = ":";    
boolean parseForAdminMsg(){       
  if(serialIndex < 7){return false;}    
  Serial.println(inBytes);          
  if(inBytes[0] == 'b' && inBytes[1] == '{' && inBytes[serialIndex-1] == '}'  ){  
    inBytes[serialIndex-1] = '\0';   
    if( inBytes[2]=='u' && inBytes[3]=='d' && inBytes[4]=='i' && inBytes[5]==':' ){  
      insID = strtok(&inBytes[6], delims);  
      params = strtok(NULL, delims);    
      updateParams(getIndexFromName(insID),params);   
      return true;      
    }   
    if( inBytes[2]=='a' && inBytes[3]=='i' && inBytes[4]=='n' && inBytes[5]==':' ){ 
      insID = strtok(&inBytes[6], delims); 
      typeID = strtok(NULL, delims);
      params = strtok(NULL, delims); 
      Serial.println(insID);
      Serial.println(typeID); 
      Serial.println(params); 
      //createInstance(typeID,insID,params);  
      return true;
    }      
    if( inBytes[2]=='r' && inBytes[3]=='i' && inBytes[4]=='n' && inBytes[5]==':' ){    
      insID = strtok(&inBytes[6], delims);   
      Serial.println(insID);                
      //removeInstance(getIndexFromName(insID));  
      return true;   
    }               
    if( inBytes[2]=='a' && inBytes[3]=='b' && inBytes[4]=='i' && inBytes[5]==':' ){   
      insID = strtok(&inBytes[6], delims);                                             
      chID = strtok(NULL, delims);                                                      
      portID = strtok(NULL, delims);                                                     
      Serial.println(insID);                                                              
      Serial.println(chID);                                                                
      Serial.println(portID);                                                               
      //bind(getIndexFromName(insID),getIndexFromName(chID),portID);                         
      return true;                                                                            
    }                                                                                         
    if( inBytes[2]=='r' && inBytes[3]=='b' && inBytes[4]=='i' && inBytes[5]==':' ){           
      insID = strtok(&inBytes[6], delims);                                                    
      chID = strtok(NULL, delims);                                                            
      portID = strtok(NULL, delims);                                                          
      Serial.println(insID);                                                                  
      Serial.println(chID);                                                                   
      Serial.println(portID);                                                                 
      //unbind(getIndexFromName(insID),getIndexFromName(chID),portID);                        
      return true;                                                                            
    }                                                                                         
  }                                                                                           
  return false;                                                                               
}                                                                                             
void setup(){
Serial.begin(9600);
int indexhub846068439 = createInstance(2,"hub846068439","");
int indexDigitalLight138771701 = createInstance(0,"DigitalLight138771701","pin=9,");
int indext1 = createInstance(1,"t1","period=1000,");
int indexDigitalLight1083761075 = createInstance(0,"DigitalLight1083761075","pin=10,");
bind(indexDigitalLight138771701,indexhub846068439,2);
bind(indext1,indexhub846068439,3);
bind(indexDigitalLight1083761075,indexhub846068439,2);
Serial.println(freeRam());
}
void loop(){
for(int i=0;i<nbInstances;i++){
  if(periodicExecution(i)){
    runInstance(i);
  }
}
for(int i=0;i<nbInstances;i++){
  if(getPortQueuesSize(i)>0){
    runInstance(i);
  }
}
checkForAdminMsg();
}
    int freeRam () {
 extern int __heap_start, *__brkval;
  int v;
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);
}



