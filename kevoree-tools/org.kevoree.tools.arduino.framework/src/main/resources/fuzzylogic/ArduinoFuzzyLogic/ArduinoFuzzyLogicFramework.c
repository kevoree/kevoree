/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


void displayRules(){

	int rule_index,i,y,in_domain,in_term,out_domain,out_term;

	for( rule_index=0;rule_index <numberOfRules;rule_index++)
	{
        Serial.print("RULE #");
        Serial.print(rule_index);
        Serial.print(" IF ");

		for( i = 0;i < pgm_read_byte_near(num_rule_antecedent+rule_index);i++)
		{
			in_domain = rules[rule_index].antecedent[i].domain;
			in_term = rules[rule_index].antecedent[i].term;

            if(i >= 1) Serial.print(" AND ");
			Serial.print("[");
			Serial.print(in_domain);
			Serial.print("]");
			Serial.print("[");
			Serial.print(in_term);
			Serial.print("]");

		}

		Serial.print(" THEN ");
		for (y = 0;y <  pgm_read_byte_near(num_rule_coutcome+rule_index);y++)
		{
			out_domain = rules[rule_index].consequent[y].domain;
			out_term = rules[rule_index].consequent[y].term;

            if(y >= 1) Serial.print(" AND ");
			Serial.print("[");
			Serial.print(out_domain);
			Serial.print("]");
		    Serial.print("[");
			Serial.print(out_term);
			Serial.print("]");

		}
		 Serial.println(" END ");
	}
}



void updateInMemberShipFunction(int numDomain,int numterm,int numpoint,float newvalue)
{

	inMemberShipFunction[numDomain][numterm][numpoint] = newvalue;
}



void updateoutMemberShipFunction(int numDomain,int numterm,float newvalue)
{

	outMemberShipFunction[numDomain][numterm][0] = newvalue;
}

/*
char temp_cold[MAX_UNTYPED_DICTIONARY];
char temp_warn[MAX_UNTYPED_DICTIONARY];
char temp_hot[MAX_UNTYPED_DICTIONARY];
char fan_stop[MAX_UNTYPED_DICTIONARY];
char fan_slow[MAX_UNTYPED_DICTIONARY];
char fan_fast[MAX_UNTYPED_DICTIONARY];
*/

void cleanArraysFunctions()
{
    int i;
    for(i=0;i<NUM_INPUTS;i++){
            Serial.print("clean : ");
             Serial.println(i);
             in_num_MemberShipFunction[i]  = 0;
    }

    for(i=0;i<NUM_OUTPUTS;i++){
              out_num_MemberShipFunction[i]  = 0;
    }

}


 // -10,-10,-5,-5
void parseDictionnary(int type,int numDomain,int numTerm,char *name)
{
     int count=0,i=0,j=0;
     char parsing[MAXIMUM_SIZE_FLOAT];
   if((int)strlen(name) > 1)
   {


    if(type == 0)
    {
          Serial.print("Parsing INPUT ");
           Serial.print("[");
           Serial.print(numDomain);
           Serial.print("]");
           Serial.print(numTerm);
             Serial.print("] <");
               Serial.print(name);
                      Serial.println(">");
        j=0;
        for(i=0;i<(int)strlen(name);i++)
        {


             if(name[i] != ';' && name[i] != '\n')
             {
                if(j <MAXIMUM_SIZE_FLOAT)
                {
                       parsing[j] = name[i];
                       j++;
                }
                else
                {
                    strcpy(parsing,"0.0");
                }
             }
             else
             {
                  parsing[j] = '\n';

                  updateInMemberShipFunction(numDomain,numTerm,count,atof(parsing));
                  count++;

                  Serial.print("d=");
                  Serial.print(numDomain);
                  Serial.print("c=");
                  Serial.print(count);
                  Serial.print(" value=");
                  Serial.println(atof(parsing));
                  j=0;
             }
        }
    }
    else
    {
                 Serial.print("Parsing OUTPUT ");
                 Serial.print("[");
                 Serial.print(numDomain);
                 Serial.print("]");
                 Serial.print(numTerm);
                 Serial.print("] <");
                 Serial.print(name);
                 Serial.println(">");

         updateoutMemberShipFunction(numDomain,numTerm,atof(name));

     }
        }
}


void displayInputs()
{
    int num_out;
      for( num_out=0;num_out <NUM_INPUTS;num_out++)
     	{
         Serial.print("Input #");
         Serial.print(num_out);
         Serial.println(crisp_inputs[num_out]);
   	}

}


