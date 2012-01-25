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

import ast.FuzzyRules
import org.kevoree.tools.arduino.framework.ArduinoGenerator
import org.kevoree.ComponentType
import java.util.HashMap
import org.kevoree.framework.template.MicroTemplate
import org.kevoree.tools.arduino.framework.fuzzylogic.{DefaultFuzzyRulesContext, AbstractFuzzyLogicArduinoComponent}
import org.kevoree.tools.arduino.framework.fuzzylogic.gen.utils.ArduinoException
import org.kevoree.annotation.PortType


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 24/01/12
 *
 */
object GeneratorHelper {



  def generateUpdateDictonnary(gen : ArduinoGenerator) : Unit = {

    gen.getTypeModel.getDictionaryType.get.getAttributes.foreach{ p =>
      val chaine = p.getName.split("_")
      gen.getTypeModel.asInstanceOf[ComponentType].getProvided.exists(d => d.getName == chaine(0)) match {

        /// INPUTS FUZZY TERMS
        case true  =>  gen.appendNativeStatement("parseDictionnary(0,"+getPositiongetProvided(gen, chaine(0))+","+getPositionTerm(gen, chaine(0),chaine(1))+","+p.getName+");")

       // OUTPUTS FUZZY TERMS
        case false =>   gen.appendNativeStatement("parseDictionnary(1,"+getPositiongetRequired(gen, chaine(0))+","+getPositionTerm(gen, chaine(0),chaine(1))+","+p.getName+");")
      }
    }
  }

  def getPositiongetProvided(gen : ArduinoGenerator,name : String) :Int ={
    var count = 0
    gen.getTypeModel.asInstanceOf[ComponentType].getProvided.foreach( p =>
      if(p.getName ==  name)
      {
        return  count
      }
      else
      {
        count = count +1
      }
    )
    throw  new ArduinoException("The domain "+name+" is not found :  "+gen.getTypeModel.asInstanceOf[ComponentType].getProvided )
  }

  def getPositiongetRequired(gen : ArduinoGenerator,name : String) :Int ={
    var count = 0
    gen.getTypeModel.asInstanceOf[ComponentType].getRequired.foreach( p =>
      if(p.getName ==  name)
      {
        return  count
      }
      else
      {
        count = count +1
      }
    )
    throw  new ArduinoException("The domain "+name+" is not found :  "+    gen.getTypeModel.asInstanceOf[ComponentType].getRequired)
  }

  def getPositionTerm(gen : ArduinoGenerator,domain : String, term : String) :Int ={
    var count = 0
    gen.getTypeModel.getDictionaryType.get.getAttributes.foreach( p =>
      if(p.getName == (domain+"_"+term))
      {
        return  count
      }
      else
      {
        if(p.getName.startsWith(domain))
        {
          count = count +1
        }
      }
    )
    throw  new ArduinoException("The term "+domain+"_"+term+" is not found  : "+gen.getTypeModel.getDictionaryType.get.getAttributes)
  }

  def generateClassVariables(gen : ArduinoGenerator, nbRules : Int){

    val nbInputs = gen.getTypeModel.asInstanceOf[ComponentType].getProvided.size
    val nbOutputs = gen.getTypeModel.asInstanceOf[ComponentType].getRequired.size

    gen.appendNativeStatement("#define NUM_INPUTS "+nbInputs)
    gen.appendNativeStatement("#define NUM_OUTPUTS "+nbOutputs)


    gen.appendNativeStatement("float crisp_inputs["+nbInputs+"];")
    gen.appendNativeStatement("float crisp_outputs["+nbInputs+"];")


    gen.appendNativeStatement("float fuzzy_outputs["+nbOutputs+"][NB_TERMS];")
    gen.appendNativeStatement("float fuzzy_inputs["+nbInputs+"][NB_TERMS];")
    gen.appendNativeStatement("float rule_crispvalue["+nbRules+"];")

    gen.appendNativeStatement("unsigned char in_num_MemberShipFunction["+nbInputs+"];")
    gen.appendNativeStatement("unsigned char out_num_MemberShipFunction["+nbInputs+"];")
    gen.appendNativeStatement("float outMemberShipFunction["+nbOutputs+"][PRECISION][2];")
    gen.appendNativeStatement("float inMemberShipFunction["+nbInputs+"][NB_TERMS][PRECISION];")

    // map global variables to local constante
    gen.appendNativeStatement("unsigned char const	*num_rule_antecedent;");
    gen.appendNativeStatement("unsigned char const	*num_rule_coutcome;");
    gen.appendNativeStatement("_Rule const *rules;")
    gen.appendNativeStatement("unsigned char numberOfRules;")

  }



}