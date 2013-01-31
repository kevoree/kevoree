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
import templates.SimpleCopyTemplate
import util.Random
import org.kevoree.library.arduinoNodeType.ArduinoBoardType._
import org.kevoree.library.arduinoNodeType.ArduinoBoardType
import org.kevoree.tools.arduino.framework.RawTypeHelper
import org.kevoree.library.arduinoNodeType.ArduinoNode
import org.kevoree.library.arduinoNodeType.utils.ArduinoHelper
import scala.collection.JavaConversions._


trait KevoreeCFrameworkGenerator extends KevoreeCAbstractGenerator {

  def generateMaxSize(boardType: ArduinoBoardType): String = {
    boardType match {
      case ArduinoBoardType.atmega328 => {
        "1024"
      }
      case ArduinoBoardType.atmega1280 => {
        "4096"
      }
      case ArduinoBoardType.uno => {
        "1024"
      }
      case ArduinoBoardType.mega2560 => {
        "4096"
      }
      case _ => {
        "1024"
      }
    }
  }

  def generateKcFrameworkHeaders(types: List[TypeDefinition], boardType: ArduinoBoardType, pmax: String): Unit = {

    context h SimpleCopyTemplate.copyFromClassPath("templates/KevFrameworkHeaders.c")

    val maxSize = pmax match {
      case "" => {
        generateMaxSize(boardType)
      }
      case "MAX" => {
        generateMaxSize(boardType)
      }

      case _ => pmax
    }
    context h "#define EEPROM_MAX_SIZE " + maxSize
    context h "#define MAX_INST_ID 4" //TO CHANGE

    val random = new Random
    context h ("#define kevoreeID1 " + random.nextInt(10))
    context h ("#define kevoreeID2 " + random.nextInt(10))

    context h "int eepromIndex;"


    context h "//Global Kevoree Type Defintion declaration"
    //GENERATE ALL TypeDefinition
    var index = 0
    types.foreach {
      ktype =>
        typeCodeMap.put(ktype.getName, index)
        context h "const prog_char " + ktype.getName.toLowerCase + "[] PROGMEM = \"" + ktype.getName + "\";"
        index = index + 1
    }
    //GENERATE Global TD Tab
    context h "PROGMEM const char * typedefinition[] = { "
    var first = true
    types.foreach {
      ktype =>
        if (first) {
          context h ktype.getName.toLowerCase
        } else {
          context h "," + ktype.getName.toLowerCase
        }
        first = false
    }
    context h "};"
    context h "//Global Kevoree Port Type Defintion declaration"
    //GENERATE ALL Port
    index = 0
    var portNameGenerated: List[String] = List()
    types.filter(p => p.isInstanceOf[ComponentType]).foreach {
      ktype =>
        val ct = ktype.asInstanceOf[ComponentType]
        (ct.getProvided.toList ++ ct.getRequired.toList).toList.foreach {
          ptRef =>
            val portName = ptRef.getName
            val portNamePrefix = ("port_" + portName).toLowerCase
            if (!portNameGenerated.contains(portNamePrefix)) {
              portCodeMap.put(portName, index)
              portNameGenerated = portNameGenerated ++ List(portNamePrefix)
              context h "const prog_char " + portNamePrefix + "[] PROGMEM = \"" + portName + "\";"
              index = index + 1
            }
        }
    }
    //GENERATE Global PORT Tab
    context h "PROGMEM const char * portdefinition[] = { " + portNameGenerated.mkString(",") + "};"

    //GENERATE ALL PROPERTIES NAME
    index = 0
    var propertiesGenerated: List[String] = List()
    types.foreach {
      ktype =>
        if (ktype.getDictionaryType!=null) {
          ktype.getDictionaryType.getAttributes.foreach {
            att =>
              val propName = att.getName
              val propNamePrefix = ("prop_" + propName).toLowerCase
              if (!propertiesGenerated.contains(propNamePrefix)) {
                propsCodeMap.put(propName, index)
                propertiesGenerated = propertiesGenerated ++ List(propNamePrefix)
                context h "const prog_char " + propNamePrefix + "[] PROGMEM = \"" + propName + "\";"
                index = index + 1
              }
          }
        }
    }
    //GENERATE Global PROPERTIES Tab
    context h "PROGMEM const char * properties[] = { " + propertiesGenerated.mkString(",") + "};"

  }

