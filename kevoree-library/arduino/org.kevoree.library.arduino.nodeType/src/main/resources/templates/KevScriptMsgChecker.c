     //ARDUINO SERIAL INPUT READ                                        
     #define BUFFERSIZE 100                                             
     int serialIndex = 0;                                               
     char inBytes[BUFFERSIZE];                                          
     const char startBAdminChar = '{';                                  
     const char endAdminChar = '}';                                     
     const char startAdminChar = '$';                                   
     const char sepAdminChar = '/';                                     
     char ackToken;                                                     
     boolean parsingAdmin = false;                                      
     int eepromPreviousIndex;                                               
     boolean firstAdd ;                                                     
     unsigned long timeBeforeScript; 
         void checkForAdminMsg () {                                                       
           if (Serial.peek() == startAdminChar) {                                         
             timeBeforeScript = millis(); 
             Serial.read(); //DROP ADMIN START CHAR                                       
             while (!Serial.available() > 0) {                                            
               delay(10);                                                                 
             }                                                                            
             ackToken = Serial.read();                                                    
             while (!Serial.available() > 0) {                                            
               delay(10);                                                                 
             }                                                                             
             if (Serial.read() == startBAdminChar) {                                       
               parsingAdmin = true;                                                        
               eepromPreviousIndex = eepromIndex;                                          
               eepromIndex ++;                                                             
               firstAdd = true;                                                            
               while (parsingAdmin) {                                                      
                 if (Serial.available() > 0 && serialIndex < BUFFERSIZE) {                 
                   inBytes[serialIndex] = Serial.read();                                   
                   if (inBytes[serialIndex] == sepAdminChar) {                             
                     inBytes[serialIndex] = '\0';
                     //saveScriptCommand();                                                
                     if (!firstAdd) {                                                      
                       save2Memory(sepAdminChar);            
                     }                                                                     
                     parseAndSaveAdminMsg();                                               
                     flushAdminBuffer();                                                   
                     firstAdd = false;                                                     
                   } else {                                                                
                     if (inBytes[serialIndex] == endAdminChar) {                           
                       parsingAdmin = false;                                               
                       inBytes[serialIndex] = '\0';
                       //saveScriptCommand();                                              
                                                                                           
                       if (!firstAdd) {                                                    
                         save2Memory(sepAdminChar);          
                       }                                                                   
                       parseAndSaveAdminMsg();                                             
                       flushAdminBuffer();                                                 
               save2MemoryNoInc(eepromIndex, endAdminChar);                                    
               eepromIndex = eepromPreviousIndex + 1;                                      
               executeScriptFromEEPROM();                                                  
                                                                                           
               save2MemoryNoInc(eepromPreviousIndex, sepAdminChar); //CLOSE TRANSACTION        
    //COMPRESS EEPROM IF NECESSARY , DON'T GO TO LIMIT
               if(eepromIndex > (EEPROM_MAX_SIZE-100)){         
                 compressEEPROM();             
               }                               
                       Serial.print("ms");
                       Serial.println( millis() - timeBeforeScript );                       
                       Serial.print("mem");
                       Serial.println(freeRam());                                          
                       Serial.print("emem");
                       Serial.println(eepromIndex);                                          
                       Serial.print("ack");
                       Serial.println(ackToken);                                           
                       firstAdd = false;                                                   
                     } else {                                                              
                       serialIndex ++;                                                     
                     }                                                                     
                   }                                                                        
                 }                                                                         
                 if (serialIndex >= BUFFERSIZE) {                                          
                   Serial.println("BFO");
                   flushAdminBuffer();                                                     
                   Serial.flush();                                                         
                   parsingAdmin = false; //KILL PARSING ADMIN                              
                 }                                                                         
               }                                                                           
                                                                                           
             } else {                                                                      
               Serial.println("BAM");
               flushAdminBuffer();                                                          
               Serial.flush();                                                              
             }                                                                              
           }                                                                                
         }                                                                                  
     void flushAdminBuffer(){                                           
       for(int i=0;i<serialIndex;i++){                                  
          inBytes[serialIndex];                                         
       }                                                                
       serialIndex = 0;                                                 
     }                                                                  
     //END SECTION ADMIN DETECTION 1 SPLIT                              
