
    byte typeIDB;                                          
    byte portIDB;                                          
    boolean parseForCAdminMsg(){                           
      switch((byte)inBytes[0]){                            
        case AIN_C: {                                      
          insID = strtok(&inBytes[1], delims);             
          typeIDB = (byte)inBytes[strlen(insID)+3];        
          params = &inBytes[strlen(insID)+5];              
          createInstance(typeIDB,insID,params);            
          return true;                                     
          break;                                           
        }                                                  
        case RIN_C: {                                              
          insID = strtok (& inBytes[1], delims);                   
          removeInstance (getIndexFromName (insID) );              
          return true;                                             
          break;                                                   
        }                                                          
        case ABI_C: {                                                           
            insID = strtok(&inBytes[1], delims);                                
            chID = strtok(NULL, delims);                                        
            portIDB = (byte)chID[strlen(chID)+1];                               
            bind(getIndexFromName(insID),getIndexFromName(chID),portIDB);       
            return true;break;                                                  
          }                                                                     
        case RBI_C: {                                                           
            insID = strtok(&inBytes[1], delims);                                
            chID = strtok(NULL, delims);                                        
            portIDB = (byte)chID[strlen(chID)+1];                               
            unbind(getIndexFromName(insID),getIndexFromName(chID),portIDB);       
            return true;break;                                                  
          }                                                                     
        case UDI_C: {                                                                 
          insID = strtok (& inBytes[1], delims);                                      
          params = & inBytes[strlen (insID) + 3];                                     
          updateParams (getIndexFromName (insID), params);                            
        }                                                                             
      }                                                    
    }
                                                          
                                                          
            void executeScriptFromEEPROM () {                                            
              inBytes[serialIndex] = readPMemory(eepromIndex);                           
              while (inBytes[serialIndex] != endAdminChar && eepromIndex < EEPROM_MAX_SIZE) {        
                if (inBytes[serialIndex] == sepAdminChar) {                              
                  inBytes[serialIndex] = '\0';                                           
                  parseForCAdminMsg();                                                   
                  flushAdminBuffer();                                                    
                } else {                                                                 
                  serialIndex ++;                                                        
                }                                                                        
                eepromIndex ++;                                                          
                inBytes[serialIndex] = readPMemory(eepromIndex);                         
              }                                                                          
              //PROCESS LAST CMD                                                         
              if (inBytes[serialIndex] == endAdminChar) {                                
                inBytes[serialIndex] = '\0';                                             
                parseForCAdminMsg();                                                     
                flushAdminBuffer();                                                      
              }                                                                          
   
            }                                                                            
                                                          
                                                          