  def generateConstCheckSum(nodename : String,nodeTypeName:String, types: List[TypeDefinition]): Unit = {

    var checksum : Long = 0
    var checksum_header : Long = 0
    var checksum_typedefinition : Long = 0
    var checksum_portdefinition : Long = 0
    var checksum_properties : Long = 0

    checksum_header +=ArduinoHelper.checksumArduino(nodename.toLowerCase)
    checksum_header +=ArduinoHelper.checksumArduino(nodeTypeName.toLowerCase)

    //typedefinition
    types.foreach {
      ktype => {
        val typename = ktype.getName
        checksum_typedefinition   +=ArduinoHelper.checksumArduino(typename.toLowerCase)
      }
    }
    //portdefinition
    types.filter(p => p.isInstanceOf[ComponentType]).foreach {
      ktype =>
        val ct = ktype.asInstanceOf[ComponentType]
        (ct.getProvided.toList ++ ct.getRequired.toList).toList.foreach {
          ptRef =>
          {
            val portName = ptRef.getName
            checksum_portdefinition +=ArduinoHelper.checksumArduino(portName.toLowerCase)
          }}}

    //properties
    var propertiesGenerated: List[String] = List()
    types.foreach {
      ktype =>
        if (ktype.getDictionaryType!=null) {
          ktype.getDictionaryType.getAttributes.foreach {
            att =>  {
              val propName = att.getName
              checksum_properties +=ArduinoHelper.checksumArduino(propName.toLowerCase)

            }

          }
        }
    }
    checksum +=checksum_header
   // checksum +=checksum_typedefinition
   // checksum +=checksum_portdefinition
    //checksum += checksum_properties

    context b "const long checksumTypeDefiniton = " + checksum + ";"
  }

  def generateKcConstMethods(nodename : String,nodeTypeName:String, types: List[TypeDefinition]): Unit = {

    var nbPorts = 0
    types.filter(p => p.isInstanceOf[ComponentType]).foreach {
      ktype =>
        val ct = ktype.asInstanceOf[ComponentType]
        nbPorts = nbPorts + (ct.getProvided.toList ++ ct.getRequired.toList).size
    }
    var propertiesGenerated: List[String] = List()
    types.foreach {
      ktype =>
        if (ktype.getDictionaryType!=null) {
          ktype.getDictionaryType.getAttributes.foreach {
            att =>
              propertiesGenerated = propertiesGenerated ++ List(att.getName)
          }
        }
    }

    context b "const int nbPortType = " + nbPorts + ";"
    context b "int getIDFromPortName(char * portName){"
    context b "  for(int i=0;i<nbPortType;i++){"
    context b "   if(strcmp_P(portName, (char*)pgm_read_word(&(portdefinition[i]))  )==0) { return i; }"
    context b "  }"
    context b "  return -1;"
    context b "}"
    context b "const int nbTypeDef = " + types.size + ";"
    context b "int getIDFromType(char * typeName){"
    context b "  for(int i=0;i<nbTypeDef;i++){"
    context b "   if(strcmp_P(typeName, (char*)pgm_read_word(&(typedefinition[i]))  )==0) { return i; }"
    context b "  }"
    context b "  return -1;"
    context b "}"
    context b "const int nbProps = " + propertiesGenerated.size + ";"
    context b "int getIDFromProps(char * propName){"
    context b "  for(int i=0;i<nbProps;i++){"
    context b "   if(strcmp_P(propName, (char*)pgm_read_word(&(properties[i]))  )==0) { return i; }"
    context b "  }"
    context b "  return -1;"
    context b "}"

    context b "void printNodeName() {"
    context b "Serial.print(F(\""+nodename+"\")); "
    context b "}"

    context b "void printNodeTypeName() {"
    context b "Serial.print(F(\""+nodeTypeName+"\")); "
    context b "}"


    /*
context b "void printlnNodeName() {"
context b "Serial.println(F(\""+nodename+"\")); "
context b "}"
    */
  }


