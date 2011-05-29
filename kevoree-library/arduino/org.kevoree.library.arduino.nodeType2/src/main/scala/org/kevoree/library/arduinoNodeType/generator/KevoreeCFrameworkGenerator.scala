/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.Channel
import org.kevoree.ChannelType
import org.kevoree.ComponentInstance
import org.kevoree.ComponentType
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.Instance
import org.kevoree.MBinding
import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._

trait KevoreeCFrameworkGenerator extends KevoreeCAbstractGenerator {

  def generateKcFramework : Unit = {  
    context h "#include <QueueList.h>"
    context b "struct kmessage {char* value;char* metric;};"
    context b "class KevoreeType { public : char* subTypeName; int subTypeCode; };"  
    
    //GENERATE kbinding framework
    context b "struct kbinding { char* instanceName;char * portName; QueueList<kmessage> * port;   };"
    context b "class kbindings {"
    context b " public:"
    context b " kbinding ** bindings;"
    context b " int nbBindings;"
    context b " void init(){ nbBindings = 0; }"
    context b " int addBinding( char * instanceName , char * portName , QueueList<kmessage> * port){"
    context b "   kbinding * newBinding = (kbinding*) malloc(sizeof(kbinding));"
    context b "   if(newBinding){"
    context b "      memset(newBinding, 0, sizeof(kbinding));"
    context b "   }"
    context b "   newBinding->instanceName = instanceName;"
    context b "   newBinding->portName = portName;"
    context b "   newBinding->port = port;"
    context b "   kbinding ** newBindings =  (kbinding**) malloc(  (nbBindings+1) * sizeof(kbinding*) );"
    context b "   if (!newBindings) { return -1;}"
    context b "   for (int idx=0;idx < nbBindings ; idx++ ){ newBindings[idx] = bindings[idx]; }"
    context b "   newBindings[nbBindings] = newBinding;"
    context b "   if(bindings){ free(bindings); }"
    context b "   bindings = newBindings;"
    context b "   nbBindings ++;"
    context b "   return nbBindings-1;"
    context b " }"
    context b " boolean removeBinding( char * instanceName , char * portName ){"
    context b "   //SEARCH INDEX"
    context b "   int indexToRemove = -1;"
    context b "   for(int i=0;i<nbBindings;i++){"
    context b "     kbinding * binding = bindings[i];"
    context b "     if(binding->instanceName == instanceName && binding->portName == portName){ indexToRemove = i; }"
    context b "   }"
    context b "   if(indexToRemove == -1){return -1;}"
    context b "   kbinding** newBindings =  (kbinding**) malloc(  (nbBindings-1) *sizeof(kbinding*) );"
    context b "   if (!newBindings) { return false;}"
    context b "   for (int idx=0;idx < nbBindings ; idx++ ){ "
    context b "    if(idx < indexToRemove){ newBindings[idx] = bindings[idx]; }"
    context b "    if(idx > indexToRemove){ newBindings[idx-1] = bindings[idx]; }"
    context b "   }"
    context b "   if(bindings){ free(bindings); }"
    context b "   bindings = newBindings; "
    context b "  nbBindings--;"
    context b "  return true;  "
    context b " } "
    context b "};"
  }

  def generateRunInstanceMethod(types : List[TypeDefinition]){
    context b "void runInstance(int index){"
    var i = 1; //ASSUME LIST ORDER SIMILAR TO GENERATE FACTORY !!!
    context b "int typeCode = instances[index]->subTypeCode;"
    context b " switch(typeCode){"
    types.foreach{ktype =>
      context b "case "+i+":{"
      context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[index];"
      context b "instance->runInstance();"
      context b "break;}"
      i = i +1
    }
    context b "}"  
    context b "}"
  }
  
