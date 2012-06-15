byte typeIDB;
byte portIDB;
boolean
parseForCAdminMsg ()
{
  if (inBytes[0] == UDI_C)
    {
      insID = strtok (&inBytes[1], delims);
      params = &inBytes[strlen (insID) + 3];
      updateParams (getIndexFromName (insID), params);
      return true;
    }
  else if (inBytes[0] == ABI_C)
    {
      insID = strtok (&inBytes[1], delims);
      chID = strtok (NULL, delims);
      portIDB = (byte) chID[strlen (chID) + 1];
      bind (getIndexFromName (insID), getIndexFromName (chID), portIDB);
      return true;
    }
  else if (inBytes[0] == AIN_C)
    {
      insID = strtok (&inBytes[1], delims);
      typeIDB = (byte) inBytes[strlen (insID) + 3];
      params = &inBytes[strlen (insID) + 5];
      createInstance (typeIDB, insID, params);
      return true;
    }
  else if (inBytes[0] == RIN_C)
    {
      insID = strtok (&inBytes[1], delims);
      removeInstance (getIndexFromName (insID));
      return true;
    }
  else if (inBytes[0] == RBI_C)
    {
      insID = strtok (&inBytes[1], delims);
      chID = strtok (NULL, delims);
      portIDB = (byte) chID[strlen (chID) + 1];
      unbind (getIndexFromName (insID), getIndexFromName (chID), portIDB);
      return true;
    }
  return false;
}


void
executeScriptFromEEPROM ()
{
  inBytes[serialIndex] = readPMemory (eepromIndex);
  while (inBytes[serialIndex] != endAdminChar
	 && eepromIndex < EEPROM_MAX_SIZE)
    {
      if (inBytes[serialIndex] == sepAdminChar)
	{
	  inBytes[serialIndex] = '\0';
	  parseForCAdminMsg ();
	  flushAdminBuffer ();
	}
      else
	{
	  serialIndex++;
	}
      eepromIndex++;
      inBytes[serialIndex] = readPMemory (eepromIndex);
    }
  //PROCESS LAST CMD                                                         
  if (inBytes[serialIndex] == endAdminChar)
    {
      inBytes[serialIndex] = '\0';
      parseForCAdminMsg ();
      flushAdminBuffer ();
    }
}

