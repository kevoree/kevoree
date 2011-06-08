package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.TypeDefinition
import scala.collection.JavaConversions._

/**
 * User: ffouquet
 * Date: 06/06/11
 * Time: 12:36
 */

trait KevoreeCSchedulerGenerator extends KevoreeCAbstractGenerator {

  def generateLoop: Unit = {
    /*
    context b "void loop(){"
    context b "for(int i=0;i<nbInstances;i++){"
    context b "  if(periodicExecution(i)){"
    context b "    runInstance(i);"
    context b "  }"
    context b "}"
    context b "for(int i=0;i<nbInstances;i++){"
    context b "  if(getPortQueuesSize(i)>0){"
    context b "    runInstance(i);"
    context b "  }"
    context b "}"
    context b "checkForAdminMsg();"
    context b "}"
    */

    context b "#define minSleepDuration 80                        "
    context b "long minNextOp;                                    "
    context b "boolean allEmptyQueue;                             "
    context b "long duration;                                     "
    context b "void loop(){                                       "
    context b "  checkForAdminMsg();                              "
    context b "  for(int i=0;i<nbInstances;i++){                  "
    context b "    if(periodicExecution(i)){                      "
    context b "      runInstance(i);                              "
    context b "    }                                              "
    context b "  }                                                "
    context b "  allEmptyQueue = true;                            "
    context b "  for(int i=0;i<nbInstances;i++){                  "
    context b "    int queueSize = getPortQueuesSize(i);          "
    context b "    if(getPortQueuesSize(i)>0){                    "
    context b "      allEmptyQueue = false;                       "
    context b "      runInstance(i);                              "
    context b "    }                                              "
    context b "  }                                                "
    context b "//POWER OPTIMISATION                               "
    context b "  if(allEmptyQueue){                               "
    context b "    //COMPUTE NEXT DELAY                           "
    context b "    minNextOp = -1;                                "
    context b "    for(int i=0;i<nbInstances;i++){                "
    context b "      long nextOp = nextExecutionGap(i);           "
    context b "      if(nextOp != -1){                            "
    context b "        if(minNextOp == -1){                       "
    context b "          minNextOp = nextOp;                      "
    context b "        } else {                                   "
    context b "          if(nextOp < minNextOp){                  "
    context b "            minNextOp = nextOp;                    "
    context b "          }                                        "
    context b "        }                                          "
    context b "      }                                            "
    context b "    }//END FOR ALL INSTANCE                        "
    context b "    duration = minNextOp-minSleepDuration;         "
    context b "    if( duration > 0){                             "
    context b "        sleepNowFor(duration);                     "
    context b "    }                                              "
    context b "  }//END IF Qeue size null                         "
    context b "}                                                  "


  }

  def generateSleepMethod() {
    context b "void sleepNowFor(long duration){ "
    //context b "   Narcoleptic.delay(duration);  "  //TO IMPROVE
    context b "}                                "
  }

  def generateCurrentTimeMethod() {
    context b "unsigned long currentMillis(){                          "
    //context b "unsigned long time = (millis() + Narcoleptic.millis()); "
    //context b "return time;                                            "
    context b "return millis();"
    context b "}"
  }

  def generateNextExecutionGap(types : List[TypeDefinition]) : Unit = {
    context b "long nextExecutionGap(int index){"
    context b " switch(instances[index]->subTypeCode){"
    types.foreach{ktype =>
      if(ktype.getDictionaryType != null){
        if(ktype.getDictionaryType.getAttributes.exists(att => att.getName == "period")){
          context b "case "+typeCodeMap.get(ktype.getName).get+":{"
          context b "return (((("+ktype.getName+" *) instances[index] )->nextExecution)- currentMillis());"
          context b "}"
        }
      }
    }
    context b "}"//end break
    context b "return -1;"
    context b "}"//END FONCTION
  }


}