
     char * insID;
     char * typeID;
     char * params;
     char * chID;
     char * portID;
     const char delims[] = ":";
     boolean parseAndSaveAdminMsg(){
         if( inBytes[0]=='p' && inBytes[1]=='i' && inBytes[2]=='n' && inBytes[3]=='g' ){
           return true;
         }
         if( inBytes[0]=='u' && inBytes[1]=='d' && inBytes[2]=='i' && inBytes[3]==':' ){
           insID = strtok(&inBytes[4], delims);
           params = strtok(NULL, delims);
           saveUDI_CMD(insID,params);
           return true;
         }
         if( inBytes[0]=='a' && inBytes[1]=='i' && inBytes[2]=='n' && inBytes[3]==':' ){
           insID = strtok(&inBytes[4], delims);
           typeID = strtok(NULL, delims);
           params = strtok(NULL, delims);
           saveAIN_CMD(insID,getIDFromType(typeID),params);
           return true;
         }
         if( inBytes[0]=='r' && inBytes[1]=='i' && inBytes[2]=='n' && inBytes[3]==':' ){
           insID = strtok(&inBytes[4], delims);
            saveRIN_CMD(insID);
           return true;
         }
         if( inBytes[0]=='a' && inBytes[1]=='b' && inBytes[2]=='i' && inBytes[3]==':' ){
           insID = strtok(&inBytes[4], delims);
           chID = strtok(NULL, delims);
           portID = strtok(NULL, delims);
           saveBI_CMD(true,insID,chID,getIDFromPortName(portID));
           return true;
         }
         if( inBytes[0]=='r' && inBytes[1]=='b' && inBytes[2]=='i' && inBytes[3]==':' ){
           insID = strtok(&inBytes[4], delims);
           chID = strtok(NULL, delims);
           portID = strtok(NULL, delims);
           saveBI_CMD(false,insID,chID,getIDFromPortName(portID));
           return true;
         }
       return false;
     }