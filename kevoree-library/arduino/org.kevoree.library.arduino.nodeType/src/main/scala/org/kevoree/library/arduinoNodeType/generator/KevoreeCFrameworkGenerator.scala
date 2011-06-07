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
import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._

trait KevoreeCFrameworkGenerator extends KevoreeCAbstractGenerator {

  def generateKcFrameworkHeaders(types : List[TypeDefinition]) : Unit = {
    context h "#include <QueueList.h>"
    context h "#include <avr/pgmspace.h>"
    context h "#include <avr/wdt.h>"
    context h "//Global Kevoree Type Defintion declaration" 
    //GENERATE ALL TypeDefinition
    var index = 0
    types.foreach{ktype=>
      typeCodeMap.put(ktype.getName, index)
      context h "const prog_char "+ktype.getName.toLowerCase+"[] PROGMEM = \""+ktype.getName+"\";"
      index = index + 1
    }
    //GENERATE Global TD Tab
    context h "PROGMEM const char * typedefinition[] = { "
    var first = true
    types.foreach{ktype=>
      if(first){ context h ktype.getName.toLowerCase } else { context h ","+ktype.getName.toLowerCase } 
      first = false
    }   
    context h "};"
    context h "//Global Kevoree Port Type Defintion declaration"
    //GENERATE ALL Port
    index = 0
    types.filter(p=> p.isInstanceOf[ComponentType]).foreach{ktype=>
      val ct = ktype.asInstanceOf[ComponentType]
      (ct.getProvided.toList ++ ct.getRequired.toList).toList.foreach{ptRef =>
        portCodeMap.put(ptRef.getName, index)
        context h "const prog_char "+ptRef.getName.toLowerCase+"[] PROGMEM = \""+ptRef.getName+"\";"
        index = index + 1
      }
    }
    //GENERATE Global PORT Tab
    context h "PROGMEM const char * portdefinition[] = { "
    first = true
    types.filter(p=> p.isInstanceOf[ComponentType]).foreach{ktype=>
      val ct = ktype.asInstanceOf[ComponentType]
      (ct.getProvided.toList ++ ct.getRequired.toList).toList.foreach{ptRef =>
        if(first){ context h ptRef.getName.toLowerCase } else { context h ","+ptRef.getName.toLowerCase } 
        first = false
      }
    }
    context h "};"
    //GENERATE ALL PROPERTIES NAME
    index = 0
    var propertiesGenerated : List[String] = List()
    types.foreach{ktype=>
      if(ktype.getDictionaryType != null){
        ktype.getDictionaryType.getAttributes.foreach{att =>
          var propName = att.getName
          propsCodeMap.put(propName, index)
          propertiesGenerated = propertiesGenerated ++ List(propName)
          context h "const prog_char "+propName.toLowerCase+"[] PROGMEM = \""+propName+"\";"
          index = index + 1
        }   
      }
    }
    //GENERATE Global PROPERTIES Tab
    context h "PROGMEM const char * properties[] = { "+propertiesGenerated.mkString(",")+"};"

  }
  
  
  def generateKcConstMethods(types : List[TypeDefinition]) : Unit = { 
    
    var nbPorts = 0
    types.filter(p=> p.isInstanceOf[ComponentType]).foreach{ktype=>
      val ct = ktype.asInstanceOf[ComponentType]
      nbPorts = nbPorts + (ct.getProvided.toList ++ ct.getRequired.toList).size
    }
    var propertiesGenerated : List[String] = List()
    types.foreach{ktype=>
      if(ktype.getDictionaryType != null){
        ktype.getDictionaryType.getAttributes.foreach{att =>
          propertiesGenerated = propertiesGenerated ++ List(att.getName)
        }   
      }
    }

    context b "const int nbPortType = "+nbPorts+";"
    context b "int getIDFromPortName(char * portName){"
    context b "  for(int i=0;i<nbPortType;i++){"
    context b "   if(strcmp_P(portName, (char*)pgm_read_word(&(portdefinition[i]))  )==0) { return i; }"
    context b "  }"
    context b "  return -1;"
    context b "}"
    context b "const int nbTypeDef = "+types.size+";"
    context b "int getIDFromType(char * typeName){"
    context b "  for(int i=0;i<nbTypeDef;i++){"
    context b "   if(strcmp_P(typeName, (char*)pgm_read_word(&(typedefinition[i]))  )==0) { return i; }"
    context b "  }"
    context b "  return -1;"
    context b "}"
    context b "const int nbProps = "+propertiesGenerated.size+";"
    context b "int getIDFromProps(char * propName){"
    context b "  for(int i=0;i<nbProps;i++){"
    context b "   if(strcmp_P(propName, (char*)pgm_read_word(&(properties[i]))  )==0) { return i; }"
    context b "  }"
    context b "  return -1;"
    context b "}"
    
  }
  
  
  def generateKcFramework : Unit = {  
    
    context b "struct kmessage {char* value;char* metric;};"
    context b "class KevoreeType { public : int subTypeCode; char instanceName[15]; };"  
    //GENERATE kbinding framework
    context b "struct kbinding { KevoreeType * instance;int portCode; QueueList<kmessage> * port;   };"
    context b "#define BDYNSTEP 3"
    context b "class kbindings {"
    context b " public:"
    context b " kbinding ** bindings;"
    context b " int nbBindings;"
    context b " void init(){ nbBindings = 0; }"
    context b " int addBinding( KevoreeType * instance , int portCode , QueueList<kmessage> * port){"
    context b "   kbinding * newBinding = (kbinding*) malloc(sizeof(kbinding));"
    context b "   if(newBinding){"
    context b "      memset(newBinding, 0, sizeof(kbinding));"
    context b "   }"
    context b "   newBinding->instance=instance;"
    context b "   newBinding->portCode=portCode;"
    context b "   newBinding->port = port;"
   
    /*
     context b "   kbinding ** newBindings =  (kbinding**) malloc(  (nbBindings+1) * sizeof(kbinding*) );"
     context b "   if (!newBindings) { return -1;}"
     context b "   for (int idx=0;idx < nbBindings ; idx++ ){ newBindings[idx] = bindings[idx]; }"
     context b "   newBindings[nbBindings] = newBinding;"
     context b "   if(bindings){ free(bindings); }"
     context b "   bindings = newBindings;"
     */
   
    context b "if(nbBindings % BDYNSTEP == 0){"
    context b "  bindings = (kbinding**) realloc(bindings, (nbBindings+BDYNSTEP) * sizeof(kbinding*) );"
    context b "}"
    context b "bindings[nbBindings] = newBinding;"
    
    
    context b "   nbBindings ++;"
    context b "   return nbBindings-1;"
    context b " }"
    context b " boolean removeBinding( KevoreeType * instance , int portCode ){"
    context b "   //SEARCH INDEX"
    context b "   int indexToRemove = -1;"
    context b "   for(int i=0;i<nbBindings;i++){"
    context b "     kbinding * binding = bindings[i];"
    context b "     if( (strcmp(binding->instance->instanceName,instance->instanceName) == 0 ) && binding->portCode == portCode){ indexToRemove = i; }"
    context b "   }"
    context b "   if(indexToRemove == -1){return -1;} else { free(bindings[indexToRemove]); }"
    
    context b "if(indexToRemove != nbBindings-1){"
    context b "  bindings[indexToRemove] = bindings[nbBindings-1];"
    context b "}"
    context b "if(nbBindings % BDYNSTEP == 0){"
    context b "  bindings = (kbinding**) realloc(bindings, (nbBindings) * sizeof(kbinding*) );"
    context b "}"
    
    
    /*
     context b "   kbinding** newBindings =  (kbinding**) malloc(  (nbBindings-1) *sizeof(kbinding*) );"
     context b "   if (!newBindings) { return false;}"
     context b "   for (int idx=0;idx < nbBindings ; idx++ ){ "
     context b "    if(idx < indexToRemove){ newBindings[idx] = bindings[idx]; }"
     context b "    if(idx > indexToRemove){ newBindings[idx-1] = bindings[idx]; }"
     context b "   }"
     context b "   if(bindings){ free(bindings); }"
     context b "   bindings = newBindings; "
     */
    context b "  nbBindings--;"
    context b "  return true;  "
    context b " } "
    context b "void destroy(){"
    context b " for(int i=0;i<nbBindings;i++){ free(bindings[i]); } "
    context b " free(bindings);"
    context b "}"
    context b "};"
  }