  def generateParamsMethod : Unit = {
    //GENERATE GLOBAL UPDATE METHOD
    context b "void updateParams(int index,char* params){"
    context b "  int typeCode = instances[index]->subTypeCode;"
    context b "  char *p = params;"
    context b "  char * str;"
    context b "  while ((str = strtok_r(p, \",\", &p)) != NULL){"
    context b "    char * str2;"
    context b "    char* keyval[2];"
    context b "    int keyvalIndex = 0;"
    context b "    while ((str2 = strtok_r(str, \"=\", &str)) != NULL){"
    context b "      keyval[keyvalIndex] = str2;"
    context b "      keyvalIndex ++;"
    context b "    }"
    context b "    updateParam(index,typeCode,keyval[0],keyval[1]);"
    context b "  }"  
    context b "}"  
  }
  
  def generateParamMethod(types : List[TypeDefinition]) : Unit = {
    context b "void updateParam(int index,int typeCode,char * key,char * val){"
    var i = 1; //ASSUME LIST ORDER SIMILAR TO GENERATE FACTORY !!!
    context b " switch(typeCode){"
    types.foreach{ktype =>
      context b "case "+i+":{"
      context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[index];"
      if(ktype.getDictionaryType != null){
        ktype.getDictionaryType.getAttributes.foreach{ attribute =>
          context b "if(String(key) == \""+attribute.getName+"\"){"
          context b "instance->"+attribute.getName+" = val;"
          context b "}"
        }
      }
      context b "break;}"
      i = i +1
    }
    context b "}"
    context b "}"
  }
  
  
  def generateGlobalInstanceState : Unit = {
    //GENERATE GLOBAL VARIABLE
    context b "KevoreeType ** instances; //GLOBAL INSTANCE DYNAMIC ARRAY"
    context b "int nbInstances = 0; //GLOBAL NB INSTANCE"
    context b "KevoreeType * tempInstance; //TEMP INSTANCE POINTER"
    //GENERATE ADD INSTANCE HELPER
    context b "int addInstance(){ //TECHNICAL HELPER ADD INSTANCE"
    context b "  if(tempInstance){"
    context b "    KevoreeType** newInstances =  (KevoreeType**) malloc(  (nbInstances+1) *sizeof(KevoreeType*) );"
    context b "    if (!newInstances) { return -1;}"
    context b "    for (int idx=0;idx < nbInstances ; idx++ ){ newInstances[idx] = instances[idx]; }"
    context b "    newInstances[nbInstances] = tempInstance;"
    context b "    if(instances){ free(instances); }"
    context b "    instances = newInstances;"
    context b "    tempInstance = NULL;"
    context b "    nbInstances ++;"
    context b "    return nbInstances-1;"
    context b "  }"
    context b "  return -1;"
    context b "}"
    //GENERATE REMOVE INSTANCE HELPER
    context b "boolean removeInstance(int index){"
    context b "KevoreeType** newInstances =  (KevoreeType**) malloc(  (nbInstances-1) *sizeof(KevoreeType*) );"
    context b "if (!newInstances) { return false;}"
    context b "for (int idx=0;idx < nbInstances ; idx++ ){ "
    context b "  if(idx < index){ newInstances[idx] = instances[idx]; }"
    context b "  if(idx > index){ newInstances[idx-1] = instances[idx]; }"
    context b "}"
    context b "if(instances){ free(instances); }"
    context b "instances = newInstances; "
    context b "nbInstances--;return true;"
    context b "}" 
  }


  def generateGlobalInstanceFactory(types : List[TypeDefinition]) : Unit = {
    context b "int createInstance(char* typeName,char* instanceName,char* params){"
    var i = 1;
    types.foreach{ ktype =>
      context b "if(typeName == \""+ktype.getName+"\"){"
      context b "  "+ktype.getName+" * newInstance = ("+ktype.getName+"*) malloc(sizeof("+ktype.getName+"));"
      context b "  if (newInstance){"
      context b "    memset(newInstance, 0, sizeof("+ktype.getName+"));"
      context b "  } "
      context b "  newInstance->instanceName = instanceName;"
      context b "  newInstance->init();"
      context b "  tempInstance = newInstance;"
      context b "  tempInstance->subTypeName = typeName; "
      context b "  tempInstance->subTypeCode = "+i+"; "
      context b "  int newIndex = addInstance();"
      context b "  updateParams(newIndex,params);"
      context b "  return newIndex;"
      context b "}"
      i = i + 1
    }
    context b " return -1;"
    context b "}"
  }
  
