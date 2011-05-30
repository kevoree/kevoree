#include <QueueList.h>
#include <LiquidCrystal.h> 


struct kmessage {char* value;char* metric;};
class KevoreeType { public : char* subTypeName; int subTypeCode; };
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
class LCDDisplay : public KevoreeType {
 public : 
LiquidCrystal * lcd;

QueueList<kmessage> * input;
void init(){
input = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(input){
   memset(input, 0, sizeof(QueueList<kmessage>));
}
lcd = (LiquidCrystal*) malloc(sizeof(LiquidCrystal));
if (lcd){memset(lcd, 0, sizeof(LiquidCrystal));}LiquidCrystal lcdObj(10, 11, 12, 13, 14, 15, 16);memcpy (lcd,&lcdObj,sizeof(LiquidCrystal));lcd->begin(16, 2);
}
void runInstance(){
if(!input->isEmpty()){
kmessage * msg = &(input->pop());
LCDDisplay::input_pport(msg);
}
}
void input_pport(kmessage * msg){
lcd->clear();
lcd->print(String(msg->value)+String(":")+String(msg->metric));

}
char * instanceName;
};
class Timer : public KevoreeType {
 public : 
unsigned long nextExecution;
QueueList<kmessage> * tick;
char * period;
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
char * instanceName;
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
char * instanceName;
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
LCDDisplay * instance = (LCDDisplay*) instances[index];
break;}
case 2:{
Timer * instance = (Timer*) instances[index];
if(String(key) == "period"){
instance->period = val;
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
if(typeName == "LCDDisplay"){
  LCDDisplay * newInstance = (LCDDisplay*) malloc(sizeof(LCDDisplay));
  if (newInstance){
    memset(newInstance, 0, sizeof(LCDDisplay));
  } 
  newInstance->instanceName = instanceName;
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeName = typeName; 
  tempInstance->subTypeCode = 1; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
}
if(typeName == "Timer"){
  Timer * newInstance = (Timer*) malloc(sizeof(Timer));
  if (newInstance){
    memset(newInstance, 0, sizeof(Timer));
  } 
  newInstance->instanceName = instanceName;
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeName = typeName; 
  tempInstance->subTypeCode = 2; 
  int newIndex = addInstance();
  updateParams(newIndex,params);
  return newIndex;
}
if(typeName == "LocalChannel"){
  LocalChannel * newInstance = (LocalChannel*) malloc(sizeof(LocalChannel));
  if (newInstance){
    memset(newInstance, 0, sizeof(LocalChannel));
  } 
  newInstance->instanceName = instanceName;
  newInstance->init();
  tempInstance = newInstance;
  tempInstance->subTypeName = typeName; 
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
LCDDisplay * instance = (LCDDisplay*) instances[index];
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
void bind(int indexComponent,int indexChannel,char * portName){
QueueList<kmessage> * providedPort = 0;
QueueList<kmessage> ** requiredPort = 0;
char * componentInstanceName;
int componentTypeCode = instances[indexComponent]->subTypeCode;
int channelTypeCode = instances[indexChannel]->subTypeCode;
 switch(componentTypeCode){
case 1:{
LCDDisplay * instance = (LCDDisplay*) instances[indexComponent];
if(String(portName) == "input"){
   providedPort=instance->input;
componentInstanceName=instance->instanceName;
}
break;}
case 2:{
Timer * instance = (Timer*) instances[indexComponent];
if(String(portName) == "tick"){
   requiredPort=&instance->tick;
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
return (((LCDDisplay *)instances[index])->input->count());
}
case 3:{
return (((LocalChannel *)instances[index])->input->count());
}
}
return 0;
}
void setup(){
Serial.begin(9600);
int indexhub1366727710 = createInstance("LocalChannel","hub1366727710","");
int indexLCDDisplay732527497 = createInstance("LCDDisplay","LCDDisplay732527497","");
int indexTimer400078833 = createInstance("Timer","Timer400078833","period=1000,");
bind(indexLCDDisplay732527497,indexhub1366727710,"input");
bind(indexTimer400078833,indexhub1366727710,"tick");
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
}



