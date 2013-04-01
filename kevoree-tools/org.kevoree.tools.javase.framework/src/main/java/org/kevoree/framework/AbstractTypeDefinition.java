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
package org.kevoree.framework;

import org.kevoree.annotation.KevoreeInject;
import org.kevoree.api.Bootstraper;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 05/10/12
 * Time: 11:04
 */
public class AbstractTypeDefinition {

    private HashMap<String, Object> dictionary = new HashMap<String, Object>();

    public HashMap<String, Object> getDictionary () {
        return this.dictionary;
    }

    public void setDictionary (HashMap<String, Object> dic) {
        dictionary = dic;
    }

    @KevoreeInject
    public KevoreeModelHandlerService modelServiceProxy;

    public void setModelService(KevoreeModelHandlerService ms) {
        modelServiceProxy = new ModelHandlerServiceProxy(ms);
    }

    public KevoreeModelHandlerService getModelService() {
        return modelServiceProxy;
    }

    @KevoreeInject
    public KevScriptEngineFactory kevScriptEngineFactory = null;

    public KevScriptEngineFactory getKevScriptEngineFactory () {
        return kevScriptEngineFactory;
    }

    public void setKevScriptEngineFactory (KevScriptEngineFactory kf) {
        kevScriptEngineFactory = kf;
    }

    @KevoreeInject
    public Bootstraper bootstrapService = null;

    public void setBootStrapperService(Bootstraper brs) {
        bootstrapService = brs;
    }

    public Bootstraper getBootStrapperService() {
        return bootstrapService;
    }


    private String nodeName = "";

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String pnodeName) {
        nodeName = pnodeName;
    }

    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String pname) {
        name = pname;
    }


}
