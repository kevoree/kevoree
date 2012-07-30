//ARDUINO SERIAL INPUT READ                                        

char ackToken;
boolean parsingAdmin = false;
int eepromPreviousIndex;
boolean firstAdd;
unsigned long timeBeforeScript;
void
checkForAdminMsg ()
{
  if (Serial.peek () == startAdminChar)
    {
      timeBeforeScript = millis ();
      Serial.read ();		//DROP ADMIN START CHAR                                       
      while (!Serial.available () > 0)
	{
	  delay (10);
	}
      ackToken = Serial.read ();
      if (ackToken == 'g' || ackToken == 'p' || ackToken == 'r')
	{
	  if (ackToken == 'g')
	    {
	      printScriptFromEEPROM ();
	    }
	  else if (ackToken == 'p')
	    {
	      while (!Serial.available () > 0)
		{
		  delay (10);
		}
	      ackToken = Serial.read ();
	      Serial.print ("ack");
	      Serial.println (ackToken);
	    } else if (ackToken == 'r')
         {
         	  Serial.println ("ack");
              reset();
         }
	  else
	    {
	      // error
	    }
	}
      else
	{
	  while (!Serial.available () > 0)
	    {
	      delay (10);
	    }
	  if (Serial.read () == startBAdminChar)
	    {
	      parsingAdmin = true;
	      eepromPreviousIndex = eepromIndex;
	      eepromIndex++;
	      firstAdd = true;
	      while (parsingAdmin)
		{
		  if (Serial.available () > 0 && serialIndex < BUFFERSIZE)
		    {
		      inBytes[serialIndex] = Serial.read ();
		      if (inBytes[serialIndex] == sepAdminChar)
			{
			  inBytes[serialIndex] = '\0';
			  //saveScriptCommand();
			  if (!firstAdd)
			    {
			      save2Memory (sepAdminChar);
			    }
			  parseAndSaveAdminMsg ();
			  flushAdminBuffer ();
			  firstAdd = false;
			}
		      else
			{
			  if (inBytes[serialIndex] == endAdminChar)
			    {
			      parsingAdmin = false;
			      inBytes[serialIndex] = '\0';
			      //saveScriptCommand();

			      if (!firstAdd)
				{
				  save2Memory (sepAdminChar);
				}
			      parseAndSaveAdminMsg ();
			      flushAdminBuffer ();
			      save2MemoryNoInc (eepromIndex, endAdminChar);
			      eepromIndex = eepromPreviousIndex + 1;
			      executeScriptFromEEPROM ();

			      save2MemoryNoInc (eepromPreviousIndex, sepAdminChar);	//CLOSE TRANSACTION
			      //COMPRESS EEPROM IF NECESSARY , DON'T GO TO LIMIT
			      if (eepromIndex > (EEPROM_MAX_SIZE - 100))
				{	//TODO REMOVE MAGIC NUMBER !!!
				  compressEEPROM ();
				}
			      kprint ("ms");
			      Serial.println (millis () - timeBeforeScript);
			      kprint ("mem");
			      Serial.println (freeRam ());
			      kprint ("emem");
			      Serial.println (eepromIndex);
			      kprint ("ack");
			      Serial.println (ackToken);
			      firstAdd = false;
			    }
			  else
			    {
			      serialIndex++;
			    }
			}
		    }
		  if (serialIndex >= BUFFERSIZE)
		    {
		      kprintln ("BFO");
		      flushAdminBuffer ();
		      Serial.flush ();
		      parsingAdmin = false;	//KILL PARSING ADMIN
		    }
		}

	    }
	  else
	    {
	      kprintln ("BAM");
	      flushAdminBuffer ();
	      Serial.flush ();
	    }

	}
    }
  else
    {
      processUserMessage ();
    }


}

void
flushAdminBuffer ()
{
  for (int i = 0; i < serialIndex; i++)
    {
      inBytes[serialIndex];
    }
  serialIndex = 0;
}

/* CHECK IF NEW MESSAGE ARRIVE */
void
initUserMessage ()
{
  messageInProgress = true;
  instanceNameRead = false;
  currentMsgIndex = (currentMsgIndex + 1) % MSGBUFFERSIZE;
  currentMsgBufIndex = -1;
}

void
processUserMessage ()
{
  if (!(Serial.available () > 0))
    {
      return;
    }				/* IGNORE \n and check for new message */
  if (Serial.peek () == '#')
    {
      initUserMessage ();
      Serial.read ();
      return;
    }
  if (messageInProgress == true)
    {
      // DO USER MESSAGE
      currentMsgBufIndex = currentMsgBufIndex + 1;
      msgBytes[currentMsgIndex][currentMsgBufIndex] = Serial.read ();
      if (!instanceNameRead)
	{
	  lookForInstanceName ();
	}
      else
	{
	  continueUserMessage ();
	}
    }
  else
    {
      if (Serial.peek () != '$')
	{
	  //Serial.println(Serial.read(),BYTE);
	  Serial.println (Serial.read ());	// fix for arduino 1.0
	}
    }
}

void
lookForInstanceName ()
{
  if (msgBytes[currentMsgIndex][currentMsgBufIndex] == '[')
    {
      msgBytes[currentMsgIndex][currentMsgBufIndex] = '\0';
      currentInstanceID = getIndexFromName (&msgBytes[currentMsgIndex][0]);
      currentMsgBufIndex = -1;
      instanceNameRead = true;
    }
}

void
continueUserMessage ()
{
  if (msgBytes[currentMsgIndex][currentMsgBufIndex] == ']')
    {
      msgBytes[currentMsgIndex][currentMsgBufIndex] = '\0';
      if (currentInstanceID != -1)
	{
	  pushToChannel (currentInstanceID, &msgBytes[currentMsgIndex][0]);
	}
      messageInProgress = false;
    }
}
