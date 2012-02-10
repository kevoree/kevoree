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
import org.kevoree.tools.arduino.framework.fuzzylogic.gen.utils.ArduinoException
import org.kevoree.ComponentType


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 24/01/12
 *
 */
object GeneratorHelper {


  def generateUpdateDictonnary(gen: ArduinoGenerator): Unit = {

    gen.getTypeModel.getDictionaryType.get.getAttributes.foreach {
      p =>
        val chaine = p.getName.split("_")
        gen.getTypeModel.asInstanceOf[ComponentType].getProvided.exists(d => d.getName == chaine(0)) match {

          /// INPUTS FUZZY TERMS
          case true => gen.appendNativeStatement("parseDictionnary(0," + getPositiongetProvided(gen, chaine(0)) + "," + getPositionTerm(gen, chaine(0), chaine(1)) + "," + p.getName + ");")

          // OUTPUTS FUZZY TERMS
          case false => gen.appendNativeStatement("parseDictionnary(1," + getPositiongetRequired(gen, chaine(0)) + "," + getPositionTerm(gen, chaine(0), chaine(1)) + "," + p.getName + ");")
        }
    }
  }

  def getPositiongetProvided(gen: ArduinoGenerator, name: String): Int = {
    var count = 0
    gen.getTypeModel.asInstanceOf[ComponentType].getProvided.foreach(p =>
      if (p.getName == name) {
        return count
      }
      else {
        count = count + 1
      }
    )
    throw new ArduinoException("The domain " + name + " is not found :  " + gen.getTypeModel.asInstanceOf[ComponentType].getProvided)
  }

  def getPositiongetRequired(gen: ArduinoGenerator, name: String): Int = {
    var count = 0
    gen.getTypeModel.asInstanceOf[ComponentType].getRequired.foreach(p =>
      if (p.getName == name) {
        return count
      }
      else {
        count = count + 1
      }
    )
    throw new ArduinoException("The domain " + name + " is not found :  " + gen.getTypeModel.asInstanceOf[ComponentType].getRequired)
  }

  def getPositionTerm(gen: ArduinoGenerator, domain: String, term: String): Int = {
    var count = 0
    gen.getTypeModel.getDictionaryType.get.getAttributes.foreach(p =>
      if (p.getName == (domain + "_" + term)) {
        return count
      }
      else {
        if (p.getName.startsWith(domain)) {
          count = count + 1
        }
      }
    )
    throw new ArduinoException("The term " + domain + "_" + term + " is not found  : " + gen.getTypeModel.getDictionaryType.get.getAttributes)
  }


  def generateMemberShipVariables(gen: ArduinoGenerator){
    var countDomain= 0
    var countTerm = 0
    gen.getTypeModel.asInstanceOf[ComponentType].getProvided.foreach{ g =>
      gen.getTypeModel.getDictionaryType.get.getAttributes.foreach{a =>
        if(a.getName.split("_")(0) == g.getName)
        {
          countTerm = countTerm +1
        }
      }
      gen.appendNativeStatement("in_num_MemberShipFunction["+countDomain+"]="+countTerm+";")
      countDomain = countDomain +1
    }
    countDomain= 0
    countTerm = 0
    gen.getTypeModel.asInstanceOf[ComponentType].getRequired.foreach{ g =>
      gen.getTypeModel.getDictionaryType.get.getAttributes.foreach{a =>
        if(a.getName.split("_")(0) == g.getName)
        {
          countTerm = countTerm +1
        }
      }
      gen.appendNativeStatement("out_num_MemberShipFunction["+countDomain+"]="+countTerm+";")
      countDomain = countDomain +1
    }
  }
  def generateClassVariables(gen: ArduinoGenerator, nbRules: Int) {

    val nbInputs = gen.getTypeModel.asInstanceOf[ComponentType].getProvided.size
    val nbOutputs = gen.getTypeModel.asInstanceOf[ComponentType].getRequired.size

    gen.appendNativeStatement("#define NUM_INPUTS " + nbInputs)
    gen.appendNativeStatement("#define NUM_OUTPUTS " + nbOutputs)


    gen.appendNativeStatement("float crisp_inputs[" + nbInputs + "];")
    gen.appendNativeStatement("float crisp_outputs[" + nbOutputs + "];")


    gen.appendNativeStatement("float fuzzy_outputs[" + nbOutputs + "][NB_TERMS];")
    gen.appendNativeStatement("float fuzzy_inputs[" + nbInputs + "][NB_TERMS];")
    gen.appendNativeStatement("float rule_crispvalue[" + nbRules + "];")

    gen.appendNativeStatement("int in_num_MemberShipFunction[" + nbInputs + "];")
    gen.appendNativeStatement("int out_num_MemberShipFunction[" + nbInputs + "];")
    gen.appendNativeStatement("float outMemberShipFunction[" + nbOutputs + "][PRECISION][2];")
    gen.appendNativeStatement("float inMemberShipFunction[" + nbInputs + "][NB_TERMS][PRECISION];")

    // map global variables to local constante
    gen.appendNativeStatement("const unsigned char *num_rule_antecedent;");
    gen.appendNativeStatement("const unsigned char *num_rule_coutcome;");
    gen.appendNativeStatement("_Rule const *rules;")
    gen.appendNativeStatement("unsigned char numberOfRules;")

  }

  def generateControlMethod(gen: ArduinoGenerator) {

    gen.appendNativeStatement("void fire_all_rules(){control(); }")

    gen.appendNativeStatement("void control(){")

    gen.appendNativeStatement("unsigned char  in_index,rule_index,out_index;")
    gen.appendNativeStatement("float   in_val;")
    gen.appendNativeStatement("for (in_index = 0;in_index < NUM_INPUTS;in_index++){fuzzify(in_index,crisp_inputs[in_index]);}")
    gen.appendNativeStatement("for (rule_index = 0;rule_index < numberOfRules;rule_index++){fire_rule(rule_index);}")

    gen.appendNativeStatement("for (out_index = 0;out_index < NUM_OUTPUTS;out_index++){")

    gen.appendNativeStatement("float previousVal = crisp_outputs[out_index];");
    gen.appendNativeStatement("int currentResult = defuzzify(out_index, crisp_inputs);")
    gen.appendNativeStatement("if(previousVal != currentResult){")
    gen.declareStaticKMessage("temp_kmsg", "")
    gen.appendNativeStatement("switch(out_index){")

    gen.getTypeModel.asInstanceOf[ComponentType].getRequired.foreach {
      req =>
        gen.appendNativeStatement("case " + getPositiongetRequired(gen, req.getName) + " : ")
        gen.appendNativeStatement("sprintf(buf_" + req.getName + ",\"%d\",currentResult);\n")

        gen.appendNativeStatement("temp_kmsg->value = buf_" + req.getName+";")
        gen.sendKMessage("temp_kmsg", req.getName)
        gen.appendNativeStatement("break;")
    }
    gen.appendNativeStatement("}")

    gen.freeStaticKMessage("temp_kmsg")

    gen.appendNativeStatement("}") // END IF
    gen.appendNativeStatement("}")//END FOR
    gen.appendNativeStatement("#ifdef DEBUG")
    gen.appendNativeStatement(" //displayRules();")
    gen.appendNativeStatement("// displayDomains();")
    gen.appendNativeStatement("// displayInputs();")
    gen.appendNativeStatement("//displayOutputs();")
    gen.appendNativeStatement("#endif")
    gen.appendNativeStatement("}")


  }


}