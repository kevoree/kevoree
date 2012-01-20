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
#include \<Time.h>
#include \<TimeAlarms.h>

#define ECA_NUM_RULES <ECA_NUM_RULES>
#define ECA_NUM_INPUTS <ECA_NUM_INPUTS>
#define ECA_NUM_ACTIONS <ECA_NUM_ACTIONS>
#define ECA_NUM_PREDICATE <ECA_NUM_PREDICATE>

#define HIGHER 0
#define LESS 1
#define EQUALS 2

#define AND 0
#define OR 1

static float eca_inputs_values[ECA_NUM_RULES];

float eca_get_input_value(unsigned char i);
void eca_fire_rule(unsigned char i);
void eca_fire_all();

typedef struct _eca {
	unsigned char id;
	unsigned char operation;
	float value;
}ECApredicate;

typedef struct _ECA_Rule {
	ECApredicate             eca_predicate[ECA_NUM_PREDICATE];
}ECA_Rule;

  void buzz(int targetPin, long frequency, long length) {
  long delayValue = 1000000/frequency/2; // calculate the delay value between transitions
  //// 1 second's worth of microseconds, divided by the frequency, then split in half since
  //// there are two phases to each cycle
  long numCycles = frequency * length/ 1000; // calculate the number of cycles for proper timing
  //// multiply frequency, which is really cycles per second, by the number of seconds to
  //// get the total number of cycles to produce
 for (long i=0; i \< numCycles; i++){ // for the calculated length of time...
    digitalWrite(targetPin,HIGH); // write the buzzer pin high to push out the diaphram
    delayMicroseconds(delayValue); // wait for the calculated delay value
    digitalWrite(targetPin,LOW); // write the buzzer pin low to pull back the diaphram
    delayMicroseconds(delayValue); // wait againf or the calculated delay value
  }
}

void TurnOnbeep(){
	buzz(4, 2500, 100); // buzz the buzzer on pin 4 at 2500Hz for 100 milliseconds
}

void TurnOnbeep1(){
	buzz(4, 2500, 2000); // buzz the buzzer on pin 4 at 2500Hz for 100 milliseconds
}


void TurnOffbeep(){

}


void turnOnLed(){
	Serial.println("turn on led");
	  digitalWrite(13, HIGH);

}

void turnOffLed(){
  digitalWrite(13, LOW);    // set the LED off

}

static unsigned char mouvementbool=0;


void checkMove(){
     if(rand()==1){
        mouvementbool++;
     }

}