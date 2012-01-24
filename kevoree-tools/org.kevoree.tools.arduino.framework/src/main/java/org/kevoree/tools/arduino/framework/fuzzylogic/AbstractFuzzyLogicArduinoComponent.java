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
package org.kevoree.tools.arduino.framework.fuzzylogic;

import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.Generate;
import org.kevoree.annotation.Port;
import org.kevoree.framework.template.MicroTemplate;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;
import org.kevoree.tools.arduino.framework.fuzzylogic.fuzzy.GeneratorHelper;
import org.kevoree.tools.arduino.framework.fuzzylogic.fuzzy.ast.FuzzyRule;
import org.kevoree.tools.arduino.framework.fuzzylogic.fuzzy.ast.FuzzyRules;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 23/01/12
 *
 */
@ComponentFragment
public abstract class AbstractFuzzyLogicArduinoComponent extends AbstractArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen)
    {
        DefaultFuzzyRulesContext def = new DefaultFuzzyRulesContext();
        declareRules(def);

        gen.appendNativeStatement(MicroTemplate.fromClassPath("fuzzylogic/ArduinoFuzzyLogic/ArduinoFuzzyLogicHeader.c",AbstractFuzzyLogicArduinoComponent.class));

        // Generate rules
        generateRules(gen,def);

    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        DefaultFuzzyRulesContext def = new DefaultFuzzyRulesContext();
        declareRules(def);

        //GENERATE TEMP TAB
        GeneratorHelper.generateClassVariables(gen,def.getNumberOfRules());

        // add framework
        gen.appendNativeStatement(MicroTemplate.fromClassPath("fuzzylogic/ArduinoFuzzyLogic/ArduinoFuzzyLogicFramework.c",AbstractFuzzyLogicArduinoComponent.class));
    }

    //TODO UPDATE

    private ArduinoGenerator lastUsed = null;

    @Port(name="*")
    public void portGeneration(Object o)
    {
        if(lastUsed != null)
        {

            //TODO MAPPING & CODE GENRATION
            lastUsed = getGenerator();

            // fire all rules
            lastUsed.appendNativeStatement("control(crisp_inputs,crisp_outputs);");

        }
    }

    @Override
    public void generateUpdatedParams(ArduinoGenerator gen)
    {
        GeneratorHelper.generateUpdateDictonnary(gen);
    }

    public abstract void declareRules(FuzzyRulesContext rulesContext);




    public void generateRules(ArduinoGenerator gen,DefaultFuzzyRulesContext contextRules)
    {

        // TODO create one id by ruleset
        String uuid  = contextRules.getId().toString().substring(0,5);

        gen.appendNativeStatement("#define NUM_RULES "+contextRules.getNumberOfRules());

        StringBuilder code_rules = new StringBuilder();
        StringBuilder _num_rule_antecedent = new StringBuilder();
        StringBuilder _num_rule_coutcome = new StringBuilder();
        List<FuzzyRule> rules =  contextRules.getParsedRules();

        for(int i=0;i< rules.size();i++)
        {
            int numberAntecedent =    rules.get(i).getAntecedent().size();
            _num_rule_antecedent.append(numberAntecedent);

            if(i < rules.size()-1)
            {
                _num_rule_antecedent.append(",");
            }

            code_rules.append("{{");
            for(int j=0;j< numberAntecedent;j++)
            {
                String domain = rules.get(i).getAntecedent().get(j).getDomain();
                String term =   rules.get(i).getAntecedent().get(j).getTerm();

                int domain_position =  GeneratorHelper.getPositiongetProvided(gen,domain);
                int term_position =   GeneratorHelper.getPositionTerm(gen,domain,term);

                System.out.println("Rule #" + i + " Antecedent " + j + " " + domain + " " + domain_position + " " + term + " " + term_position + "");

                code_rules.append(domain_position+","+term_position);
                if(numberAntecedent > 0)
                    code_rules.append(",");
                //{ { 1,1},{ 1,1} },
            }
            code_rules.append("},{");
            int numberoutcome =    rules.get(i).getoutcome().size();
            _num_rule_coutcome.append(numberoutcome);
            if(i < rules.size()-1){

                _num_rule_coutcome.append(",");
            }

            for(int j=0;j<  numberoutcome;j++)
            {
                String domain = rules.get(i).getoutcome().get(j).getDomain();
                String term =   rules.get(i).getoutcome().get(j).getTerm();

                int domain_position =  GeneratorHelper.getPositiongetRequired(gen,domain);
                int term_position =    GeneratorHelper.getPositionTerm(gen,domain,term);

                code_rules.append(domain_position+","+term_position);
                if(numberoutcome > 0)
                    code_rules.append(",");

                System.out.println("Rule #" + i + " Consequence " + j + " " + domain + " " + domain_position + " " + term + " " + term_position + "");

            }
            code_rules.append("}}");

            if(i < rules.size()-1)
            {
                code_rules.append(",");
            }
        }

        gen.appendNativeStatement("PROGMEM const unsigned char	num_rule_antecedent[NUM_RULES] = { "+_num_rule_antecedent.toString()+"};");
        gen.appendNativeStatement("PROGMEM const unsigned char num_rule_coutcome[NUM_RULES] = { "+_num_rule_coutcome.toString()+"};")  ;
        gen.appendNativeStatement("const struct _Rule rules[NUM_RULES] = {");
        gen.appendNativeStatement(code_rules.toString());
        gen.appendNativeStatement( "};");

    }

}
