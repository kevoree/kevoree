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
float eca_get_input_value(unsigned char i)
{
	return eca_inputs_values[i];
}

void eca_fire_rule(unsigned char i)
{
	unsigned char j=0;
    boolean condition = true;

    #ifdef DEBUG
	 //   Serial.print("fire rule #");
     	//Serial.println(i);
    #endif

     while(j<num_rule_predicate[i] && condition== true)
     {
		switch(eca_rules[i].eca_predicate[j].operation)
		{
		case EQUALS :

			if(eca_get_input_value(eca_rules[i].eca_predicate[j].id) == eca_rules[i].eca_predicate[j].value)
			{
			    /*
				Serial.println(eca_rules[i].eca_predicate[j].value);
			    Serial.print(eca_get_input_value(eca_rules[i].eca_predicate[j].id));
		    	Serial.print("=");       */
				(*eca_actions[i])();
			}else { condition = false;}
			break;

		case HIGHER :

			if(eca_get_input_value(eca_rules[i].eca_predicate[j].id) > eca_rules[i].eca_predicate[j].value)
			{
				/*	    Serial.print(eca_get_input_value(eca_rules[i].eca_predicate[j].id));
			Serial.print(">");
			Serial.println(eca_rules[i].eca_predicate[j].value);     */
				(*eca_actions[i])();
				}else { condition = false;}

			break;

		case LESS :

			if(eca_get_input_value(eca_rules[i].eca_predicate[j].id) < eca_rules[i].eca_predicate[j].value)
			{
				/*	    Serial.print(eca_get_input_value(eca_rules[i].eca_predicate[j].id));
			Serial.print("<");
			Serial.println(eca_rules[i].eca_predicate[j].value);     */
				(*eca_actions[i])();
					}else { condition = false;}

			break;
		default:
		    #ifdef DEBUG
		    	Serial.println("ERROR");
	         #endif
			break;
		}
		j++;
	}
}

void eca_fire_all(){
	unsigned int i=0;
	for(i=0;i< ECA_NUM_RULES;i++)
	{
		eca_fire_rule(i);
	}
}

