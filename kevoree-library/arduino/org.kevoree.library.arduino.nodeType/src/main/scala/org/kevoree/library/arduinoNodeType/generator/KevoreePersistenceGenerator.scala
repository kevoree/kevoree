package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._
import org.kevoree.library.arduinoNodeType.PMemory
import templates.SimpleCopyTemplate

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
      context b "  if(b == endAdminChar | b == sepAdminChar) file.sync();                                         "
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
      context b "  if(b == endAdminChar | b == sepAdminChar) file.sync();                                         "
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
      context b "if (file.open(\"PKEVS\", O_CREAT |O_RDWR)) Serial.println(\"Fat16::open\");  "

      context b "  file.writeError = false;                        "
      context b "  file.rewind();                                   "
      context b "  for (int i = 0; i < EEPROM_MAX_SIZE; i++) {       "
      context b "    file.write((byte)'=');                           "
      context b "  }                                                   "
      context b "  file.rewind();                                       "
    }
    context b "}                                                        "
  }



   def generatePrimitivesPersistence(): Unit = {
    context b SimpleCopyTemplate.copyFromClassPath("templates/KevScriptPersistence.c")
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