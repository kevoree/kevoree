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
const unsigned char	num_rule_predicate[ECA_NUM_RULES] = {<_num_rule_predicate>};
const struct _ECA_Rule eca_rules[ECA_NUM_RULES] = {
	<eca_rules>
};


void (*eca_actions[ECA_NUM_ACTIONS])(void) = {<ECA_ACTIONS> };


void setup_eca()
{
       <ECA_SETUP>
}