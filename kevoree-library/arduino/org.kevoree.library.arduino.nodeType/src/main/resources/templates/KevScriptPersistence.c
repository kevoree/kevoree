
void
saveRIN_CMD (char *instName)
{
  save2Memory (RIN_C);
  save2Memory (delimitation);
  for (int j = 0; j < strlen (instName); j++)
    {
      if (instName[j] != '\0 ')
	{
	  save2Memory (instName[j]);
	}
    }
}

void
saveBI_CMD (boolean abiCmd, char *compName, char *chaName, int portCode)
{
  if (abiCmd)
    {
      save2Memory (ABI_C);
    }
  else
    {
      save2Memory (RBI_C);
    }
  save2Memory (delims[0]);
  for (int j = 0; j < strlen (compName); j++)
    {
      if (compName[j] != '\0')
	{
	  save2Memory (compName[j]);
	}
    }
  save2Memory (delims[0]);
  for (int j = 0; j < strlen (chaName); j++)
    {
      if (chaName[j] != '\0')
	{
	  save2Memory (chaName[j]);
	}
    }
  save2Memory (delims[0]);
  save2Memory (portCode);
}


char *str;
boolean first = true;
void
saveUDI_CMD (char *instName, char *params)
{
  save2Memory (UDI_C);

  save2Memory (delimitation);
  for (int j = 0; j < strlen (instName); j++)
    {
      if (instName[j] != '\0')
	{
	  save2Memory (instName[j]);
	}
    }
  save2Memory (delimitation);
  first = true;
  while ((str = strtok_r (params, ",", &params)) != NULL)
    {
      if (!first)
	{
	  save2Memory (',');
	}
      first = false;
      key = strtok (str, delimsEQ);
      val = strtok (NULL, delimsEQ);
      save2Memory (getIDFromProps (key));
      save2Memory ('=');
      for (int j = 0; j < strlen (val); j++)
	{
	  if (val[j] != '\0')
	    {
	      save2Memory (val[j]);
	    }
	}
    }
}

void
saveAIN_CMD (char *instanceName, int typeID, char *params)
{
  save2Memory (AIN_C);
  save2Memory (delims[0]);
  for (int i = 0; i < strlen (instanceName); i++)
    {
      save2Memory (instanceName[i]);
    }
  save2Memory (delims[0]);
  save2Memory (typeID);
  save2Memory (delims[0]);
  first = true;
  while ((str = strtok_r (params, ",", &params)) != NULL)
    {
      if (!first)
	{
	  save2Memory (',');
	}
      first = false;
      key = strtok (str, delimsEQ);
      val = strtok (NULL, delimsEQ);
      save2Memory (getIDFromProps (key));
      save2Memory ('=');
      for (int j = 0; j < strlen (val); j++)
	{
	  if (val[j] != '\0')
	    {
	      save2Memory (val[j]);
	    }
	}
    }
}