void
printScriptFromEEPROM ()
{
  int i = 0;
  int indexInBytes = 0;
  int indexEEPROM = 3;

  Serial.print (startAdminChar);
  printNodeName();
  Serial.print(delimitation);
  printNodeTypeName();
  Serial.print (F("@"));
  Serial.println (startBAdminChar);

  for (i = 0; i < nbProps; i++)
    {
        strcpy_P(inBytes, (char*)pgm_read_word(&(properties[i])));
        Serial.print( inBytes );
        if(i < nbProps-1){
                   Serial.print(delimitation);
        }
    }
    if(nbProps > 0){
      Serial.print (F(","));
    }

  for (i = 0; i < nbTypeDef; i++)
    {
        strcpy_P(inBytes, (char*)pgm_read_word(&(typedefinition[i])));
        Serial.print( inBytes );
        if(i < nbTypeDef-1){
                   Serial.print(delimitation);
        }
    }
    if(nbTypeDef > 0){
      Serial.print (F(","));
    }

  for (i = 0; i < nbPortType; i++)
    {
     strcpy_P(inBytes, (char*)pgm_read_word(&(portdefinition[i])));
     Serial.print( inBytes );
        if(i < nbPortType-1){
                   Serial.print(delimitation);
        }
    }

    if(nbTypeDef > 0|nbProps > 0|nbPortType > 0 )
    {
         Serial.println (sepAdminChar);
    }
   // todo checksum EEPROM
  do
    {
      inBytes[indexInBytes] = readPMemory (indexEEPROM);

   if (inBytes[indexInBytes] == sepAdminChar | indexEEPROM == eepromIndex )
	{
	  inBytes[indexInBytes] = '\0';

	  if (inBytes[0] == UDI_C)
	    {
	      insID = strtok (&inBytes[1], delims);
	      Serial.print (UDI_C);
	      Serial.print (delimitation);
	      Serial.print (insID);


	      i = (strlen (insID) + 3);
                if(i != indexInBytes)
                     {

	           Serial.print (delimitation);
           	      do
           		{

           		  Serial.print (inBytes[i], DEC);
           		  Serial.print (F ("="));
           		  i = i + 2;
           		  do
           		    {
           		      Serial.print (inBytes[i]);
           		      i++;
           		    }
           		  while (inBytes[i] != ',' && inBytes[i] != '\0' && inBytes[i]!= sepAdminChar );
           		  if (inBytes[i] != '\0')
           		    {
           		      Serial.print (F (","));
           		    }
           		  i++;
           		}
	      while (inBytes[i+1]  != '\0' && i <= indexInBytes);
	      }

	    }
	  else if (inBytes[0] == AIN_C)
	    {
	      insID = strtok (&inBytes[1], delims);
	      typeIDB = (byte) inBytes[strlen (insID) + 3];
	      Serial.print (AIN_C);
	      Serial.print (delimitation);
	      Serial.print (insID);
	      Serial.print (delimitation);
	      Serial.print (typeIDB);

	      i = strlen (insID) + 5;
                  if(i != indexInBytes)
                    {
          	           Serial.print (delimitation);
	      do
		{

		  Serial.print (inBytes[i], DEC);
		  Serial.print (F ("="));
		  i = i + 2;
		  do
		    {
		      Serial.print (inBytes[i]);
		      i++;
		    }
		  while (inBytes[i] != ',' && inBytes[i] != '\0' && inBytes[i]!= sepAdminChar );
		  if (inBytes[i] != '\0')
		    {
		      Serial.print (F (","));
		    }
		  i++;
		}
	      while (inBytes[i+1]  != '\0' && i <= indexInBytes);
	           }
	    }
	  else if (inBytes[0] == ABI_C)
	    {
	      insID = strtok (&inBytes[1], delims);
	      chID = strtok (NULL, delims);
	      portIDB = (byte) chID[strlen (chID) + 1];

	      Serial.print (ABI_C);
	      Serial.print (delimitation);
	      Serial.print (insID);
	      Serial.print (delimitation);
	      Serial.print (chID);
	      Serial.print (delimitation);
	      Serial.print (portIDB);
	    }
	  else if (inBytes[0] == RIN_C)
	    {
	      insID = strtok (&inBytes[1], delims);
	      Serial.print (RIN_C);
	      Serial.print (delimitation);
	      Serial.print (insID);
	    }
	  else if (inBytes[0] == RBI_C)
	    {
	      insID = strtok (&inBytes[1], delims);
	      chID = strtok (NULL, delims);
	      portIDB = (byte) chID[strlen (chID) + 1];
	      Serial.print (RBI_C);
	      Serial.print (delimitation);
	      Serial.print (insID);
	      Serial.print (chID);
	      Serial.print (portIDB);
	    }

	  if (firstAdd == false)
	    {
	    if(inBytes[0] == RBI_C | inBytes[0] == RIN_C |inBytes[0] == ABI_C |inBytes[0] == AIN_C | inBytes[0] == UDI_C) {
	   	      Serial.println (sepAdminChar);
	    }
	    }
	  else
	    {
	      firstAdd = false;
	    }
	  Serial.flush ();
	  indexInBytes = 0;
	  memset (&inBytes, 0, sizeof (inBytes));
	}
      else
	{
	  indexInBytes++;
	}
      indexEEPROM++;

    }
  while ((inBytes[indexInBytes - 1] != endAdminChar)&&  (indexEEPROM != eepromIndex+1)	 && (indexEEPROM < EEPROM_MAX_SIZE));

  Serial.print (endAdminChar);
  Serial.print("+");
  Serial.print(checksumTypeDefiniton);
  Serial.println("!");

  Serial.flush ();
}