  def generateKcFramework: Unit = {
    context b SimpleCopyTemplate.copyFromClassPath("templates/KevFrameworkUtil.c")
    context b SimpleCopyTemplate.copyFromClassPath("templates/KevFrameworkBase.c")

  }

  def generateRunInstanceMethod(types: List[TypeDefinition]) {
    context b "void runInstance(int index){"
    context b "int typeCode = instances[index]->subTypeCode;"
    context b " switch(typeCode){"
    types.foreach {
      ktype =>
        context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
        context b ktype.getName + " * instance = (" + ktype.getName + "*) instances[index];"
        context b "instance->runInstance();"
        context b "break;}"
    }
    context b "}"
    context b "}"
  }


  def generateParamMethod(types: List[TypeDefinition]): Unit = {
    context b "void updateParam(int index,int typeCode,int keyCode,char * val){"
    context b " switch(typeCode){"
    types.foreach {
      ktype =>
        context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
        context b ktype.getName + " * instance = (" + ktype.getName + "*) instances[index];"
        if (ktype.getDictionaryType!=null) {
          context b " switch(keyCode){"
          ktype.getDictionaryType.getAttributes.foreach {
            attribute =>
              context b "case " + propsCodeMap.get(attribute.getName).get + ":{"


              val rawType: String = attribute.getDatatype.replaceFirst("raw=", "")

              if(RawTypeHelper.isArduinoTypeArray(rawType))
              {
                // parsing array
                rawType match {
                  case _ if (rawType.contains("IntList4")) =>
                  {
                    context b  "parse_IntList4(val,instance->"+attribute.getName+");"
                  }

                }

              }
              else
              {
                RawTypeHelper.getArduinoType(rawType) match {
                  case "long" => {
                    context b "instance->" + attribute.getName + "=atol(val);"
                  }
                  case "int" => {
                    context b "instance->" + attribute.getName + "=atoi(val);"
                  }
                  case _ => {
                    context b "if(strlen(val)<MAX_UNTYPED_DICTIONARY){"
                    context b "strcpy (instance->" + attribute.getName + ",val);"
                    context b "}"
                  }
                }
              }


              context b "instance->updated_p();"
              context b "break;}"
          }
          context b "}"
        }
        context b "break;}"
    }
    context b "}"
    context b "}"
  }


  def generateDestroyInstanceMethod(types: List[TypeDefinition]): Unit = {
    context b "boolean destroyInstance(int index){"
    context b "KevoreeType * instance = instances[index];"
    context b " switch(instance->subTypeCode){"
    types.foreach {
      ktype =>
        context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
        context b "((" + ktype.getName + "*) instances[index])->destroy();"
        context b "break;}"
    }
    context b "}"
    context b "free(instance);"
    context b "}"
  }


  def generateGlobalInstanceFactory(types: List[TypeDefinition]): Unit = {
    context b "int createInstance(int typeCode,char* instanceName,char* params){"
    context b "switch(typeCode){"
    types.foreach {
      ktype =>
        context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
        context b "  " + ktype.getName + " * newInstance = (" + ktype.getName + "*) malloc(sizeof(" + ktype.getName + "));"
        context b "  if (newInstance){"
        context b "    memset(newInstance, 0, sizeof(" + ktype.getName + "));"
        context b "  } "
        context b "  strcpy(newInstance->instanceName,instanceName);"
        context b "  newInstance->init();"
        context b "  tempInstance = newInstance;"
        //context b "  strcpy(tempInstance->subTypeName,typeName); "
        context b "  tempInstance->subTypeCode = " + typeCodeMap.get(ktype.getName).get + "; "
        context b "  int newIndex = addInstance();"
        context b "  updateParams(newIndex,params);"
        context b "  return newIndex;"
        context b "break;}"
    }
    context b "}" //End switch
    context b " return -1;"
    context b "}"
  }

