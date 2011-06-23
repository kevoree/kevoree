package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._

/**
 * User: ffouquet
 * Date: 20/06/11
 * Time: 09:47
 */

trait KevoreePersistenceGenerator extends KevoreeCAbstractGenerator {

  def generateSave2Memory(): Unit = {
    context b "void save2Memory(byte b){                                "
    context b "  EEPROM.write(eepromIndex,b);eepromIndex++;  "
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