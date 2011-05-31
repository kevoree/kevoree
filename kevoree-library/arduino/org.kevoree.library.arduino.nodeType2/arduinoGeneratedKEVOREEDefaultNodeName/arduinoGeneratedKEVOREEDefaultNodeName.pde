#include <QueueList.h>

struct kmessage {char* value;char* metric;};
class KevoreeType { public : char subTypeName[50]; int subTypeCode; char instanceName[50]; };
struct kbinding { char* instanceName;char * portName; QueueList<kmessage> * port;   };
class kbindings {
 public:
 kbinding ** bindings;
 int nbBindings;
 void init(){ nbBindings = 0; }
 int addBinding( char * instanceName , char * portName , QueueList<kmessage> * port){
   kbinding * newBinding = (kbinding*) malloc(sizeof(kbinding));
   if(newBinding){
      memset(newBinding, 0, sizeof(kbinding));
   }
   newBinding->instanceName = instanceName;
   newBinding->portName = portName;
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
 boolean removeBinding( char * instanceName , char * portName ){
   //SEARCH INDEX
   int indexToRemove = -1;
   for(int i=0;i<nbBindings;i++){
     kbinding * binding = bindings[i];
     if(binding->instanceName == instanceName && binding->portName == portName){ indexToRemove = i; }
   }
   if(indexToRemove == -1){return -1;}
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
};
class Timer : public KevoreeType {
 public : 
unsigned long nextExecution;
QueueList<kmessage> * tick;
char period[50];
void init(){
nextExecution = millis();
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
void updateParam(int index,int typeCode,char * key,char * val){
 switch(typeCode){
case 1:{
Timer * instance = (Timer*) instances[index];
if(String(key) == "period"){
strcpy (instance->period,val);
}
break;}
case 2:{
DigitalLight * instance = (DigitalLight*) instances[index];
if(String(key) == "pin"){
strcpy (instance->pin,val);
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
int createInstance(char* typeName,char* instanceName,char* params){
if(String(typeName) == "Timer"){
  Timer * newInstance = (Timer*) malloc(sizeof(Timer));
  if (newInstance){
    memset(newInstance, 0, sizeof(Timer));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  strcpy(tempInstance->subTypeName,typeName); 
  tempInstance->subTypeCode = 1; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
}
if(String(typeName) == "DigitalLight"){
  DigitalLight * newInstance = (DigitalLight*) malloc(sizeof(DigitalLight));
  if (newInstance){
    memset(newInstance, 0, sizeof(DigitalLight));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  strcpy(tempInstance->subTypeName,typeName); 
  tempInstance->subTypeCode = 2; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
}
if(String(typeName) == "LocalChannel"){
  LocalChannel * newInstance = (LocalChannel*) malloc(sizeof(LocalChannel));
  if (newInstance){
    memset(newInstance, 0, sizeof(LocalChannel));
  } 
  strcpy(newInstance->instanceName,instanceName);
  newInstance->init();
  tempInstance = newInstance;
  strcpy(tempInstance->subTypeName,typeName); 
  tempInstance->subTypeCode = 3; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
}
 return -1;
}
void runInstance(int index){
int typeCode = instances[index]->subTypeCode;
 switch(typeCode){
case 1:{
Timer * instance = (Timer*) instances[index];
instance->runInstance();
break;}
case 2:{
DigitalLight * instance = (DigitalLight*) instances[index];
instance->runInstance();
break;}
case 3:{
LocalChannel * instance = (LocalChannel*) instances[index];
instance->runInstance();
break;}
}
}
void bind(int indexComponent,int indexChannel,char * portName){
QueueList<kmessage> * providedPort = 0;
QueueList<kmessage> ** requiredPort = 0;
char * componentInstanceName;
int componentTypeCode = instances[indexComponent]->subTypeCode;
int channelTypeCode = instances[indexChannel]->subTypeCode;
 switch(componentTypeCode){
case 1:{
Timer * instance = (Timer*) instances[indexComponent];
if(String(portName) == "tick"){
   requiredPort=&instance->tick;
}
break;}
case 2:{
DigitalLight * instance = (DigitalLight*) instances[indexComponent];
if(String(portName) == "on"){
   providedPort=instance->on;
componentInstanceName=instance->instanceName;
}
if(String(portName) == "off"){
   providedPort=instance->off;
componentInstanceName=instance->instanceName;
}
if(String(portName) == "toggle"){
   providedPort=instance->toggle;
componentInstanceName=instance->instanceName;
}
break;}
}
 switch(channelTypeCode){
case 3:{
LocalChannel * instance = (LocalChannel*) instances[indexChannel];
if(providedPort){
instance->bindings->addBinding(componentInstanceName,portName,providedPort);
}
if(requiredPort){
*requiredPort = instance->input;
}
break;}
}
}
void unbind(int indexComponent,int indexChannel,char * portName){
Serial.println("Not supported yet !");
}
boolean periodicExecution(int index){
 switch(instances[index]->subTypeCode){
case 1:{
return millis() > (((Timer *) instances[index] )->nextExecution);
}
}
return false;
}
int getPortQueuesSize(int index){
 switch(instances[index]->subTypeCode){
case 2:{
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
    boolean parseForAdminMsg(char * msgToTest){
  String adminMsg = String(msgToTest);
  if(adminMsg.length() < 3){return false;}
  if(adminMsg.charAt(0) == 'b' && adminMsg.charAt(1) == '{' ){
     int i = 2;
     char currentChar = adminMsg.charAt(i);
     char * block = (char *)calloc(200,sizeof (char));
     while(currentChar != '}' && currentChar != ' '){
      block[i-2] = currentChar;
      i++;currentChar = adminMsg.charAt(i);
     }
     block[i-2] = ' ';   
     char * str;
     char *p = block;
     while ((str = strtok_r(p, "/", &p)) != NULL){
      char * str2;
      char* values[5];
      int valueIndex = 0;
      while ((str2 = strtok_r(str, ":", &str)) != NULL){
        values[valueIndex] = str2;
        valueIndex ++;
      }  
      if(String(values[0]) == "aco" && valueIndex  >= 3){//ACO CHECK
        if(valueIndex == 4){
           createInstance(values[2],values[1],values[3]);
        } else {
           createInstance(values[2],values[1],""); 
        }
      } //END ACO CHECK      
      if(String(values[0]) == "abi" && valueIndex  == 4){//ACO CHECK
           bind(getIndexFromName(values[1]),getIndexFromName(values[2]),values[3]);
      } //END ACO CHECK     
      if(String(values[0]) == "rbi" && valueIndex  == 4){//ACO CHECK
           unbind(getIndexFromName(values[1]),getIndexFromName(values[2]),values[3]);
      } //END ACO CHECK   
      if(String(values[0]) == "udi" && valueIndex  == 3){//ACO CHECK
           updateParams(getIndexFromName(values[1]),values[2]);
      } //END ACO CHECK   
     }
     free(block);return true;
  }
  return false;
}
  int serialIndex = 0;
  char inBytes[200];
void checkForAdminMsg(){
  while(Serial.available()>0 && serialIndex < 200) {
      inBytes[serialIndex] = Serial.read();
      if (inBytes[serialIndex] == '\n' || inBytes[serialIndex] == ';') {
        inBytes[serialIndex] = '\0';
Serial.println(inBytes);
        parseForAdminMsg(inBytes);
        serialIndex = 0;
      } else {
        serialIndex++;
      }
  }
  if(serialIndex >= 200){
      serialIndex = 0;
  }
}
void setup(){
Serial.begin(9600);
int indexhub92542879 = createInstance("LocalChannel","hub92542879","");
int indext1 = createInstance("Timer","t1","period=1000,");
int indexDigitalLight1823264205 = createInstance("DigitalLight","DigitalLight1823264205","pin=9,");
bind(indext1,indexhub92542879,"tick");
bind(indexDigitalLight1823264205,indexhub92542879,"toggle");
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



