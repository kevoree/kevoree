package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._
import org.kevoree.library.arduinoNodeType.PMemory

/**
 * User: ffouquet
 * Date: 20/06/11
 * Time: 09:47
 */

trait KevoreePersistenceGenerator extends KevoreeCAbstractGenerator {


  def generateReadPMemory(pm: PMemory): Unit = {
    context b "byte readPMemory(int preciseIndex){                                "
    if (pm.equals(PMemory.EEPROM)) {
      context b "  return EEPROM.read(preciseIndex);"
    }
    if (pm.equals(PMemory.SD)) {
      context b "  file.seekSet(preciseIndex);                             "
      context b "  return file.read();                                         "
    }
    context b "}                                                        "
  }

  def generateSave2MemoryNoInc(pm: PMemory): Unit = {
    context b "void save2MemoryNoInc(int preciseIndex,byte b){                                "
    if (pm.equals(PMemory.EEPROM)) {
      context b "  EEPROM.write(preciseIndex,b);"
    }
    if (pm.equals(PMemory.SD)) {
      context b "  file.seekSet(preciseIndex);                             "
      context b "  file.write(b);                                         "
      context b "  file.sync();                                         "
    }
    context b "}                                                        "
  }

  def generateSave2Memory(pm: PMemory): Unit = {
    context b "void save2Memory(byte b){                                "
    if (pm.equals(PMemory.EEPROM)) {
      context b "  EEPROM.write(eepromIndex,b);eepromIndex++;  "
    }
    if (pm.equals(PMemory.SD)) {
      context b "  file.seekSet(eepromIndex);                             "
      context b "  file.write(b);                                         "
      context b "  file.sync();                                         "
      context b "  eepromIndex++;                                         "
    }
    context b "}                                                        "
  }

  def generatePMemInit(pm: PMemory): Unit = {
    context b "    void initPMEM() {               "
    if (pm.equals(PMemory.SD)) {
      context h "#include <Fat16.h>"
      context h "#include <Fat16util.h>"
      context h "SdCard card;"
      context h "Fat16 file;"
      context b "if (!card.init()) Serial.println(\"error.card.init\"); "
      context b "if (!Fat16 :: init(& card)) Serial.println(\"Fat16::init\");    "
      context b "if (file.open(\"PKEVS\", O_CREAT | O_SYNC |O_RDWR)) Serial.println(\"Fat16::open\");  "

      context b "  file.writeError = false;                        "
      context b "  file.rewind();                                   "
      context b "  for (int i = 0; i < EEPROM_MAX_SIZE; i++) {       "
      context b "    file.write((byte)'=');                           "
      context b "  }                                                   "
      context b "  file.rewind();                                       "
    }
    context b "}                                                        "
  }


  def generateRINCommandSave(): Unit = {
    context b "    void saveRIN_CMD (char * instName) {               "
    context b "      save2Memory(RIN_C);                "
    context b "      save2Memory(':');                  "
    context b "      for (int j = 0;                                  "
    context b "      j < strlen(instName);                            "
    context b "      j ++)                                            "
    context b "      {                                                "
    context b "        if (instName[j] != '\\0 ')                    "
    context b "        {                                              "
    context b "          save2Memory(instName[j]);      "
    context b "        }                                              "
    context b "      }                                                "
    context b "    }                                                  "
  }

  def generateBICommandSave(): Unit = {
    context b "    void saveBI_CMD (boolean abiCmd, char * compName, char * chaName, int portCode) {      "
    context b "      if (abiCmd) {                                                                        "
    context b "        save2Memory(ABI_C);                                                  "
    context b "      } else {                                                                             "
    context b "        save2Memory(RBI_C);                                                  "
    context b "      }                                                                                    "
    context b "      save2Memory(delims[0]);                                              "
    context b "      for (int j = 0;                                                                      "
    context b "      j < strlen(compName);                                                                "
    context b "      j ++)                                                                                "
    context b "      {                                                                                    "
    context b "        if (compName[j] != '\\0') {                                                         "
    context b "          save2Memory(compName[j]);                                          "
    context b "        }                                                                                  "
    context b "      }                                                                                     "
    context b "      save2Memory(delims[0] );                                               "
    context b "      for (int j = 0;                                                                      "
    context b "      j < strlen(chaName);                                                                 "
    context b "      j ++)                                                                                "
    context b "      {                                                                                    "
    context b "        if (chaName[j] != '\\0') {                                                          "
    context b "          save2Memory(chaName[j]);                                           "
    context b "        }                                                                                  "
    context b "      }                                                                                    "
    context b "      save2Memory(delims[0] );                                              "
    context b "      save2Memory(portCode);                                                 "
    context b "    }                                                                                      "


  }


