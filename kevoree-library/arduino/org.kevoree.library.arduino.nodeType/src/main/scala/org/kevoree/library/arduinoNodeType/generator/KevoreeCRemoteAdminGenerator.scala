package org.kevoree.library.arduinoNodeType.generator

trait KevoreeCRemoteAdminGenerator extends KevoreeCAbstractGenerator {

  def generateCheckForAdminMsg() {
    context b "//ARDUINO SERIAL INPUT READ "
    context b "#define BUFFERSIZE 100      "
    context b "int serialIndex = 0;        "
    context b "char inBytes[BUFFERSIZE];   "
    context b "void checkForAdminMsg(){     "
    context b "  if(Serial.available()>0 && serialIndex < BUFFERSIZE) { "
    context b "    inBytes[serialIndex] = Serial.read();                 "
    context b "    if (inBytes[serialIndex] == '\\n' || inBytes[serialIndex] == ';') {  "
    context b "              inBytes[serialIndex] = '\0';  "
    context b "              parseForAdminMsg();                "
    context b "               for(int i=0;i<serialIndex;i++){   "
    context b "                    inBytes[serialIndex];       "
    context b "                }                               "
    context b "                serialIndex = 0;                "
    context b "               Serial.println(freeRam());        "
    context b "    } else {      "
    context b "      serialIndex++;   "
    context b "    }       "
    context b "  }          "
    context b "  if(serialIndex >= BUFFERSIZE){   "
    context b "    Serial.println(\"Buffer overflow\");  "
    context b "      for(int i=0;i<serialIndex;i++){   "
    context b "          inBytes[serialIndex];         "
    context b "      }                                "
    context b "      serialIndex = 0;                "
    context b "  }                                   "
    context b "}                                    "
  }

  def generateConcatKevscriptParser(): Unit = {
    context b "char * insID;  "
    context b "char * typeID; "
    context b "char * params;   "
    context b "char * chID;        "
    context b "char * portID;            "
    context b "const char delims[] = \":\";    "
    context b "char ackToken;"
    context b "boolean parseForAdminMsg(){       "
    context b "  if(serialIndex < 6){return false;}    "

    /*
    context b "if( inBytes[2]=='r' && inBytes[3]=='s' && inBytes[4]=='t' ){//SOFTWARE RESET "
    //context b "   cli();wdt_enable(WDTO_15MS);                                    "
    //context b "   while(1) {};                                              "
    context b " }                                                           "
      */


    //  context b "  Serial.println(inBytes);          "
    context b "  if(inBytes[1] == '{' && inBytes[serialIndex-1] == '}'  ){  "
    context b "    ackToken = inBytes[0];"
    context b "    inBytes[serialIndex-1] = '\\0';   "


    context b "    if( inBytes[2]=='p' && inBytes[3]=='i' && inBytes[4]=='n' && inBytes[5]=='g' ){  "
    context b "Serial.print(\"ack\"); "
    context b "Serial.println(ackToken); "
    context b "      return true;      "
    context b "    }   "

    context b "    if( inBytes[2]=='u' && inBytes[3]=='d' && inBytes[4]=='i' && inBytes[5]==':' ){  "
    context b "      insID = strtok(&inBytes[6], delims);  "
    context b "      params = strtok(NULL, delims);    "
    context b "      updateParams(getIndexFromName(insID),params);   "
    context b "Serial.print(\"ack\"); "
    context b "Serial.println(ackToken); "
    context b "      return true;      "
    context b "    }   "



    context b "    if( inBytes[2]=='a' && inBytes[3]=='i' && inBytes[4]=='n' && inBytes[5]==':' ){ "
    context b "      insID = strtok(&inBytes[6], delims); "
    context b "      typeID = strtok(NULL, delims);"
    context b "      params = strtok(NULL, delims); "
    // context b "      Serial.println(insID);"
    // context b "      Serial.println(typeID); "
    // context b "      Serial.println(params); "
    context b "      createInstance(getIDFromType(typeID),insID,params);  "

    context b "Serial.print(\"ack\"); "
    context b "Serial.println(ackToken); "

    context b "      return true;"
    context b "    }      "
    context b "    if( inBytes[2]=='r' && inBytes[3]=='i' && inBytes[4]=='n' && inBytes[5]==':' ){    "
    context b "      insID = strtok(&inBytes[6], delims);   "
    // context b "      Serial.println(insID);                "
    context b "      removeInstance(getIndexFromName(insID));  "

    context b "Serial.print(\"ack\"); "
    context b "Serial.println(ackToken); "


    context b "      return true;   "
    context b "    }               "
    context b "    if( inBytes[2]=='a' && inBytes[3]=='b' && inBytes[4]=='i' && inBytes[5]==':' ){   "
    context b "      insID = strtok(&inBytes[6], delims);                                             "
    context b "      chID = strtok(NULL, delims);                                                      "
    context b "      portID = strtok(NULL, delims);                                                     "
    // context b "      Serial.println(insID);                                                              "
    // context b "      Serial.println(chID);                                                                "
    //  context b "     Serial.println(portID);                                                               "
    context b "      bind(getIndexFromName(insID),getIndexFromName(chID),getIDFromPortName(portID));                        "
    context b "Serial.print(\"ack\"); "
    context b "Serial.println(ackToken); "


    context b "      return true;                                                                            "
    context b "    }                                                                                         "
    context b "    if( inBytes[2]=='r' && inBytes[3]=='b' && inBytes[4]=='i' && inBytes[5]==':' ){           "
    context b "      insID = strtok(&inBytes[6], delims);                                                    "
    context b "      chID = strtok(NULL, delims);                                                            "
    context b "      portID = strtok(NULL, delims);                                                          "
    //  context b "      Serial.println(insID);                                                                  "
    //  context b "      Serial.println(chID);                                                                   "
    //   context b "     Serial.println(portID);                                                                 "
    context b "      unbind(getIndexFromName(insID),getIndexFromName(chID),getIDFromPortName(portID));                        "

    context b "Serial.print(\"ack\"); "
    context b "Serial.println(ackToken); "

    context b "      return true;                                                                            "
    context b "    }                                                                                         "
    context b "  }                                                                                           "
    context b "  return false;                                                                               "
    context b "}                                                                                             "
  }
}