  def generateBindMethod(types : List[TypeDefinition]) : Unit = {
    context b "void bind(int indexComponent,int indexChannel,char * portName){"
    context b "QueueList<kmessage> * providedPort = 0;"
    context b "QueueList<kmessage> ** requiredPort = 0;"
    context b "char * componentInstanceName;"
    context b "int componentTypeCode = instances[indexComponent]->subTypeCode;"
    context b "int channelTypeCode = instances[indexChannel]->subTypeCode;"
    //FOR ALL COMPONENT LOOK FOR TYPE
    var i = 1
    context b " switch(componentTypeCode){"
    types.foreach{ktype =>
      if(ktype.isInstanceOf[ComponentType]){
        var kcomponentType = ktype.asInstanceOf[ComponentType]
        context b "case "+i+":{"
        context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[indexComponent];"
        if(kcomponentType.getProvided != null){
          kcomponentType.getProvided.foreach{ provided =>
            context b "if(String(portName) == \""+provided.getName+"\"){"
            context b "   providedPort=instance->"+provided.getName+";";
            context b "componentInstanceName=instance->instanceName;"
            context b "}"
          }
        }
        if(kcomponentType.getRequired != null){
          kcomponentType.getRequired.foreach{ required =>
            context b "if(String(portName) == \""+required.getName+"\"){"
            context b "   requiredPort=&instance->"+required.getName+";";
            context b "}"
          }
        } 
        context b "break;}"  
      }
      i = i +1
    }
    context b "}" //END SWITCH COMPONENT TYPE
    
    
    //FOR ALL Channel LOOK FOR TYPE
    i = 1
    context b " switch(channelTypeCode){"
    types.foreach{ktype =>
      if(ktype.isInstanceOf[ChannelType]){
        var kcomponentType = ktype.asInstanceOf[ChannelType]
        context b "case "+i+":{"
        context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[indexChannel];"       
        context b "if(providedPort){"
        context b "instance->bindings->addBinding(componentInstanceName,portName,providedPort);"
        context b "}"
        context b "if(requiredPort){"
        context b "*requiredPort = instance->input;"
        context b "}" 
        context b "break;}"
      }
      i = i +1
    }
    context b "}" //END SWITCH Channel TYPE

    context b "}"
  }
 
  def generateLoop : Unit = {
    context b "void loop(){"
    context b "delay(1000);"//TO REMOVE DEBUG ONLY
    context b "for(int i=0;i<nbInstances;i++){"
    context b "runInstance(i);"
    context b "}"
    context b "}"  
  }
  
  def generateSetup(initInstances : List[Instance],nodeName:String) : Unit = { //DUMMY SCHEDULER FOR TEST
    context b "void setup(){"
    context b "Serial.begin(9600);"//TO REMOVE DEBUG ONLY
    initInstances.foreach{ instance =>
      //GENERATE INIT DICTIONARY  
      val dictionary: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
      if (instance.getTypeDefinition.getDictionaryType != null) {
        if (instance.getTypeDefinition.getDictionaryType.getDefaultValues != null) {
          instance.getTypeDefinition.getDictionaryType.getDefaultValues.foreach {
            dv =>
            dictionary.put(dv.getAttribute.getName, dv.getValue)
          }
        }
      }
      val dictionaryResult = new StringBuffer
      dictionary.foreach{dic=> dictionaryResult.append(dic._1+"="+dic._2+",");}
      context b "int index"+instance.getName+" = createInstance(\""+instance.getTypeDefinition.getName+"\",\""+instance.getName+"\",\""+dictionaryResult.toString+"\");"
    }
    if(initInstances.filter(i=>i.isInstanceOf[Channel]).size > 0){
      var i0 = initInstances.filter(i=>i.isInstanceOf[Channel]).get(0)
      i0.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach{binding=>
        if(binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == nodeName){
          context b "bind(index"+binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName+",index"+binding.getHub.getName+",\""+binding.getPort.getPortTypeRef.getName+"\");"
        }
      }
    }
    context b "}"  
  }
  
  
  
  
  
  
}
