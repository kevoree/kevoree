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
package org.kevoree.platform.osgi.standalone.gui;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.extra.jcl.KevoreeJarClassLoader;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.MavenResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.jcl.JclObjectFactory;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Hello world!
 */
public class App {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected void start() {

        DefaultSystem.saveSystemFlux();

        // System.setProperty("actors.corePoolSize", "10");
        //  System.setProperty("actors.maxPoolSize", "256");
        //   System.setProperty("actors.enableForkJoin", "false");


        try {
            File cacheFolder = createTempDirectory();
            cacheFolder.deleteOnExit();
            System.setProperty("osgi.base", cacheFolder.getAbsolutePath());
        } catch (IOException io) {
            io.printStackTrace();
        }
        ContainerRoot model = null;


        Object param = System.getProperty("node.bootstrap");
        if (param != null) {
            model = KevoreeXmiHelper.load(param.toString());
        } else {
            try {
                System.setSecurityManager(null);
                
                KevoreeJarClassLoader temp_cl = new KevoreeJarClassLoader();
                temp_cl.add(App.class.getClassLoader().getResourceAsStream("boot/org.kevoree.tools.aether.framework-" + KevoreeFactory.getVersion() + ".jar"));
                JclObjectFactory factory = JclObjectFactory.getInstance();

                MavenResolver mres = (MavenResolver) factory.create(temp_cl, "org.kevoree.tools.aether.framework.AetherMavenResolver");
                File fileMarShell = mres.resolveKevoreeArtifact("org.kevoree.library.model.bootstrap", "org.kevoree.library.model", KevoreeFactory.getVersion());
                JarFile jar = new JarFile(fileMarShell);
                JarEntry entry = jar.getJarEntry("KEV-INF/lib.kev");
                model = KevoreeXmiHelper.loadStream(jar.getInputStream(entry));


      

            } catch (Exception e) {
                logger.error("Error while bootstrap ", e);
            }
        }

        final KevoreeGUIFrame frame = new KevoreeGUIFrame(model);
    }

    public static void main(String[] args) {

        // System.out.println(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        // System.setProperty("apple.awt.graphics.UseQuartz","true");

        GuiConstantsHandler.setGuiConstantValuesProvider(new GuiConstantValuesImpl());

        App app = new App();
        app.start();
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