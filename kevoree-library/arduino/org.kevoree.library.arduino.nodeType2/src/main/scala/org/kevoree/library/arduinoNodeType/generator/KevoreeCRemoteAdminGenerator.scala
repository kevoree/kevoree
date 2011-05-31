
package org.kevoree.library.arduinoNodeType.generator

trait KevoreeCRemoteAdminGenerator extends KevoreeCAbstractGenerator {

  def generateCheckForAdminMsg(){
    
    context b "  int serialIndex = 0;"
    context b "  char inBytes[200];"
    context b "void checkForAdminMsg(){"
    context b "  while(Serial.available()>0 && serialIndex < 200) {"
    context b "      inBytes[serialIndex] = Serial.read();"
    context b "      if (inBytes[serialIndex] == '\\n' || inBytes[serialIndex] == ';') {"
    context b "        inBytes[serialIndex] = '\\0';"
    context b "Serial.println(inBytes);"
    context b "        parseForAdminMsg(inBytes);"
    context b "        serialIndex = 0;"
    context b "      } else {"
    context b "        serialIndex++;"
    context b "      }"
    context b "  }"
    context b "  if(serialIndex >= 200){"
    context b "      serialIndex = 0;"
    context b "  }"
    context b "}"
  }
  
  def generateConcatKevscriptParser(){
    context b "    boolean parseForAdminMsg(char * msgToTest){"
    context b "  String adminMsg = String(msgToTest);"
    context b "  if(adminMsg.length() < 3){return false;}"
    context b "  if(adminMsg.charAt(0) == 'b' && adminMsg.charAt(1) == '{' ){"
    context b "     int i = 2;"
    context b "     char currentChar = adminMsg.charAt(i);"
    context b "     char * block = (char *)calloc(200,sizeof (char));"
    context b "     while(currentChar != '}' && currentChar != '\0'){"
    context b "      block[i-2] = currentChar;"
    context b "      i++;currentChar = adminMsg.charAt(i);"
    context b "     }"
    context b "     block[i-2] = '\0';   "
    context b "     char * str;"
    context b "     char *p = block;"
    context b "     while ((str = strtok_r(p, \"/\", &p)) != NULL){"
    context b "      char * str2;"
    context b "      char* values[5];"
    context b "      int valueIndex = 0;"
    context b "      while ((str2 = strtok_r(str, \":\", &str)) != NULL){"
    context b "        values[valueIndex] = str2;"
    context b "        valueIndex ++;"
    context b "      }  "
    context b "      if(String(values[0]) == \"aco\" && valueIndex  >= 3){//ACO CHECK"
    context b "        if(valueIndex == 4){"
    context b "           createInstance(values[2],values[1],values[3]);"
    context b "        } else {"
    context b "           createInstance(values[2],values[1],\"\"); "
    context b "        }"
    context b "      } //END ACO CHECK      "
    context b "      if(String(values[0]) == \"abi\" && valueIndex  == 4){//ACO CHECK"
    context b "           bind(getIndexFromName(values[1]),getIndexFromName(values[2]),values[3]);"
    context b "      } //END ACO CHECK     "
    context b "      if(String(values[0]) == \"rbi\" && valueIndex  == 4){//ACO CHECK"
    context b "           unbind(getIndexFromName(values[1]),getIndexFromName(values[2]),values[3]);"
    context b "      } //END ACO CHECK   "
    context b "      if(String(values[0]) == \"udi\" && valueIndex  == 3){//ACO CHECK"
    context b "           updateParams(getIndexFromName(values[1]),values[2]);"
    context b "      } //END ACO CHECK   "
    context b "     }"
    context b "     free(block);return true;"
    context b "  }"
    context b "  return false;"
    context b "}"
    
    
  }
  
}
