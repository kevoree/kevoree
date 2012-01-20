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
typedef struct _Predicate {
	unsigned char domain;
	unsigned char term;
}Predicate;

typedef struct _Rule {
	Predicate             antecedent[NB_TERMS];
	Predicate             consequent[NB_TERMS];
}Rule;


PROGMEM const unsigned char	num_rule_antecedent[NUM_RULES] = { <num_rule_antecedent>};
PROGMEM const unsigned char	num_rule_coutcome[NUM_RULES] = { <num_rule_coutcome>};

const struct _Rule rules[NUM_RULES] = {
 <loadrules>

};
