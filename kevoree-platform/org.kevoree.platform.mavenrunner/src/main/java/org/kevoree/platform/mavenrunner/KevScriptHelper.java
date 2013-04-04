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

package org.kevoree.platform.mavenrunner;

import org.apache.maven.project.MavenProject;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.tools.marShell.KevScriptOfflineEngine;
import org.kevoree.tools.modelsync.FakeBootstraperService;

import java.io.*;
import java.util.Enumeration;

class KevScriptHelper {

    private static KevoreeFactory factory = new DefaultKevoreeFactory();

    public static ContainerRoot generate(File scriptFile, MavenProject mavenSource) throws IOException, KevScriptEngineException {
        KevScriptOfflineEngine kevEngine = new KevScriptOfflineEngine(factory.createContainerRoot(), new FakeBootstraperService().getBootstrap());
        kevEngine.addVariable("kevoree.version", factory.getVersion());
        Enumeration propEnum = mavenSource.getProperties().propertyNames();
        String propName = "project.version";
        String propVal = mavenSource.getVersion();
        kevEngine.addVariable(propName.toString(), propVal.toString());
        while (propEnum.hasMoreElements()) {
            propName = propEnum.nextElement().toString();
            propVal = mavenSource.getProperties().get(propName).toString();
            kevEngine.addVariable(propName.toString(), propVal.toString());
        }
        kevEngine.addVariable("basedir", mavenSource.getBasedir().getAbsolutePath());

        BufferedReader br = new BufferedReader(new FileReader(scriptFile));
        String line = br.readLine();
        while (line != null) {
            kevEngine.append(line);
            line = br.readLine();
        }
        return kevEngine.interpret();
    }
}