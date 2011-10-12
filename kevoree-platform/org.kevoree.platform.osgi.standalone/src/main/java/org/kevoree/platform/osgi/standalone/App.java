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
import org.kevoree.framework.KevoreeXmiHelper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App {

    public void start() {
                    File mavenDir = new File(System.getProperty("user.home") + "/.m2/repository");
        if (mavenDir.exists() && mavenDir.isDirectory()) {
            System.out.println("use mavenDir=file:///" + mavenDir.getAbsoluteFile().getAbsolutePath());
            System.setProperty("org.kevoree.remote.provisioning", "file:///"+mavenDir.getAbsolutePath());
        }

        EmbeddedFelix felix = new EmbeddedFelix();
        felix.run();
        try {
            felix.getM_fwk().waitForStop(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    public static void main(String[] args) {

        ConstantsHandler.setConstantValuesProvider(new ConstantValuesImpl());

        App app = new App();
        app.start();

    }
}
