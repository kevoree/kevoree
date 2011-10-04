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
package org.kevoree.platform.osgi.standalone;

import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.ScriptInterpreter;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.tools.marShell.ast.Script;
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext;
import org.kevoree.tools.marShell.interpreter.KevsScriptInterpreter;
import org.kevoree.tools.marShell.parser.KevsParser;
import scala.Option;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KevScriptInterpreterService implements ScriptInterpreter {

    KevoreeModelHandlerService handler = null;
    KevsParser parser = new KevsParser();

    public KevScriptInterpreterService(KevoreeModelHandlerService _handler) {
        handler = _handler;
    }

    @Override
    public Boolean interpret(String content) {

        Option<Script> script = parser.parseScript(content);

        if (script.isDefined()) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(outputStream,handler.getLastModel());

            ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray());
            ContainerRoot model = KevoreeXmiHelper.loadStream(input);

            //ContainerRoot model = EcoreUtil.copy(handler.getLastModel());

            KevsInterpreterContext context = new KevsInterpreterContext(model);
            KevsScriptInterpreter interpreter = new KevsScriptInterpreter((Script) script.get());
            boolean result = interpreter.interpret(context);
            if(result){
                handler.updateModel(model);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }
}