  def generateUnBindMethod(types: List[TypeDefinition]): Unit = {
    context b "void unbind(int indexComponent,int indexChannel,int portCode){"

    context b "kbinding * requiredPort = 0;"
    context b " switch(instances[indexComponent]->subTypeCode){"
    types.foreach {
      ktype =>
        if (ktype.isInstanceOf[ComponentType] && ktype.asInstanceOf[ComponentType].getRequired != null && ktype.asInstanceOf[ComponentType].getRequired.size > 0) {
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          context b "switch(portCode){"
          ktype.asInstanceOf[ComponentType].getRequired.foreach {
            required =>
              context b "case " + portCodeMap.get(required.getName).get + ":{"
              context b "   requiredPort=(((" + ktype.getName + "*)instances[indexComponent])->" + required.getName + ");";
              context b "break;}"
          }
          context b "}" //END SWITCH PORT CODE
          context b "break;}"
        }
    }
    context b "}" //END SWITCH COMPONENT TYPE CODE

    context b "if(requiredPort){"
    context b "     requiredPort->instance=NULL;"
    context b "     requiredPort->port=NULL;"
    context b "     requiredPort->portCode=0;"
    context b "} else {"

    context b " switch(instances[indexChannel]->subTypeCode){"
    types.foreach {
      ktype =>
        if (ktype.isInstanceOf[ChannelType]) {
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          context b "((" + ktype.getName + "*)instances[indexChannel])->bindings->removeBinding(instances[indexComponent],portCode);"
          context b "break;}"
        }
    }
    context b "}" //END SWITCH CHANNEL TYPE CODE

    context b "}"
    context b "}"
  }

