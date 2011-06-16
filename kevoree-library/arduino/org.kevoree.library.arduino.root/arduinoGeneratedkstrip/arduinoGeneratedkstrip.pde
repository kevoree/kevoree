#include <QueueList.h>
#include <avr/pgmspace.h>
#include <avr/wdt.h>
#include <EEPROM.h>
#define kevoreeID1 0
#define kevoreeID2 1
//Global Kevoree Type Defintion declaration
const prog_char timer[] PROGMEM = "Timer";
const prog_char lightsensor[] PROGMEM = "LightSensor";
const prog_char localchannel[] PROGMEM = "LocalChannel";
const prog_char serialct[] PROGMEM = "SerialCT";
PROGMEM const char * typedefinition[] = { 
timer
,lightsensor
,localchannel
,serialct
};
//Global Kevoree Port Type Defintion declaration
const prog_char port_tick[] PROGMEM = "tick";
const prog_char port_trigger[] PROGMEM = "trigger";
const prog_char port_light[] PROGMEM = "light";
PROGMEM const char * portdefinition[] = { port_tick,port_trigger,port_light};
const prog_char prop_period[] PROGMEM = "period";
const prog_char prop_pin[] PROGMEM = "pin";
const prog_char prop_port[] PROGMEM = "PORT";
PROGMEM const char * properties[] = { prop_period,prop_pin,prop_port};