  def generateRunInstanceMethod(types : List[TypeDefinition]){
    context b "void runInstance(int index){"
    context b "int typeCode = instances[index]->subTypeCode;"
    context b " switch(typeCode){"
    types.foreach{ktype =>
      context b "case "+typeCodeMap.get(ktype.getName).get+":{"
      context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[index];"
      context b "instance->runInstance();"
      context b "break;}"
    }
    context b "}"  
    context b "}"
  }
  
  def generateParamsMethod : Unit = {
    //GENERATE GLOBAL UPDATE METHOD
    
context b "const char delimsEQ[] = \"=\";"
context b "char * key;"
context b "char * val;"
context b "char * str;"
context b "void updateParams(int index,char* params){"
context b "  while ((str = strtok_r(params, \",\", &params)) != NULL){"
context b "    key = strtok(str, delimsEQ);"
context b "    val = strtok(NULL, delimsEQ);"
context b "    updateParam(index,instances[index]->subTypeCode,getIDFromProps(key),val);"
context b "  }"
context b "}"
    
    /*
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
    context b "    updateParam(index,typeCode,getIDFromProps(keyval[0]),keyval[1]);"
    context b "  }"  
    context b "}"  
    */
    
  }
  
  def generateParamMethod(types : List[TypeDefinition]) : Unit = {
    context b "void updateParam(int index,int typeCode,int keyCode,char * val){"
    context b " switch(typeCode){"
    types.foreach{ktype =>
      context b "case "+typeCodeMap.get(ktype.getName).get+":{"
      context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[index];"
      if(ktype.getDictionaryType != null){
        context b " switch(keyCode){"
        ktype.getDictionaryType.getAttributes.foreach{ attribute =>
          context b "case "+propsCodeMap.get(attribute.getName).get+":{"
          context b "strcpy (instance->"+attribute.getName+",val);"
          context b "break;}"
        }
        context b "}" 
      }
      context b "break;}"
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
    context b "destroyInstance(index);"
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
  
  def generateDestroyInstanceMethod(types : List[TypeDefinition]) : Unit = {
    context b "boolean destroyInstance(int index){"
    context b "KevoreeType * instance = instances[index];"
    context b " switch(instance->subTypeCode){"
    types.foreach{ktype =>
      context b "case "+typeCodeMap.get(ktype.getName).get+":{"
      context b "(("+ktype.getName+"*) instances[index])->destroy();"
      context b "break;}"
    }
    context b "}"
    context b "free(instance);"
    context b "}" 
  }


  def generateGlobalInstanceFactory(types : List[TypeDefinition]) : Unit = {
    context b "int createInstance(int typeCode,char* instanceName,char* params){"
    context b "switch(typeCode){"
    types.foreach{ ktype =>
      context b "case "+typeCodeMap.get(ktype.getName).get+":{"
      context b "  "+ktype.getName+" * newInstance = ("+ktype.getName+"*) malloc(sizeof("+ktype.getName+"));"
      context b "  if (newInstance){"
      context b "    memset(newInstance, 0, sizeof("+ktype.getName+"));"
      context b "  } "
      context b "  strcpy(newInstance->instanceName,instanceName);"
      context b "  newInstance->init();"
      context b "  tempInstance = newInstance;"
      //context b "  strcpy(tempInstance->subTypeName,typeName); "
      context b "  tempInstance->subTypeCode = "+typeCodeMap.get(ktype.getName).get+"; "
      context b "  int newIndex = addInstance();"
      context b "  updateParams(newIndex,params);"
      context b "  return newIndex;"
      context b "break;}"
    }
    context b "}"//End switch
    context b " return -1;"
    context b "}"
  }
  
  def generateUnBindMethod(types : List[TypeDefinition]) : Unit = {
    context b "void unbind(int indexComponent,int indexChannel,int portCode){"
    
    context b "QueueList<kmessage> ** requiredPort = 0;"
    context b " switch(instances[indexComponent]->subTypeCode){"
    types.foreach{ktype =>
      if(ktype.isInstanceOf[ComponentType] && ktype.asInstanceOf[ComponentType].getRequired != null && ktype.asInstanceOf[ComponentType].getRequired.size > 0){
        context b "case "+typeCodeMap.get(ktype.getName).get+":{"
        context b "switch(portCode){"
        ktype.asInstanceOf[ComponentType].getRequired.foreach{ required =>
          context b "case "+portCodeMap.get(required.getName).get+":{"
          context b "   requiredPort=&((("+ktype.getName+"*)instances[indexComponent])->"+required.getName+");";
          context b "break;}"
        }
        context b "}"//END SWITCH PORT CODE
        context b "break;}"
      }
    }
    context b "}"//END SWITCH COMPONENT TYPE CODE
    
    context b "if(requiredPort){"
    context b "     *requiredPort=NULL;"
    context b "} else {"
    
    context b " switch(instances[indexChannel]->subTypeCode){"
    types.foreach{ktype =>
      if(ktype.isInstanceOf[ChannelType]){
        context b "case "+typeCodeMap.get(ktype.getName).get+":{"
        context b "(("+ktype.getName+"*)instances[indexChannel])->bindings->removeBinding(instances[indexComponent],portCode);"
        context b "break;}"
      }
    }
    context b "}"//END SWITCH CHANNEL TYPE CODE
    
    context b "}"
    context b "}"
  }
  
  def generateBindMethod(types : List[TypeDefinition]) : Unit = {
    context b "void bind(int indexComponent,int indexChannel,int portCode){"
    context b "QueueList<kmessage> * providedPort = 0;"
    context b "QueueList<kmessage> ** requiredPort = 0;"
    context b "char * componentInstanceName;"
    context b "int componentTypeCode = instances[indexComponent]->subTypeCode;"
    context b "int channelTypeCode = instances[indexChannel]->subTypeCode;"
    //FOR ALL COMPONENT LOOK FOR TYPE
    context b " switch(componentTypeCode){"
    types.foreach{ktype =>
      if(ktype.isInstanceOf[ComponentType]){
        var kcomponentType = ktype.asInstanceOf[ComponentType]
        context b "case "+typeCodeMap.get(ktype.getName).get+":{"
        context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[indexComponent];"
        context b "switch(portCode){"
        if(kcomponentType.getProvided != null){
          kcomponentType.getProvided.foreach{ provided =>
            context b "case "+portCodeMap.get(provided.getName).get+":{"
            context b "   providedPort=instance->"+provided.getName+";";
            context b "componentInstanceName=instance->instanceName;"
            context b "break;}"
          }
        }
        if(kcomponentType.getRequired != null){
          kcomponentType.getRequired.foreach{ required =>
            context b "case "+portCodeMap.get(required.getName).get+":{"
            context b "   requiredPort=&instance->"+required.getName+";";
            context b "break;}"
          }
        } 
        context b "}"
        context b "break;}"  
      }
    }
    context b "}" //END SWITCH COMPONENT TYPE
    
    
    //FOR ALL Channel LOOK FOR TYPE
    context b " switch(channelTypeCode){"
    types.foreach{ktype =>
      if(ktype.isInstanceOf[ChannelType]){
        var kcomponentType = ktype.asInstanceOf[ChannelType]
        context b "case "+typeCodeMap.get(ktype.getName).get+":{"
        context b ktype.getName+" * instance = ("+ktype.getName+"*) instances[indexChannel];"       
        context b "if(providedPort){"
        context b "instance->bindings->addBinding(instances[indexComponent],portCode,providedPort);"
        context b "}"
        context b "if(requiredPort){"
        context b "*requiredPort = instance->input;"
        context b "}" 
        context b "break;}"
      }
    }
    context b "}" //END SWITCH Channel TYPE

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
        if(instance.getDictionary != null){
          instance.getDictionary.getValues.foreach{value =>
            dictionary.put(value.getAttribute.getName,value.getValue)
          }
        }
      }
      val dictionaryResult = new StringBuffer
      dictionary.foreach{dic=> dictionaryResult.append(dic._1+"="+dic._2+",");}
      context b "int index"+instance.getName+" = createInstance("+typeCodeMap.get(instance.getTypeDefinition.getName).get+",\""+instance.getName+"\",\""+dictionaryResult.toString+"\");"
    }
    if(initInstances.filter(i=>i.isInstanceOf[Channel]).size > 0){
      val i0 = initInstances.filter(i=>i.isInstanceOf[Channel]).get(0)
      i0.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach{binding=>
        if(binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == nodeName){
          context b "bind(index"+binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName+",index"+binding.getHub.getName+","+portCodeMap.get(binding.getPort.getPortTypeRef.getName).get+");"
        }
      }
    }
    context b "Serial.println(freeRam());"
    context b "}"  
  }
  
