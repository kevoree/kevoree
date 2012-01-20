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
unsigned char	in_num_MemberShipFunction[NUM_INPUTS] = { <_innum_MemberShipFunction> };
float inMemberShipFunction[NUM_INPUTS][NB_TERMS][PRECISION] =
{
		<_invaluesMemberShipFunction>
};
unsigned char	out_num_MemberShipFunction[NUM_INPUTS] = { <_outnum_MemberShipFunction> };
float	outMemberShipFunction[NUM_OUTPUTS][PRECISION][2] =
{
<_outvaluesMemberShipFunction>
};

float	crisp_inputs[NUM_OUTPUTS];    // values of inputs such as sensors
float	crisp_outputs[NUM_OUTPUTS];   // values after rules fire
// arrays use during the fire process of rules
float   fuzzy_outputs[NUM_OUTPUTS][NB_TERMS];
float   fuzzy_inputs[NUM_INPUTS][NB_TERMS];
float   rule_crispvalue[NUM_RULES];

