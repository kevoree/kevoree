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
import org.kevoree.annotation.Port;
import org.kevoree.framework.template.MicroTemplate;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;
import org.kevoree.tools.arduino.framework.fuzzylogic.fuzzy.GeneratorHelper;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/01/12
 * Time: 10:20
 */
@ComponentFragment
public abstract class AbstractFuzzyLogicArduinoComponent extends AbstractArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.appendNativeStatement(MicroTemplate.fromClassPath("fuzzylogic/ArduinoFuzzyLogic/ArduinoFuzzyLogicHeader.c",AbstractFuzzyLogicArduinoComponent.class));
    }

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        DefaultFuzzyRulesContext def = new DefaultFuzzyRulesContext();
        declareRules(def);

        //GENERATE TEMP TAB
        GeneratorHelper.generateClassVariables(gen,def.getNumberOfRules());


        gen.appendNativeStatement(MicroTemplate.fromClassPath("fuzzylogic/ArduinoFuzzyLogic/ArduinoFuzzyLogicFramework.c",AbstractFuzzyLogicArduinoComponent.class));
    }

    //TODO UPDATE

    private ArduinoGenerator lastUsed = null;

    @Port(name="*")
    public void portGeneration(Object o){
        if(lastUsed != null && getGenerator() != lastUsed){
            //TODO MAPPING & CODE GENRATION
            
            
            lastUsed = getGenerator(); 
        }
    }

    public abstract void declareRules(FuzzyRulesContext rulesContext);

}
