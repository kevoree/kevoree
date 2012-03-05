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
  /*
	int rule_index,i,y;
    unsigned char in_domain,in_term,out_domain,out_term;
	for( rule_index=0;rule_index <numberOfRules;rule_index++)
	{
        Serial.print("#");
        Serial.print(rule_index);
        Serial.print("IF");

		for( i = 0;i < pgm_read_byte_near(num_rule_antecedent+rule_index);i++)
		{
			in_domain =  pgm_read_word(&rules[rule_index].antecedent[i].domain);
			in_term =  pgm_read_word(&rules[rule_index].antecedent[i].term);

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
			out_domain =  pgm_read_word(&rules[rule_index].consequent[y].domain);
			out_term =  pgm_read_word(&rules[rule_index].consequent[y].term);

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
	*/
}



void updateInMemberShipFunction(int numDomain,int numterm,int numpoint,int newvalue)
{

	inMemberShipFunction[numDomain][numterm][numpoint] = newvalue;
}



void updateoutMemberShipFunction(int numDomain,int numterm,int newvalue)
{

	outMemberShipFunction[numDomain][numterm][0] = newvalue;
}



void cleanArraysFunctions()
{
    int i;
    for(i=0;i<NUM_INPUTS;i++)
    {
             in_num_MemberShipFunction[i]  = 0;
    }

    for(i=0;i<NUM_OUTPUTS;i++){
              out_num_MemberShipFunction[i]  = 0;
    }

}

void setDictionnary(int type,int numDomain,int numTerm,int *val ){
    int i=0;
    if(type == 0)
    {
        for(i=0;i<NB_TERMS;i++)
        {
                updateInMemberShipFunction(numDomain,numTerm,i,val[i]);
        }
    }else
    {
           updateoutMemberShipFunction(numDomain,numTerm,*val);
    }
}


void displayInputs()
{
    int num_out;
      for( num_out=0;num_out <NUM_INPUTS;num_out++)
     	{
         Serial.print("I=");
         Serial.print(num_out);
         Serial.println(crisp_inputs[num_out]);
   	}

}


void displayOutputs()
{
    int num_out;
      for( num_out=0;num_out <NUM_OUTPUTS;num_out++)
     	{
    //     Serial.print("O=");
         Serial.print(num_out);
         Serial.println(crisp_outputs[num_out]);
   	}

}

void displayDomains()
{
   int num_out,j,y;
   for( num_out=0;num_out <NUM_INPUTS;num_out++)
	{
	    //  Serial.print("D_I");
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
	    //  Serial.print("D_O");
	       Serial.println(num_out);
	   for(j=0;j<out_num_MemberShipFunction[num_out];j++)
	   {
	              Serial.print(outMemberShipFunction[num_out][j][0]);
	               Serial.println(" ");
	}
	    Serial.println(" ");
  }

}



void fuzzify(unsigned char in_index,int in_val)
{
	unsigned char i;
	for (i = 0;i < in_num_MemberShipFunction[in_index];i++)
	{
		fuzzy_inputs[in_index][i] = get_membership(in_index,i,in_val);
	}
}

int get_membership(unsigned char in_index,unsigned char mf_index,int in_val)
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

	int   crispvalue = 1;

	for(i = 0;i < pgm_read_byte_near(num_rule_antecedent +rule_index);i++)
	{
		in_domain =  pgm_read_word(&rules[rule_index].antecedent[i].domain);
        in_term =  pgm_read_word(&rules[rule_index].antecedent[i].term);
		crispvalue = MIN(crispvalue,fuzzy_inputs[in_domain][in_term]);
	}
	rule_crispvalue[rule_index] = crispvalue;

	for (y = 0;y <  pgm_read_byte_near(num_rule_coutcome+rule_index);y++)
	{
		out_domain =  pgm_read_word(&rules[rule_index].consequent[y].domain);
		out_term =  pgm_read_word(&rules[rule_index].consequent[y].term);

		fuzzy_outputs[out_domain][out_term] = MAX(fuzzy_outputs[out_domain][out_term],rule_crispvalue[rule_index]);
	}
}

int defuzzify(unsigned char out_index,int *inputs)
{
	int           summ = 0;
	int           product = 0;
	int           temp1,temp2;
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
