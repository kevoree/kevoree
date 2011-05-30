#include <QueueList.h>

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
class TempSensor : public KevoreeType {
 public : 
float vcc;
float pad;
float thermr;

QueueList<kmessage> * trigger;
QueueList<kmessage> * temp;
char * pin;
void init(){
trigger = (QueueList<kmessage>*) malloc(sizeof(QueueList<kmessage>));
if(trigger){
   memset(trigger, 0, sizeof(QueueList<kmessage>));
}
vcc = 4.91;
pad = 9850;
thermr = 10000;

}
void runInstance(){
if(!trigger->isEmpty()){
kmessage * msg = &(trigger->pop());
TempSensor::trigger_pport(msg);
}
}
void temp_rport(kmessage * msg){
  Serial.println(String(msg->value));
if(temp){
temp->push(*msg);
}
}
void trigger_pport(kmessage * msg){
    long Resistance;  
    float temp;  // Dual-Purpose variable to save space.
    int RawADC = analogRead(atoi(pin));
    Resistance=((1024 * thermr / RawADC) - pad); 
    temp = log(Resistance); // Saving the Log(resistance) so not to calculate  it 4 times later
    temp = 1 / (0.001129148 + (0.000234125 * temp) + (0.0000000876741 * temp * temp * temp));
    temp = temp - 273.15;  // Convert Kelvin to Celsius  
    //send to output port
kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));if (smsg){memset(smsg, 0, sizeof(kmessage));}char buf[255];
sprintf(buf,"%d",int(temp));
smsg->value = buf;
smsg->metric="c";temp_rport(smsg);free(smsg);
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
kmessage * msg = (kmessage*) malloc(sizeof(kmessage));if (msg){memset(msg, 0, sizeof(kmessage));}tick_rport(msg);free(msg);
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
TempSensor * instance = (TempSensor*) instances[index];
if(String(key) == "pin"){
instance->pin = val;
}
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
if(typeName == "TempSensor"){
  TempSensor * newInstance = (TempSensor*) malloc(sizeof(TempSensor));
  if (newInstance){
    memset(newInstance, 0, sizeof(TempSensor));
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
TempSensor * instance = (TempSensor*) instances[index];
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
TempSensor * instance = (TempSensor*) instances[indexComponent];
if(String(portName) == "trigger"){
   providedPort=instance->trigger;
componentInstanceName=instance->instanceName;
}
if(String(portName) == "temp"){
   requiredPort=&instance->temp;
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
return (((TempSensor *)instances[index])->trigger->count());
}
case 3:{
return (((LocalChannel *)instances[index])->input->count());
}
}
return 0;
}
void setup(){
Serial.begin(9600);
int indexhub972603619 = createInstance("LocalChannel","hub972603619","");
int indexTempSensor752027732 = createInstance("TempSensor","TempSensor752027732","pin=0,");
int indexTimer1691007619 = createInstance("Timer","Timer1691007619","period=1000,");
bind(indexTimer1691007619,indexhub972603619,"tick");
bind(indexTempSensor752027732,indexhub972603619,"trigger");
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



