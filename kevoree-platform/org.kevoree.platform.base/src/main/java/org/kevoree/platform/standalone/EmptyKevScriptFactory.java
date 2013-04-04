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
package org.kevoree.platform.standalone;

import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/04/13
 * Time: 01:38
 * To change this template use File | Settings | File Templates.
 */
public class EmptyKevScriptFactory implements KevScriptEngineFactory {
    @Override
    public KevScriptEngine createKevScriptEngine() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public KevScriptEngine createKevScriptEngine(ContainerRoot containerRoot) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
