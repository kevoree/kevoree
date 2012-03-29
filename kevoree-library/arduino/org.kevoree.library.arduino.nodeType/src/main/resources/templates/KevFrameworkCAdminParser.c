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
  int indexEEPROM = 2;
   firstAdd = true;
  // cleaning memory
  memset (&inBytes, 0, sizeof (inBytes));
  Serial.println (startBAdminChar);
  printNodeName();
  Serial.println (sepAdminChar);
  do
    {
      inBytes[indexInBytes] = readPMemory (indexEEPROM);

      if (inBytes[indexInBytes] == sepAdminChar)
	{
	  inBytes[indexInBytes] = '\0';

	  if (inBytes[0] == UDI_C)
	    {
	      insID = strtok (&inBytes[1], delims);
	      Serial.print (UDI_C);
	      Serial.print (delimitation);
	      Serial.print (insID);
	      Serial.print (delimitation);
	      i = (strlen (insID) + 3);
	      do
		{
		  //propertie
		  Serial.print (inBytes[i], DEC);
		  i++;
		  do
		    {
		      Serial.print (inBytes[i]);
		      i++;
		    }
		  while (inBytes[i] != ',' && inBytes[i] != '\0');
		}
	      while (inBytes[i] != '\0');

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
	      Serial.print (delimitation);
	      i = strlen (insID) + 5;
	    do
		{
		  //propertie
		  Serial.print (inBytes[i], DEC);
		  i++;
		  do
		    {
		      Serial.print (inBytes[i]);
		      i++;
		    }
		  while (inBytes[i] != ',' && inBytes[i] != '\0');
		}
	      while (inBytes[i] != '\0');

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
	      Serial.println (insID);
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

		if(firstAdd == false)
		{
    	    Serial.println (sepAdminChar);
         }else
         {
         firstAdd=false;
         }
    	     Serial.flush();
	  indexInBytes = 0;
	  memset (&inBytes, 0, sizeof (inBytes));
	} else 	{    indexInBytes++;  	}
      indexEEPROM++;
    }
  while ((inBytes[indexInBytes - 1] != endAdminChar)	 && (indexEEPROM < EEPROM_MAX_SIZE));

  Serial.println (endAdminChar);
  Serial.flush ();

}
