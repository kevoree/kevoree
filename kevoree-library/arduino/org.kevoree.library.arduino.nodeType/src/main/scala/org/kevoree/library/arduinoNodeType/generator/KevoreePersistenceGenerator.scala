package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.TypeDefinition
import org.kevoree.library.arduinoNodeType.PMemory
import templates.SimpleCopyTemplate
import org.kevoree.tools.arduino.framework.RawTypeHelper

/**
 * User: ffouquet
 * Date: 20/06/11
 * Time: 09:47
 */

trait KevoreePersistenceGenerator extends KevoreeCAbstractGenerator {

  def generatePMemoryPrimitives(pm: PMemory): Unit = {
    if (pm.equals(PMemory.EEPROM)) {
      context b SimpleCopyTemplate.copyFromClassPath("templates/KevScriptPersistenceStorageEEPROM.c")
    }
    if (pm.equals(PMemory.SD)) {
      context b SimpleCopyTemplate.copyFromClassPath("templates/KevScriptPersistenceStorageSD.c")
    }
  }

  def generatePMemInit(pm: PMemory): Unit = {
    context b "    void initPMEM() {               "
    if (pm.equals(PMemory.SD)) {
      context h "#include <Fat16.h>"
      //  context h "#include <Fat16util.h>"
      context h "SdCard card;"
      context h "Fat16 file;"
      context b "if (!card.init()) kprintln(\"error.card.init\"); "
      context b "if (!Fat16 :: init(& card)) kprintln(\"Fat16::init\");    "
      context b "if (file.open(\"PKEVS\", O_CREAT |O_RDWR)) kprintln(\"Fat16::open\");  "

      context b "  file.writeError = false;                        "
      context b "  file.rewind();                                   "
      context b "    if(file.fileSize() !=  EEPROM_MAX_SIZE){                   "
      context b "    for (int i = 0; i < EEPROM_MAX_SIZE; i++) {                 "
      context b "      file.write((byte)'=');                                     "
      context b "    }                                                             "
      context b "    file.rewind();                                                 "
      context b "  }                                                                 "

    } else {
      context h "#include <EEPROM.h>"
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
        if (ktype.getDictionaryType.isDefined) {
          context b "case " + typeCodeMap.get(ktype.getName).get + ":{"
          var isFirst = true
          ktype.getDictionaryType.get.getAttributes.foreach {
            att =>
              if (!isFirst) {
                context b "save2Memory(',');"
              }
              context b "save2Memory(" + propsCodeMap.get(att.getName).get + ");"
              context b "save2Memory(delimsEQ[0]);"

              RawTypeHelper.getArduinoType(att.getDatatype.replace("raw=", "")) match {
                case "long" => {
                  context b "{"
                  context b "char tempBuf[12];"
                  context b "  sprintf(tempBuf, \"%lu\", ((" + ktype.getName + " *) instances[instanceIndex])->"+att.getName+");"
                  context b "for(int i=0;i<strlen(tempBuf);i++){save2Memory(tempBuf[i]);}"
                  context b "}"

//                  context b " save2Memory((int)(((((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ") >> 24) & 0xFF));"
 //                 context b " save2Memory((int)(((((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ") >> 16) & 0xFF));"
 //                 context b " save2Memory((int)(((((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ") >> 8) & 0xFF));"
  //                context b " save2Memory((int)(((((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ") & 0xFF)));"
                }
                case "int" => {
                  context b "{"
                  context b "char tempBuf[8];"
                  context b "  sprintf(tempBuf, \"%lu\", ((" + ktype.getName + " *) instances[instanceIndex])->"+att.getName+");"
                  context b "for(int i=0;i<strlen(tempBuf);i++){save2Memory(tempBuf[i]);}"
                  context b "}"
                 // context b " save2Memory((int)(((((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ") >> 8) & 0xFF));"
                  //context b " save2Memory((int)(((((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ") & 0xFF)));"
                }
                case _ => {
                  context b "for(int i=0;i<strlen(((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + ");i++){"
                  context b "save2Memory(((" + ktype.getName + " *) instances[instanceIndex])->" + att.getName + "[i]);"
                  context b "}"
                }
              }
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