  def generateUDICommandSave(): Unit = {
    context b "char * str;"
    context b "boolean first = true;                                                              "
    context b "void saveUDI_CMD(char * instName,char * params){                                   "
    context b " save2Memory(UDI_C);                                    "
    context b " save2Memory(':');                                     "
    context b " for(int j=0;j<strlen(instName);j++){                                        "
    context b "   if(instName[j]!='\\0'){                                                          "
    context b "     save2Memory(instName[j]);                         "
    context b "   }                                                                               "
    context b " }                                                                                 "
    context b " save2Memory(':');                                      "
    context b " first = true;                                                                     "
    context b " while ((str = strtok_r(params, \",\", &params)) != NULL){                           "
    context b "    if(!first){                                                                    "
    context b "      save2Memory(',');                                 "
    context b "    }                                                                              "
    context b "    first = false;                                                                 "
    context b "    key = strtok(str, delimsEQ);                                                   "
    context b "    val = strtok(NULL, delimsEQ);                                                  "
    context b "    save2Memory(getIDFromProps(key));                   "
    context b "    save2Memory('=');                                   "
    context b "    for(int j=0;j<strlen(val);j++){                                          "
    context b "      if(val[j]!='\\0'){                                                            "
    context b "        save2Memory(val[j]);                            "
    context b "      }                                                                            "
    context b "    }                                                                              "
    context b "  }                                                                                "
    context b "}                                                                                  "
  }


  def generateAINCommandSave(): Unit = {
    context b "    void saveAIN_CMD (char * instanceName, int typeID,char * params) {                 "
    context b "      save2Memory(AIN_C);                                                "
    context b "      save2Memory(delims[0]);                                          "
    context b "      for (int i = 0;                                                                  "
    context b "      i < strlen(instanceName);                                                        "
    context b "      i ++)                                                                            "
    context b "      {                                                                                "
    context b "        save2Memory(instanceName[i]);                                    "
    context b "      }                                                                                "
    context b "      save2Memory(delims[0]);                                          "
    context b "      save2Memory(typeID);                                               "
    context b "      save2Memory(delims[0]);                                          "
    context b "      first = true;                                                                    "
    context b "      while ((str = strtok_r(params, \",\", & params)) != NULL) {                        "
    context b "        if (!first) {                                                                  "
    context b "          save2Memory(',');                                              "
    context b "        }                                                                              "
    context b "        first = false;                                                                 "
    context b "        key = strtok(str, delimsEQ);                                                   "
    context b "        val = strtok(NULL, delimsEQ);                                                  "
    context b "        save2Memory(getIDFromProps(key));                                "
    context b "        save2Memory('=');                                                "
    context b "        for (int j = 0;                                                                "
    context b "        j < strlen( val);                                                              "
    context b "        j ++)                                                                          "
    context b "        {                                                                              "
    context b "          if ( val[j] != '\\0')                                                         "
    context b "          {                                                                            "
    context b "            save2Memory(val[j] );                                        "
    context b "          }                                                                            "
    context b "        }                                                                              "
    context b "      }                                                                                "
    context b "    }                                                                                  "
  }


  def generateSavePropertiesMethod(types: List[TypeDefinition]): Unit = {
    context b "void savePropertiesToEEPROM(int instanceIndex){"
    context b " switch(instances[instanceIndex]->subTypeCode){"
    types.foreach {
      ktype =>
        if (ktype.getDictionaryType != null) {
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          var isFirst = true
          ktype.getDictionaryType.getAttributes.foreach {
            att =>
              if (!isFirst) {
                context b "save2Memory(',');"
              }
              context b "save2Memory(" + propsCodeMap.get(att.getName).get + ");"
              context b "save2Memory(delimsEQ[0]);"
              context b "for(int i=0;i<strlen(((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ");i++){"
              context b "save2Memory(((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + "[i]);"
              context b "}"
              isFirst = false
          }
          context b "break;"
          context b "}"
        }
    }
    context b "}"
    context b "}"
  }

}