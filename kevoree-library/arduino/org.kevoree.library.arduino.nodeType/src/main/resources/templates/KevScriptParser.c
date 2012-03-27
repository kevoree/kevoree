
char *insID;
char *typeID;
char *params;
char *chID;
char *portID;
const char delims[] = ":";
boolean
parseAndSaveAdminMsg ()
{
  //  UDI_C
  if (inBytes[0] == UDI_C && inBytes[1] == delimitation)
    {
      insID = strtok (&inBytes[2], delims);
      params = strtok (NULL, delims);
      saveUDI_CMD (insID, params);
      return true;
    }
  //  AIN_C
  if (inBytes[0] == AIN_C && inBytes[1] == delimitation)
    {
      insID = strtok (&inBytes[2], delims);
      typeID = strtok (NULL, delims);
      params = strtok (NULL, delims);
      saveAIN_CMD (insID, getIDFromType (typeID), params);
      return true;
    }
  //RIN_C
  if (inBytes[0] == RIN_C && inBytes[1] == delimitation)
    {
      insID = strtok (&inBytes[2], delims);
      saveRIN_CMD (insID);
      return true;
    }
  //ABI_C
  if (inBytes[0] == ABI_C && inBytes[1] == delimitation)
    {
      insID = strtok (&inBytes[2], delims);
      chID = strtok (NULL, delims);
      portID = strtok (NULL, delims);
      saveBI_CMD (true, insID, chID, getIDFromPortName (portID));
      return true;
    }
  //RBI_C
  if (inBytes[0] == RBI_C && inBytes[1] == delimitation)
    {
      insID = strtok (&inBytes[2], delims);
      chID = strtok (NULL, delims);
      portID = strtok (NULL, delims);
      saveBI_CMD (false, insID, chID, getIDFromPortName (portID));
      return true;
    }
  return false;
}

int
parse_array_int (int size, char *dico, int val[])
{
#define MAX_SIZE_INT 6
  int count = 0, i = 0, j = 0;
  char parsing[MAX_SIZE_INT];
  // init array val
  for (i = 0; i < size; i++)
    {
      val[i] = 0;
    }
  if ((int) strlen (dico) > 1)
    {
      for (i = 0; i < (int) strlen (dico) + 1; i++)
	{
	  if (dico[i] != ';' && i != strlen (dico))
	    {
	      if (j < MAX_SIZE_INT)
		{
		  parsing[j] = dico[i];
		  j++;
		}
	      else
		{
		  strcpy (parsing, "0");
		}
	    }
	  else
	    {
	      val[count] = atoi (parsing);
	      memset (parsing, 0, sizeof (parsing));
	      count++;
	      if (count > size)
		{		/* the dico is bigger */
		  return -2;
		}
	      j = 0;
	    }
	}
    }
  else
    {
      /* the dico is empty */
      return -1;
    }
}

int
parse_IntList4 (char *dico, int val[])
{
  return parse_array_int (4, dico, val);
}
