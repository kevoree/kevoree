struct kmessage
{
  char *value;
  char *metric;
};
class KevoreeType
{
public:int subTypeCode;
  char instanceName[MAX_INST_ID];
};
//GENERATE kbinding framework
struct kbinding
{
  KevoreeType *instance;
  int portCode;
    QueueList < kmessage > *port;
};
#define BDYNSTEP 3
class kbindings
{
public:
  kbinding ** bindings;
  int nbBindings;
  void init ()
  {
    nbBindings = 0;
  }
  int addBinding (KevoreeType * instance, int portCode,
		  QueueList < kmessage > *port)
  {
    kbinding *newBinding = (kbinding *) malloc (sizeof (kbinding));
    if (newBinding)
      {
	memset (newBinding, 0, sizeof (kbinding));
      }
    newBinding->instance = instance;
    newBinding->portCode = portCode;
    newBinding->port = port;
    if (nbBindings % BDYNSTEP == 0)
      {
	bindings =
	  (kbinding **) realloc (bindings,
				 (nbBindings +
				  BDYNSTEP) * sizeof (kbinding *));
      }
    bindings[nbBindings] = newBinding;
    nbBindings++;
    return nbBindings - 1;
  }
  boolean removeBinding (KevoreeType * instance, int portCode)
  {
    //SEARCH INDEX 
    int indexToRemove = -1;
    for (int i = 0; i < nbBindings; i++)
      {
	kbinding *binding = bindings[i];
	if ((strcmp (binding->instance->instanceName, instance->instanceName)
	     == 0) && binding->portCode == portCode)
	  {
	    indexToRemove = i;
	  }
      }
    if (indexToRemove == -1)
      {
	return -1;
      }
    else
      {
	free (bindings[indexToRemove]);
      }
    if (indexToRemove != nbBindings - 1)
      {
	bindings[indexToRemove] = bindings[nbBindings - 1];
      }
    if (nbBindings % BDYNSTEP == 0)
      {
	bindings =
	  (kbinding **) realloc (bindings,
				 (nbBindings) * sizeof (kbinding *));
      }
    nbBindings--;
    return true;
  }
  void destroy ()
  {
    for (int i = 0; i < nbBindings; i++)
      {
	free (bindings[i]);
      }
    free (bindings);
  }
};







//GENERATE GLOBAL VARIABLE
KevoreeType **instances;	//GLOBAL INSTANCE DYNAMIC ARRAY 
int nbInstances = 0;		//GLOBAL NB INSTANCE 
KevoreeType *tempInstance;	//TEMP INSTANCE POINTER 
//GENERATE ADD INSTANCE HELPER
int
addInstance ()
{				//TECHNICAL HELPER ADD INSTANCE 
  if (tempInstance)
    {
      KevoreeType **newInstances =
	(KevoreeType **) malloc ((nbInstances + 1) * sizeof (KevoreeType *));
      if (!newInstances)
	{
	  return -1;
	}
      for (int idx = 0; idx < nbInstances; idx++)
	{
	  newInstances[idx] = instances[idx];
	}
      newInstances[nbInstances] = tempInstance;
      if (instances)
	{
	  free (instances);
	}
      instances = newInstances;
      tempInstance = NULL;
      nbInstances++;
      return nbInstances - 1;
    }
  return -1;
}

//GENERATE REMOVE INSTANCE HELPER
boolean
removeInstance (int index)
{
  destroyInstance (index);
  KevoreeType **newInstances =
    (KevoreeType **) malloc ((nbInstances - 1) * sizeof (KevoreeType *));
  if (!newInstances)
    {
      return false;
    }
  for (int idx = 0; idx < nbInstances; idx++)
    {
      if (idx < index)
	{
	  newInstances[idx] = instances[idx];
	}
      if (idx > index)
	{
	  newInstances[idx - 1] = instances[idx];
	}
    }
  if (instances)
    {
      free (instances);
    }
  instances = newInstances;
  nbInstances--;
  return true;
}


const char delimsEQ[] = "=";
char *key;
char *val;
byte paramCode;
void
updateParams (int index, char *params)
{
  if ((params[0] == '\0') && (params[1] != '='))
    {
      return;
    }
  paramCode = params[0];
  int i = 2;
  while (params[i] != '\0' && params[i] != ',')
    {
      i++;
    }
  if (params[i] == ',')
    {
      params[i] = '\0';
      updateParam (index, instances[index]->subTypeCode, paramCode,
		   &params[2]);
      updateParams (index, &params[i + 1]);	//recursive call
    }
  else
    {
      updateParam (index, instances[index]->subTypeCode, paramCode,
		   &params[2]);
    }
}


/*
   3 byte checksum
 */
char *
checksumArduino (char *buffer)
{
  static char tBuf[4];
  long index;
  unsigned int checksum;
  for (index = 0L, checksum = 0; index < strlen (buffer);
       checksum += (unsigned int) buffer[index++]);
  sprintf (tBuf, "%03d", (unsigned int) (checksum % 256));
  return (tBuf);
}

/*
  print a string from the flash
*/
void
printStringF (const prog_char str[])
{
  char c;
  if (!str)
    return;
  while ((c = pgm_read_byte (str++)))
    {
      Serial.print (c);
    }
}