  def generateBindMethod(types: List[TypeDefinition]): Unit = {
    context b "void bind(int indexComponent,int indexChannel,int portCode){"
    context b "QueueList<kmessage> * providedPort = 0;"
    //context b "QueueList<kmessage> ** requiredPort = 0;"

    context b "kbinding * requiredPort = 0;"
    context b "char * componentInstanceName;"
    context b "int componentTypeCode = instances[indexComponent]->subTypeCode;"
    context b "int channelTypeCode = instances[indexChannel]->subTypeCode;"
    //FOR ALL COMPONENT LOOK FOR TYPE
    context b " switch(componentTypeCode){"
    types.foreach {
      ktype =>
        if (ktype.isInstanceOf[ComponentType]) {
          val kcomponentType = ktype.asInstanceOf[ComponentType]
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          context b ktype.getName + " * instance = (" + ktype.getName + "*) instances[indexComponent];"
          context b "switch(portCode){"
          if (kcomponentType.getProvided != null) {
            kcomponentType.getProvided.foreach {
              provided =>
                context b "case " + portCodeMap.get(provided.getName).get + ":{"
                context b "   providedPort=instance->" + provided.getName + ";";
                context b "componentInstanceName=instance->instanceName;"
                context b "break;}"
            }
          }
          if (kcomponentType.getRequired != null) {
            kcomponentType.getRequired.foreach {
              required =>
                context b "case " + portCodeMap.get(required.getName).get + ":{"
                context b "   requiredPort=instance->" + required.getName + ";";
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
    types.foreach {
      ktype =>
        if (ktype.isInstanceOf[ChannelType]) {
          var kcomponentType = ktype.asInstanceOf[ChannelType]
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          context b ktype.getName + " * instance = (" + ktype.getName + "*) instances[indexChannel];"
          context b "if(providedPort){"
          context b "instance->bindings->addBinding(instances[indexComponent],portCode,providedPort);"
          context b "}"
          context b "if(requiredPort){"

          context b "requiredPort->portCode = portCode;"
          context b "requiredPort->instance = instance;"
          context b "requiredPort->port = instance->input;"

          //context b "*requiredPort = instance->input;"
          context b "}"
          context b "break;}"
        }
    }
    context b "}" //END SWITCH Channel TYPE

    context b "}"
  }


  def generateSetup(initInstances: List[Instance], nodeName: String): Unit = {

    var instanceCODE: List[String] = List()
    initInstances.foreach {
      instance =>
        instanceCODE = instanceCODE ++ List("init_" + instance.getName.toLowerCase)
        context b "const prog_char init_" + instance.getName.toLowerCase + "[] PROGMEM = \"" + instance.getName + "\";"
    }
    context b "PROGMEM const char * init_tables[] = {" + instanceCODE.mkString(",") + "};"

    //DUMMY SCHEDULER FOR TEST

    context b "char instNameBuf[MAX_INST_ID];"
    context b "char instNameBuf2[MAX_INST_ID];"
    context b "unsigned long previousBootTime;"
    context b "void setup(){"
    context b "Serial.begin("+ ArduinoNode.baudrate+");" //TO REMOVE DEBUG ONLY

    context b "initPMEM();"

    context b "//STATE RECOVERY                                                         "
    context b "if(readPMemory(0) != kevoreeID1 || readPMemory(1) != kevoreeID2){            "
    //context b "  for (int i = 0; i < 512; i++){EEPROM.write(i, 0);}                         " //USELESS NOW
    context b "  save2MemoryNoInc(0,kevoreeID1);                                                "
    context b "  save2MemoryNoInc(1,kevoreeID2);                                                "
    //SAVE INIT STATE
    context b "eepromIndex = 2;"
    context b "save2Memory(startBAdminChar);"
    var isFirst = true
    initInstances.foreach {
      instance =>
      //GENERATE INIT DICTIONARY
        val dictionary: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
        if (instance.getTypeDefinition.getDictionaryType!=null) {
          if (instance.getTypeDefinition.getDictionaryType.getDefaultValues != null) {
            instance.getTypeDefinition.getDictionaryType.getDefaultValues.foreach {
              dv =>
                dictionary.put(dv.getAttribute.getName, dv.getValue)
            }
          }
          if (instance.getDictionary!=null) {
            instance.getDictionary.getValues.foreach {
              value =>
                dictionary.put(value.getAttribute.getName, value.getValue)
            }
          }
        }

        //GENERATE CMD
        if (!isFirst) {
          context b "save2Memory(sepAdminChar);"
        }
        isFirst = false



        val dictionaryResult = new StringBuffer
        dictionary.foreach {
          dic =>
          //context b "EEPROM.write(eepromIndex," + propsCodeMap.get(dic._1).get + ");eepromIndex++;"
          //context b "EEPROM.write(eepromIndex,delimsEQ[0]);eepromIndex++;"
            val key = dic._1.filter(keyC => (Character.isLetterOrDigit(keyC) || keyC == '_') )
            val value = dic._2.filter(keyC => (Character.isLetterOrDigit(keyC) || keyC == '_' || keyC == ';' || keyC == '.' || keyC == '\\' || keyC == '-' || keyC == '*' ) )
            if (dictionaryResult.length() != 0) {
              dictionaryResult.append(",")
            }
            dictionaryResult.append(key + "=" + value)
        }

        val id = instanceCODE.indexOf("init_" + instance.getName.toLowerCase)
        context b "strcpy_P(instNameBuf, (char *) pgm_read_word (&(init_tables[" +id  + "])));      "
        context b "strcpy_P(inBytes, PSTR(\""+dictionaryResult.toString+"\"));                    "
        context b "saveAIN_CMD(instNameBuf, " + typeCodeMap.get(instance.getTypeDefinition.getName).get + ", inBytes);"



    }


    if (initInstances.filter(i => i.isInstanceOf[Channel]).size > 0) {
      val i0 = initInstances.filter(i => i.isInstanceOf[Channel]).get(0)
      i0.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach {
        binding =>
          if (binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName == nodeName) {
            context b "save2Memory(sepAdminChar);"
            context b "strcpy_P(instNameBuf, (char *) pgm_read_word (&(init_tables[" + instanceCODE.indexOf("init_" + binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName.toLowerCase) + "])));        "
            context b "strcpy_P(instNameBuf2, (char *) pgm_read_word (&(init_tables[" + instanceCODE.indexOf("init_" + binding.getHub.getName.toLowerCase) + "])));        "
            context b "saveBI_CMD(true,instNameBuf, instNameBuf2, " + portCodeMap.get(binding.getPort.getPortTypeRef.getName).get + ");"
          }
      }
    }

    context b "save2Memory(endAdminChar);"


    context b "}                                                                            "
    context b "eepromIndex = 2;                                                             "

    //context b "Serial.println(\"Try to recover from state\");                                 "
    context b "inBytes[serialIndex] = readPMemory(eepromIndex);                             "
    context b "if (inBytes[serialIndex] == startBAdminChar) {                               "
    context b "  eepromIndex ++;                                                            "
    context b "previousBootTime = millis();"
    context b "  executeScriptFromEEPROM();                                                 "
    context b "                  kprint(\"bootms\");                                                 "
    context b "                  Serial.println( millis() - previousBootTime );                      "



    context b "}                                                                            "

    context b "kprint(\"mem\");"
    context b "Serial.println(freeRam());"
    context b "kprint(\"emem\");"
    context b "Serial.println(eepromIndex);"

    context b "}"
  }

  def generateParseCAdminMsg(): Unit = {
    context b SimpleCopyTemplate.copyFromClassPath("templates/KevFrameworkCAdminParser.c")
  }

  def generatePeriodicExecutionMethod(types: List[TypeDefinition]): Unit = {
    context b "boolean periodicExecution(int index){"
    context b " switch(instances[index]->subTypeCode){"
    types.foreach {
      ktype =>
        if (ktype.getDictionaryType!=null) {
          if (ktype.getDictionaryType.getAttributes.exists(att => att.getName == "period")) {
            context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
            context b "return millis() > (((" + ktype.getName + " *) instances[index] )->nextExecution);"
            context b "}"
          }
        }
    }
    context b "}" //end break
    context b "return false;"
    context b "}" //END FONCTION
  }

  def generatePortQueuesSizeMethod(types: List[TypeDefinition]): Unit = {
    context b "int getPortQueuesSize(int index){"
    context b " switch(instances[index]->subTypeCode){"
    types.foreach {
      ktype =>
        ktype match {
          case ct: ComponentType => {
            if (ct.getProvided != null && ct.getProvided.size > 0) {
              context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
              var computeSize = ""
              ct.getProvided.foreach {
                providedPort =>
                  if (computeSize != "") {
                    computeSize = computeSize + "+"
                  }
                  computeSize = computeSize + "(((" + ct.getName + " *)instances[index])->" + providedPort.getName + "->count())"
              }
              context b "return " + computeSize + ";"
              context b "}"
            }
          }
          case ct: ChannelType => {
            context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
            context b "return (((" + ct.getName + " *)instances[index])->input->count());"
            context b "}"
          }
          case _ =>
        }
    }
    context b "}" //end break
    context b "return 0;"
    context b "}" //END FONCTION
  }

  def generateNameToIndexMethod(): Unit = {
    context b "int getIndexFromName(char * id){"
    context b " for(int i=0;i<nbInstances;i++){"
    context b "  if(strcmp(instances[i]->instanceName,id)==0){ return i; }"
    context b " } "
    context b " return -1;"
    context b "}"
  }

  def generateFreeRamMethod(): Unit = {
    context b "static int freeRam () {"

    context b "extern int  __bss_end;                                     "
    context b "extern int* __brkval;                                      "
    context b "int free_memory;                                           "
    context b "if (reinterpret_cast<int>(__brkval) == 0) {                "
    context b "  // if no heap use from end of bss section                "
    context b "  free_memory = reinterpret_cast<int>(&free_memory)        "
    context b "                - reinterpret_cast<int>(&__bss_end);       "
    context b "} else {                                                   "
    context b "  // use from top of stack to heap                         "
    context b "  free_memory = reinterpret_cast<int>(&free_memory)        "
    context b "                - reinterpret_cast<int>(__brkval);         "
    context b "}                                                          "
    context b "return free_memory;                                        "

    /*
       context b " extern int __heap_start, *__brkval;"
       context b "  int v;"
       context b "  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);"
    */
    context b "}"
  }

  def generateCompressEEPROM(): Unit = {
    context b "void compressEEPROM(){                                                              "
    context b "eepromIndex=2;                                                                      "
    context b "save2Memory(startBAdminChar);                            "
    context b "for(int i=0;i<nbInstances;i++){                                                     "
    context b " if(i != 0){save2Memory(sepAdminChar);}                  "
    context b " save2Memory(AIN_C);                                       "
    context b " save2Memory(':');                                       "
    context b " for(int j=0;j<(sizeof(instances[i]->instanceName) - 1);j++){                       "
    context b "   if(instances[i]->instanceName[j]!='\\0'){                                         "
    context b "     save2Memory(instances[i]->instanceName[j]);;         "
    context b "   }                                                                                "
    context b " }                                                                                  "
    context b " save2Memory(':');                                       "
    context b " save2Memory(instances[i]->subTypeCode);                 "
    context b " save2Memory(':');                                       "
    context b " savePropertiesToEEPROM(i);                                                                "
    context b "}                                                                                   "

    context b "for (int i = 0;i < nbInstances;i ++){"
    context b "  save2Memory(sepAdminChar);         "
    context b "  saveInstancesBindings(i);          "
    context b "}                                    "

    context b "save2Memory(endAdminChar);                               "
    context b "eepromIndex--;"
    context b " }                                                                                  "
  }

  def generateSaveInstancesBindings(types: List[TypeDefinition]): Unit = {
    context b "  void saveInstancesBindings(int instanceIndex){      "
    context b "   switch(instances[instanceIndex]->subTypeCode){     "
    types.foreach {
      ktype =>
        ktype match {
          case ct: ComponentType => {
            if (ct.getRequired != null && ct.getRequired.size > 0) {
              context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
              ct.getRequired.foreach {
                requiredPort => {
                  context b "                  if ((((" + ct.getName + " *) instances[instanceIndex]) -> " + requiredPort.getName + " -> port))                          "
                  context b "                  {                                                                                    "
                  context b "                    saveBI_CMD(                                                                        "
                  context b "                      true,                                                                            "
                  context b "                      instances[instanceIndex] -> instanceName,                                        "
                  context b "                      ((" + ct.getName + " *) instances[instanceIndex]) -> " + requiredPort.getName + " -> instance -> instanceName,        "
                  context b "                    ((" + ct.getName + " *) instances[instanceIndex]) -> " + requiredPort.getName + " -> portCode                           "
                  context b "                    );                                                                                 "
                  context b "                  }                                                                                    "
                }
              }
              context b "break;"
              context b "}"
            }
          }
          case ct: ChannelType => {
            context b "case " + typeCodeMap.get(ktype.getName).get + ":{"

            context b "  for(int i=0;i<(((" + ct.getName + " *)instances[instanceIndex])->bindings->nbBindings);i++){                   "
            context b "       saveBI_CMD(                                                                                         "
            context b "         true,                                                                                             "
            context b "         ((" + ct.getName + " *)instances[instanceIndex])->bindings->bindings[i]->instance->instanceName,        "
            context b "         instances[instanceIndex]->instanceName,                                                           "
            context b "         ((" + ct.getName + " *)instances[instanceIndex])->bindings->bindings[i]->portCode                       "
            context b "       );                                                                                                  "
            context b "  }                                                                                                        "

            context b "break;"
            context b "}"
          }
          case _ =>
        }
    }
    context b "}" //end break
    context b "}" //END FONCTION
  }


  def generatePushToChannelMethod(types: List[TypeDefinition]): Unit = {
    context b "void pushToChannel(int indexChannel,char * payload){"
    context b "QueueList<kmessage> * providedPort = 0;"
    context b "int channelTypeCode = instances[indexChannel]->subTypeCode;"
    //FOR ALL Channel LOOK FOR TYPE
    context b " switch(channelTypeCode){"
    types.foreach {
      ktype =>
        if (ktype.isInstanceOf[ChannelType]) {
          var kcomponentType = ktype.asInstanceOf[ChannelType]
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          context b ktype.getName + " * instance = (" + ktype.getName + "*) instances[indexChannel];"
          context b "kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));"
          context b "if (smsg){memset(smsg, 0, sizeof(kmessage));}"
          context b "smsg->value = payload;"
          context b "smsg->metric = \"r\";"
          context b "instance->input->push(*smsg);"
          context b "free(smsg);"
          context b "break;}"
        }
    }
    context b "}" //END SWITCH Channel TYPE

    context b "}"
  }


}
