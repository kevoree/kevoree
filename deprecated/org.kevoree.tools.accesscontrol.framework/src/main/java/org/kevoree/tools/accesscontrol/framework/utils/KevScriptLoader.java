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
package org.kevoree.tools.accesscontrol.framework.utils;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.tools.marShell.KevScriptOfflineEngine;
import org.kevoree.tools.modelsync.FakeBootstraperService;


/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class KevScriptLoader  {

    /**
     * Loading kevScript in ContainerRoot from file
     *
     * @param path_file
     * @return
     */
    public static ContainerRoot getModel(String path_file) throws KevScriptEngineException {

        KevoreeFactory kevoreeFactory = new DefaultKevoreeFactory();
        ContainerRoot basemodel = kevoreeFactory.createContainerRoot();
        byte [] file = FileManager.load(path_file);
        String kevScript = new String(file);
        FakeBootstraperService bootstraper = new FakeBootstraperService();
        KevScriptOfflineEngine kevOfflineEngine = new KevScriptOfflineEngine(basemodel,bootstraper.getBootstrap()) ;
        for( String pro : System.getProperties().stringPropertyNames())
        {
            kevOfflineEngine.addVariable(pro,System.getProperty(pro));
        }
        kevOfflineEngine.addVariable("kevoree.version", kevoreeFactory.getVersion());


        // add MessagePort
      //  String kev_framework ="merge 'mvn:org.kevoree.corelibrary.android/org.kevoree.library.android.logger/{kevoree.version}'";

        kevOfflineEngine.append("{"+kevScript.replace("tblock","")+"}") ;

        ContainerRoot model = kevOfflineEngine.interpret();

        return model;
    }




}