  def generatePeriodicExecutionMethod(types : List[TypeDefinition]) : Unit = {
    context b "boolean periodicExecution(int index){"
    context b " switch(instances[index]->subTypeCode){"
    types.foreach{ktype =>
      if(ktype.getDictionaryType != null){
        if(ktype.getDictionaryType.getAttributes.exists(att => att.getName == "period")){
          context b "case "+typeCodeMap.get(ktype.getName).get+":{"
          context b "return millis() > ((("+ktype.getName+" *) instances[index] )->nextExecution);"
          context b "}" 
        }
      }
    }
    context b "}"//end break
    context b "return false;"
    context b "}"//END FONCTION
  }
  
  def generatePortQueuesSizeMethod(types : List[TypeDefinition]) : Unit = {
    context b "int getPortQueuesSize(int index){"
    context b " switch(instances[index]->subTypeCode){"
    types.foreach{ktype =>
      ktype match {
        case ct :ComponentType => {
            if(ct.getProvided != null && ct.getProvided.size > 0){
              context b "case "+typeCodeMap.get(ktype.getName).get+":{"
              var computeSize = ""
              ct.getProvided.foreach{ providedPort =>
                if(computeSize != ""){computeSize = computeSize + "+"}
                computeSize = computeSize + "((("+ct.getName+" *)instances[index])->"+providedPort.getName+"->count())"
              }
              context b "return "+computeSize+";"
              context b "}"
            }
          }
        case ct :ChannelType => {
            context b "case "+typeCodeMap.get(ktype.getName).get+":{"
            context b "return ((("+ct.getName+" *)instances[index])->input->count());"
            context b "}"
          }
        case _ =>
      }
    }
    context b "}"//end break
    context b "return 0;"
    context b "}"//END FONCTION
  }
  
  def generateNameToIndexMethod() : Unit = {
    context b "  int getIndexFromName(char * id){"
    context b " for(int i=0;i<nbInstances;i++){"
    context b "  if(String(instances[i]->instanceName) == id){ return i; }"
    context b " } "
    context b " return -1;"
    context b "}"
  }
  
  def generateFreeRamMethod() : Unit = {
    context b "    int freeRam () {"
    context b " extern int __heap_start, *__brkval;"
    context b "  int v;"
    context b "  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);"
    context b "}"    
  }
  
  
  
  
  
  
}