const int nbPortType = 3;
int getIDFromPortName(char * portName){
  for(int i=0;i<nbPortType;i++){
   if(strcmp_P(portName, (char*)pgm_read_word(&(portdefinition[i]))  )==0) { return i; }
  }
  return -1;
}
const int nbTypeDef = 4;
int getIDFromType(char * typeName){
  for(int i=0;i<nbTypeDef;i++){
   if(strcmp_P(typeName, (char*)pgm_read_word(&(typedefinition[i]))  )==0) { return i; }
  }
  return -1;
}
const int nbProps = 3;
int getIDFromProps(char * propName){
  for(int i=0;i<nbProps;i++){
   if(strcmp_P(propName, (char*)pgm_read_word(&(properties[i]))  )==0) { return i; }
  }
  return -1;
}
struct kmessage {char* value;char* metric;};
class KevoreeType { public : int subTypeCode; char instanceName[15]; };
struct kbinding { KevoreeType * instance;int portCode; QueueList<kmessage> * port;   };
#define BDYNSTEP 3
class kbindings {
 public:
 kbinding ** bindings;
 int nbBindings;
 void init(){ nbBindings = 0; }
 int addBinding( KevoreeType * instance , int portCode , QueueList<kmessage> * port){
   kbinding * newBinding = (kbinding*) malloc(sizeof(kbinding));
   if(newBinding){
      memset(newBinding, 0, sizeof(kbinding));
   }
   newBinding->instance=instance;
   newBinding->portCode=portCode;
   newBinding->port = port;
if(nbBindings % BDYNSTEP == 0){
  bindings = (kbinding**) realloc(bindings, (nbBindings+BDYNSTEP) * sizeof(kbinding*) );
}
bindings[nbBindings] = newBinding;
   nbBindings ++;
   return nbBindings-1;
 }
 boolean removeBinding( KevoreeType * instance , int portCode ){
   //SEARCH INDEX
   int indexToRemove = -1;
   for(int i=0;i<nbBindings;i++){
     kbinding * binding = bindings[i];
     if( (strcmp(binding->instance->instanceName,instance->instanceName) == 0 ) && binding->portCode == portCode){ indexToRemove = i; }
   }
   if(indexToRemove == -1){return -1;} else { free(bindings[indexToRemove]); }
if(indexToRemove != nbBindings-1){
  bindings[indexToRemove] = bindings[nbBindings-1];
}
if(nbBindings % BDYNSTEP == 0){
  bindings = (kbinding**) realloc(bindings, (nbBindings) * sizeof(kbinding*) );
}
  nbBindings--;
  return true;  
 } 
void destroy(){
 for(int i=0;i<nbBindings;i++){ free(bindings[i]); } 
 free(bindings);
}
};
class Timer : public KevoreeType {
 public : 
unsigned long nextExecution;
QueueList<kmessage> * tick;
char period[20];
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
class LightSensor : public KevoreeType {
 public : 
QueueList<kmessage> * trigger;
QueueList<kmessage> * light;
char pin[20];
void init(){
trigger = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(trigger){
   memset(trigger, 0, sizeof(QueueList<kmessage>));
}
}
void destroy(){
free(trigger);
}
void runInstance(){
if(!trigger->isEmpty()){
kmessage * msg = &(trigger->pop());
LightSensor::trigger_pport(msg);
}
}
void light_rport(kmessage * msg){
if(light){
light->push(*msg);
}
}
void trigger_pport(kmessage * msg){
int photocellReading = analogRead(atoi(pin));
kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));if (smsg){memset(smsg, 0, sizeof(kmessage));}char buf[255];
sprintf(buf,"%d",photocellReading);
smsg->value = buf;
smsg->metric="alux";light_rport(smsg);free(smsg);
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
class SerialCT : public KevoreeType {
 public : 
QueueList<kmessage> * input;
kbindings * bindings;
char PORT[20];
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
SerialCT::dispatch(msg);
}
}
void dispatch(kmessage * msg){
for(int i=0;i<bindings->nbBindings;i++){    bindings->bindings[i]->port->push(*msg);}Serial.print(instanceName);Serial.print(":");Serial.print("kstrip");Serial.print("[");Serial.print(msg->metric);Serial.print("/");Serial.print(msg->value);Serial.println("]");
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
case 0:{
((Timer*) instances[index])->destroy();
break;}
case 1:{
((LightSensor*) instances[index])->destroy();
break;}
case 2:{
((LocalChannel*) instances[index])->destroy();
break;}
case 3:{
((SerialCT*) instances[index])->destroy();
break;}
}
free(instance);
}
void updateParam(int index,int typeCode,int keyCode,char * val){
 switch(typeCode){
case 0:{
Timer * instance = (Timer*) instances[index];
 switch(keyCode){
case 0:{
strcpy (instance->period,val);
break;}
}
break;}
case 1:{
LightSensor * instance = (LightSensor*) instances[index];
 switch(keyCode){
case 1:{
strcpy (instance->pin,val);
break;}
}
break;}
case 2:{
LocalChannel * instance = (LocalChannel*) instances[index];
break;}
case 3:{
SerialCT * instance = (SerialCT*) instances[index];
 switch(keyCode){
case 2:{
strcpy (instance->PORT,val);
break;}
}
break;}
}
}
const char delimsEQ[] = "=";
char * key;
char * val;
char * str;
void updateParams(int index,char* params){
  while ((str = strtok_r(params, ",", &params)) != NULL){
    key = strtok(str, delimsEQ);
    val = strtok(NULL, delimsEQ);
    updateParam(index,instances[index]->subTypeCode,getIDFromProps(key),val);
  }
}
int createInstance(int typeCode,char* instanceName,char* params){
switch(typeCode){
case 0:{
  Timer * newInstance = (Timer*) malloc(sizeof(Timer));
  if (newInstance){
    memset(newInstance, 0, sizeof(Timer));
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
  LightSensor * newInstance = (LightSensor*) malloc(sizeof(LightSensor));
  if (newInstance){
    memset(newInstance, 0, sizeof(LightSensor));
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
case 3:{
  SerialCT * newInstance = (SerialCT*) malloc(sizeof(SerialCT));
  if (newInstance){
    memset(newInstance, 0, sizeof(SerialCT));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeCode = 3; 
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
case 0:{
Timer * instance = (Timer*) instances[index];
instance->runInstance();
break;}
case 1:{
LightSensor * instance = (LightSensor*) instances[index];
instance->runInstance();
break;}
case 2:{
LocalChannel * instance = (LocalChannel*) instances[index];
instance->runInstance();
break;}
case 3:{
SerialCT * instance = (SerialCT*) instances[index];
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
case 0:{
Timer * instance = (Timer*) instances[indexComponent];
switch(portCode){
case 0:{
   requiredPort=&instance->tick;
break;}
}
break;}
case 1:{
LightSensor * instance = (LightSensor*) instances[indexComponent];
switch(portCode){
case 1:{
   providedPort=instance->trigger;
componentInstanceName=instance->instanceName;
break;}
case 2:{
   requiredPort=&instance->light;
break;}
}
break;}
}
 switch(channelTypeCode){
case 2:{
LocalChannel * instance = (LocalChannel*) instances[indexChannel];
if(providedPort){
instance->bindings->addBinding(instances[indexComponent],portCode,providedPort);
}
if(requiredPort){
*requiredPort = instance->input;
}
break;}
case 3:{
SerialCT * instance = (SerialCT*) instances[indexChannel];
if(providedPort){
instance->bindings->addBinding(instances[indexComponent],portCode,providedPort);
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
case 0:{
switch(portCode){
case 0:{
   requiredPort=&(((Timer*)instances[indexComponent])->tick);
break;}
}
break;}
case 1:{
switch(portCode){
case 2:{
   requiredPort=&(((LightSensor*)instances[indexComponent])->light);
break;}
}
break;}
}
if(requiredPort){
     *requiredPort=NULL;
} else {
 switch(instances[indexChannel]->subTypeCode){
case 2:{
((LocalChannel*)instances[indexChannel])->bindings->removeBinding(instances[indexComponent],portCode);
break;}
case 3:{
((SerialCT*)instances[indexChannel])->bindings->removeBinding(instances[indexComponent],portCode);
break;}
}
}
}
boolean periodicExecution(int index){
 switch(instances[index]->subTypeCode){
case 0:{
return millis() > (((Timer *) instances[index] )->nextExecution);
}
}
return false;
}
int getPortQueuesSize(int index){
 switch(instances[index]->subTypeCode){
case 1:{
return (((LightSensor *)instances[index])->trigger->count());
}
case 2:{
return (((LocalChannel *)instances[index])->input->count());
}
case 3:{
return (((SerialCT *)instances[index])->input->count());
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
const char startBAdminChar = '{';                                 
const char endAdminChar = '}';                                    
const char startAdminChar = '$';                                  
const char sepAdminChar = '/';                                    
char ackToken;                                                    
boolean parsingAdmin = false;                                     
void checkForAdminMsg(){                                          
  if(Serial.peek() == startAdminChar){                            
    Serial.read();//DROP ADMIN START CHAR                         
    while(!Serial.available()>0){delay(10);}                      
    ackToken = Serial.read();                                     
    while(!Serial.available()>0){delay(10);}                      
    if(Serial.read() == startBAdminChar){                         
      parsingAdmin = true;                                        
      while(parsingAdmin){                                        
        if(Serial.available()>0 && serialIndex < BUFFERSIZE) {    
            inBytes[serialIndex] = Serial.read();                 
            if(inBytes[serialIndex] == sepAdminChar){             
                inBytes[serialIndex] = '\0';                      
                saveScriptCommand();parseForAdminMsg();                               
                flushAdminBuffer();                               
            } else {                                              
              if(inBytes[serialIndex] == endAdminChar){           
                 parsingAdmin = false;                            
                 inBytes[serialIndex] = '\0';                     
                 saveScriptCommand();parseForAdminMsg();                              
                 flushAdminBuffer();                              
                 Serial.print("ack");                             
                 Serial.println(ackToken);                        
                 Serial.println(freeRam());
              } else {                                            
                serialIndex++;                                    
              }                                                   
            }                                                     
        }                                                         
        if(serialIndex >= BUFFERSIZE){                            
          Serial.println("BFO");  
          flushAdminBuffer();                                     
          Serial.flush();                                         
          parsingAdmin=false;//KILL PARSING ADMIN                 
        }                                                         
      }                                                           
  } else {                                                        
          Serial.println("BAM");                  
          flushAdminBuffer();                                     
          Serial.flush();                                         
  }                                                               
}                                                                 
}                                                                 
void flushAdminBuffer(){                                          
  for(int i=0;i<serialIndex;i++){                                 
     inBytes[serialIndex];                                        
  }                                                               
  serialIndex = 0;                                                
}                                                                 
//END SECTION ADMIN DETECTION 1 SPLIT                             
int eepromIndex;                                                                
void saveScriptCommand(){                                                       
  if(eepromIndex == 2){ EEPROM.write(eepromIndex,startBAdminChar);eepromIndex++;}
  if(eepromIndex+serialIndex+1 < 512){                                          
    EEPROM.write(eepromIndex,sepAdminChar);eepromIndex++;                       
    for(int i=0;i<serialIndex;i++){                                             
      EEPROM.write(eepromIndex,inBytes[i]);                                     
      eepromIndex++;                                                            
    }                                                                           
    EEPROM.write(eepromIndex,endAdminChar);                                     
  } else {                                                                      
    Serial.println("OME");                                                      
  }                                                                             
}                                                                               
char * insID;  
char * typeID; 
char * params;   
char * chID;        
char * portID;            
const char delims[] = ":";    
boolean parseForAdminMsg(){       
  if(serialIndex < 6){return false;}    
    if( inBytes[0]=='p' && inBytes[1]=='i' && inBytes[2]=='n' && inBytes[3]=='g' ){  
      return true;      
    }   
    if( inBytes[0]=='u' && inBytes[1]=='d' && inBytes[2]=='i' && inBytes[3]==':' ){  
      insID = strtok(&inBytes[4], delims);  
      params = strtok(NULL, delims);    
      updateParams(getIndexFromName(insID),params);   
      return true;      
    }   
    if( inBytes[0]=='a' && inBytes[1]=='i' && inBytes[2]=='n' && inBytes[3]==':' ){ 
      insID = strtok(&inBytes[4], delims); 
      typeID = strtok(NULL, delims);
      params = strtok(NULL, delims); 
      createInstance(getIDFromType(typeID),insID,params);  
      return true;
    }      
    if( inBytes[0]=='r' && inBytes[1]=='i' && inBytes[2]=='n' && inBytes[3]==':' ){    
      insID = strtok(&inBytes[4], delims);   
      removeInstance(getIndexFromName(insID));  
      return true;   
    }               
    if( inBytes[0]=='a' && inBytes[1]=='b' && inBytes[2]=='i' && inBytes[3]==':' ){   
      insID = strtok(&inBytes[4], delims);                                             
      chID = strtok(NULL, delims);                                                      
      portID = strtok(NULL, delims);                                                     
      bind(getIndexFromName(insID),getIndexFromName(chID),getIDFromPortName(portID));                        
      return true;                                                                            
    }                                                                                         
    if( inBytes[0]=='r' && inBytes[1]=='b' && inBytes[2]=='i' && inBytes[3]==':' ){           
      insID = strtok(&inBytes[4], delims);                                                    
      chID = strtok(NULL, delims);                                                            
      portID = strtok(NULL, delims);                                                          
      unbind(getIndexFromName(insID),getIndexFromName(chID),getIDFromPortName(portID));                        
      return true;                                                                            
    }                                                                                         
  return false;                                                                               
}                                                                                             
void setup(){
Serial.begin(9600);
int indexlhub1 = createInstance(2,"lhub1","");
int indexhubSerial = createInstance(3,"hubSerial","PORT=/dev/tty.usbserial-A400g2AP,");
int indext1 = createInstance(0,"t1","period=1000,");
int indexlux1 = createInstance(1,"lux1","pin=2,");
bind(indext1,indexlhub1,0);
bind(indexlux1,indexlhub1,1);
bind(indexlux1,indexhubSerial,2);
//STATE RECOVERY                                                         
if(EEPROM.read(0) != kevoreeID1 || EEPROM.read(1) != kevoreeID2){            
  for (int i = 0; i < 512; i++){EEPROM.write(i, 0);}                         
  EEPROM.write(0,kevoreeID1);                                                
  EEPROM.write(1,kevoreeID2);                                                
}                                                                            
eepromIndex = 2;                                                             
inBytes[serialIndex] = EEPROM.read(eepromIndex);                         
if (inBytes[serialIndex] == startBAdminChar) {                           
  eepromIndex ++;                                                        
  Serial.println("PSTATE");                         
  inBytes[serialIndex] = EEPROM.read(eepromIndex);                       
  while (inBytes[serialIndex] != endAdminChar && eepromIndex < 512) {    
    if (inBytes[serialIndex] == sepAdminChar) {                          
      inBytes[serialIndex] = '\0';                                       
      parseForAdminMsg();                                                
     flushAdminBuffer();                                                 
    } else {                                                             
      serialIndex ++;                                                    
    }                                                                    
    eepromIndex ++;                                                      
    inBytes[serialIndex] = EEPROM.read(eepromIndex);                     
  }                                                                      
  //PROCESS LAST CMD                                                     
  if (inBytes[serialIndex] == endAdminChar) {                            
    inBytes[serialIndex] = '\0';                                         
    parseForAdminMsg();                                                  
    flushAdminBuffer();                                                  
  }                                                                      
}                                                                        
Serial.println(freeRam());
}
long nextExecutionGap(int index){
 switch(instances[index]->subTypeCode){
case 0:{
return ((((Timer *) instances[index] )->nextExecution)- currentMillis());
}
}
return -1;
}
unsigned long currentMillis(){                          
return millis();
}
void sleepNowFor(long duration){ 
}                                
#define minSleepDuration 80                        
long minNextOp;                                    
boolean allEmptyQueue;                             
long duration;                                     
void loop(){                                       
  checkForAdminMsg();                              
  for(int i=0;i<nbInstances;i++){                  
    if(periodicExecution(i)){                      
      runInstance(i);                              
    }                                              
  }                                                
  allEmptyQueue = true;                            
  for(int i=0;i<nbInstances;i++){                  
    int queueSize = getPortQueuesSize(i);          
    if(getPortQueuesSize(i)>0){                    
      allEmptyQueue = false;                       
      runInstance(i);                              
    }                                              
  }                                                
//POWER OPTIMISATION                               
  if(allEmptyQueue){                               
    //COMPUTE NEXT DELAY                           
    minNextOp = -1;                                
    for(int i=0;i<nbInstances;i++){                
      long nextOp = nextExecutionGap(i);           
      if(nextOp != -1){                            
        if(minNextOp == -1){                       
          minNextOp = nextOp;                      
        } else {                                   
          if(nextOp < minNextOp){                  
            minNextOp = nextOp;                    
          }                                        
        }                                          
      }                                            
    }//END FOR ALL INSTANCE                        
    duration = minNextOp-minSleepDuration;         
    if( duration > 0){                             
        sleepNowFor(duration);                     
    }                                              
  }//END IF Qeue size null                         
}                                                  
    int freeRam () {
 extern int __heap_start, *__brkval;
  int v;
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);
}



