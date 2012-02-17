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
import org.kevoree.tools.arduino.framework.fuzzylogic.ParserFuzzyLogic;
import org.kevoree.tools.arduino.framework.fuzzylogic.fuzzy.ast.FuzzyRule;

import java.io.IOException;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 07/02/12
 * Time: 18:22
 */
public class Test {


    public static void main(String[] args) throws IOException
    {



ParserFuzzyLogic fuzzyDSL = new ParserFuzzyLogic();

        java.util.List<FuzzyRule> rules = fuzzyDSL.parseRules("" +
    "IF distance IS near THEN red IS high AND green IS low AND blue IS low AND intensity IS high;" +
    "IF distance IS med THEN red IS low AND green IS high AND blue IS low AND intensity IS high;" +
    "IF distance IS far THEN red IS low AND green IS low AND blue IS high AND intensity IS high;").rules();

        
        System.out.println(" rules : "+rules.size());
        }
}