void displayOutputs()
{
    int num_out;
      for( num_out=0;num_out <NUM_OUTPUTS;num_out++)
     	{
         Serial.print("Output #");
         Serial.print(num_out);
         Serial.println(crisp_outputs[num_out]);
   	}

}

void displayDomains()
{
   int num_out,j,y;
   for( num_out=0;num_out <NUM_INPUTS;num_out++)
	{
	      Serial.print("DOMAIN IN #");
	      Serial.print(num_out);
	      Serial.print("<");
          Serial.print(in_num_MemberShipFunction[num_out]);
          Serial.println(">");

	   for(j=0;j<in_num_MemberShipFunction[num_out];j++)
	   {

	             for(y=0;y<PRECISION;y++)
	               {
	                Serial.print(",");
	              Serial.print(inMemberShipFunction[num_out][j][y]);
	             }
	              Serial.println(" ");
	}
  }

     for( num_out=0;num_out <NUM_OUTPUTS;num_out++)
	{
	      Serial.print("DOMAIN OUT #");
	       Serial.println(num_out);
	   for(j=0;j<out_num_MemberShipFunction[num_out];j++)
	   {
	              Serial.print(outMemberShipFunction[num_out][j][0]);
	               Serial.println(" ");
	}
	    Serial.println(" ");
  }

}



void fuzzify(unsigned char in_index,float in_val)
{
	unsigned char i;
	for (i = 0;i < in_num_MemberShipFunction[in_index];i++)
	{
		fuzzy_inputs[in_index][i] = get_membership(in_index,i,in_val);
	}
}

float get_membership(unsigned char in_index,unsigned char mf_index,float in_val)
{
	if (in_val < inMemberShipFunction[in_index][mf_index][0]) return 0;
	if (in_val > inMemberShipFunction[in_index][mf_index][3]) return 0;
	if (in_val <= inMemberShipFunction[in_index][mf_index][1])
	{
		if (inMemberShipFunction[in_index][mf_index][0] == inMemberShipFunction[in_index][mf_index][1])
			return 1;
		else
			return ((in_val - inMemberShipFunction[in_index][mf_index][0]) /
					(inMemberShipFunction[in_index][mf_index][1] - inMemberShipFunction[in_index][mf_index][0]));
	}
	if (in_val >= inMemberShipFunction[in_index][mf_index][2])
	{
		if (inMemberShipFunction[in_index][mf_index][2] == inMemberShipFunction[in_index][mf_index][3])
			return 1;
		else
			return ((inMemberShipFunction[in_index][mf_index][3] - in_val) /
					(inMemberShipFunction[in_index][mf_index][3] - inMemberShipFunction[in_index][mf_index][2]));
	}
	return 1;
}

void fire_rule(unsigned char rule_index)
{
	unsigned char  in_domain,in_term,out_domain,out_term,i,y;

	float   crispvalue = 1;

	for(i = 0;i < pgm_read_byte_near(num_rule_antecedent +rule_index);i++)
	{
		in_domain = rules[rule_index].antecedent[i].domain;
		in_term = rules[rule_index].antecedent[i].term;
		crispvalue = MIN(crispvalue,fuzzy_inputs[in_domain][in_term]);
	}
	rule_crispvalue[rule_index] = crispvalue;

	for (y = 0;y <  pgm_read_byte_near(num_rule_coutcome+rule_index);y++)
	{
		out_domain = rules[rule_index].consequent[y].domain;
		out_term = rules[rule_index].consequent[y].term;

		fuzzy_outputs[out_domain][out_term] = MAX(fuzzy_outputs[out_domain][out_term],rule_crispvalue[rule_index]);
	}
}

float defuzzify(unsigned char out_index,float *inputs)
{
	float           summ = 0;
	float           product = 0;
	float           temp1,temp2;
	unsigned char             i,in_index;
	for (i = 0;i < out_num_MemberShipFunction[out_index];i++)
	{
		temp1 = fuzzy_outputs[out_index][i];
		temp2 = outMemberShipFunction[out_index][i][0];
		summ = summ + temp1;
		product = product + (temp1 * temp2);
		fuzzy_outputs[out_index][i] = 0;
	}
	if (summ > 0)
	{
		crisp_outputs[out_index] = product / summ;
		return crisp_outputs[out_index];
	}
	else
	{
		return crisp_outputs[out_index];
	}
}
