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
package org.kevoree.tools.arduino.framework.fuzzylogic.fuzzy

import org.kevoree.tools.arduino.framework.ArduinoGenerator
import org.kevoree.ComponentType

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/01/12
 * Time: 10:43
 */

object GeneratorHelper {

  def generateClassVariables(gen : ArduinoGenerator, nbRules : Int){

    val nbInputs = gen.getTypeModel.asInstanceOf[ComponentType].getProvided.size
    val nbOutputs = gen.getTypeModel.asInstanceOf[ComponentType].getRequired.size

    gen.appendNativeStatement("float crisp_inputs["+nbInputs+"];")
    
    gen.appendNativeStatement("float fuzzy_outputs["+nbOutputs+"][NB_TERMS];")
    gen.appendNativeStatement("float fuzzy_inputs["+nbInputs+"][NB_TERMS];")
    gen.appendNativeStatement("float rule_crispvalue["+nbRules+"];")

    gen.appendNativeStatement("unsigned char    in_num_MemberShipFunction["+nbInputs+"];")
    gen.appendNativeStatement("unsigned char    out_num_MemberShipFunction["+nbInputs+"];")
    gen.appendNativeStatement("float    outMemberShipFunction["+nbOutputs+"][PRECISION][2];")
    gen.appendNativeStatement("float inMemberShipFunction["+nbInputs+"][NB_TERMS][PRECISION];")


  }


}