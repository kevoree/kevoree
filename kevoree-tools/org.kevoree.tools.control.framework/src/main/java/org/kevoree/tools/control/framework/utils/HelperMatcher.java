/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.control.framework.utils;

import org.kevoree.KControlModel.KControlModelFactory;
import org.kevoree.KControlModel.RuleMatcher;
import org.kevoree.KControlModel.impl.DefaultKControlModelFactory;
import org.kevoree.kompare.JavaSePrimitive;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class HelperMatcher {

    private static  DefaultKControlModelFactory factory = new DefaultKControlModelFactory();


    public static RuleMatcher createMatcher(String pTypeQuery)
    {
        RuleMatcher m1 = factory.createRuleMatcher();
        m1.setPTypeQuery(pTypeQuery);
        return  m1;

    }
}
