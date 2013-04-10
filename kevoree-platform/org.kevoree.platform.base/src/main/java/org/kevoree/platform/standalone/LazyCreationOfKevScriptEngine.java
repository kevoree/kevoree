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
import org.kevoree.api.Bootstraper;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.kcl.KevoreeJarClassLoader;

import java.io.File;
import java.lang.reflect.Constructor;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/04/13
 * Time: 14:35
 */
public class LazyCreationOfKevScriptEngine implements KevScriptEngineFactory {

    private Bootstraper bootstraper = null;
    private KevoreeJarClassLoader aetherJCL = null;
    private String version = null;

    public LazyCreationOfKevScriptEngine(Bootstraper _b, KevoreeJarClassLoader _a, String _version) {
        bootstraper = _b;
        aetherJCL = _a;
        version = _version;
    }

    /* No injection */
    private KevoreeJarClassLoader scriptEngineKCL = null;
    private Class onlineMShellEngineClazz = null;
    private Class offLineMShellEngineClazz = null;
    private KevoreeCoreBean coreBean = null;
    private Constructor onlineCons = null;
    private Constructor offlineCons = null;

    private void checkOrInstall() throws NoSuchMethodException {
        if (scriptEngineKCL == null) {
            File fileMarShell = bootstraper.resolveKevoreeArtifact("org.kevoree.tools.marShell.pack", "org.kevoree.tools", version);
            scriptEngineKCL = new KevoreeJarClassLoader();
            scriptEngineKCL.addSubClassLoader(aetherJCL);
            scriptEngineKCL.add(fileMarShell.getAbsolutePath());
            scriptEngineKCL.lockLinks();
            onlineMShellEngineClazz = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptCoreEngine");
            offLineMShellEngineClazz = scriptEngineKCL.loadClass("org.kevoree.tools.marShell.KevScriptOfflineEngine");

            onlineCons =  onlineMShellEngineClazz.getDeclaredConstructor(KevoreeModelHandlerService.class, Bootstraper.class);
            offlineCons = offLineMShellEngineClazz.getDeclaredConstructor(ContainerRoot.class, Bootstraper.class);

        }
    }

    @Override
    public KevScriptEngine createKevScriptEngine() {
        try {
            checkOrInstall();
            return (KevScriptEngine) onlineCons.newInstance(coreBean, bootstraper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public KevScriptEngine createKevScriptEngine(ContainerRoot srcModel) {
        try {
            checkOrInstall();
            return (KevScriptEngine) offlineCons.newInstance(srcModel, bootstraper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
