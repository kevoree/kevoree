
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



int parse_array_int(int size,char *dico, int val[])
{
	#define MAX_SIZE_INT 10
	int count=0,i=0,j=0;
	char parsing[MAX_SIZE_INT];
	// init array val
	for(i=0;i<size;i++)
	{
		val[i] = 0;
	}
	dico[strlen(dico)] = '\n';
	if((int)strlen(dico) > 1)
	{
		for(i=0;i<(int)strlen(dico);i++)
		{
			if(dico[i] != ';' && dico[i] != '\n')
			{
				if(j <MAX_SIZE_INT)	{ parsing[j] = dico[i];	j++;}	else {strcpy(parsing,"0");}
			}
			else
			{
				parsing[j] = '\n';
				val[count] = atoi(parsing);
				memset(parsing, 0, sizeof(parsing));
				count++;
				if(count > size){		/* the dico is bigger */	return -2;	}
				j=0;
			}
		}
	}
	else
	{
		/* the dico is empty */
		return -1;
	}
}

/**
* Parsing type IntList4
* @param dico
* @param values parsed
* @return 0 in case of success
*/
int parse_IntList4(char *dico, int val[])
{
	return parse_array_int(4,dico,val);
}
