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
package org.kevoree.platform.standalone.gui;

import org.kevoree.ContainerRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 */
public class App extends org.kevoree.platform.standalone.App {

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public void start() {

        DefaultSystem.instance$.saveSystemFlux();
        /*
        try {
            File cacheFolder = createTempDirectory();
            cacheFolder.deleteOnExit();
            System.setProperty("osgi.base", cacheFolder.getAbsolutePath());
        } catch (IOException io) {
            io.printStackTrace();
        } */
        ContainerRoot model = null;


        /*Object param = System.getProperty("node.bootstrap");
        if (param != null) {
            model = KevoreeXmiHelper.instance$.load(param.toString());
        } else {
            try {
                System.setSecurityManager(null);

                KevoreeJarClassLoader temp_cl = new KevoreeJarClassLoader();
                temp_cl.add(KevoreeBootStrap.class.getClassLoader().getResourceAsStream("org.kevoree.tools.aether.framework-" + new DefaultKevoreeFactory().getVersion() + ".pack.jar"));

                Class clazz = temp_cl.loadClass("org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper");
                org.kevoree.api.Bootstraper bootstraper = (Bootstraper) clazz.newInstance();
                File fileMarShell = bootstraper.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap", "org.kevoree.corelibrary.model", new DefaultKevoreeFactory().getVersion());
                JarFile jar = new JarFile(fileMarShell);
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                model = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry));

                bootstraper.close();
                bootstraper = null;
                temp_cl.unload();
                temp_cl = null;


            } catch (Exception e) {
                logger.error("Error while bootstrap ", e);
            }
        }*/

        final KevoreeGUIFrame frame = new KevoreeGUIFrame(/*model*/);
    }

    public static void main(String[] args) throws Exception {
        if(System.getProperty("node.headless") != null && System.getProperty("node.headless").equals("true")){
            org.kevoree.platform.standalone.App.main(args);
        } else {
            App app = new App();
            app.initialize();
            app.start();
        }
    }


    public static File createTempDirectory()
            throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }
}