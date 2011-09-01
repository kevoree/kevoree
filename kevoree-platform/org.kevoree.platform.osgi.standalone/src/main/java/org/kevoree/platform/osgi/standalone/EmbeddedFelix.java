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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.platform.osgi.standalone;

import generated.SysPackageConstants;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author ffouquet
 */
public class EmbeddedFelix {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedFelix.class);

    private Felix m_fwk;

    public Felix getM_fwk() {
        return m_fwk;
    }

    public void run() {

        String felix_base = System.getProperty("osgi.base");
        if (felix_base == null) {
            felix_base = ".";//this.getClass().getClassLoader().getResource(".").getPath();
            logger.debug("Init Felix Default path => " + felix_base);
        }

        String node_name = System.getProperty("node.name");
        if (node_name == null || node_name.equals("")) {
            node_name = "KEVOREEDefaultNodeName";
			System.setProperty("node.name", node_name);
        }

        File cacheDir = new File(felix_base + "/" + "felixCache_" + node_name);
        Map<String, Object> configProps = new HashMap<String, Object>();
        if (cacheDir.exists()) {
         	cacheDir.mkdirs();
        }
		configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir.getAbsolutePath());


        /* Look for a free port for remote shell */
        //int remoteShellPort = AvailablePortFinder.getNextAvailable();

        //System.setProperty("osgi.shell.telnet.port", remoteShellPort + "");
        //System.out.println("Remote Shell Port => " + remoteShellPort);


        configProps.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, SysPackageConstants.getProperty() + ",sun.misc");
        configProps.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        configProps.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, EmbeddedActivators.getActivators());

        Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {

            public void run() {
                try {
                    logger.debug("Stopping OSGi Embedded Framework");
                    if (m_fwk != null) {
                        m_fwk.stop();
                        m_fwk.waitForStop(0);
                    }
                } catch (Exception ex) {
                    logger.warn("Error stopping framework: " + ex);
                }
            }
        });


        try {
            m_fwk = new Felix(configProps);
            m_fwk.init();
            // (10) Start the framework.
            m_fwk.start();

            printWelcome();

            logger.debug("Felix Embedded started");
            // (11) Wait for framework to stop to exit the VM.
            //m_fwk.waitForStop(0);
            //System.exit(0)
        } catch (Exception ex) {
//            System.err.println("Could not create framework: " + ex);
//            ex.printStackTrace();
			logger.error("Could not create framework: ", ex);
            System.exit(0);
        }

    }

    private void printWelcome() {
        String welcomeMessage = "Kevoree OSGi Standalone";

        InputStream is = getClass().getResourceAsStream("/META-INF/maven/org.kevoree.platform/org.kevoree.platform.osgi.standalone/pom.properties");
        if (is != null) {
            try {
                Properties p = new Properties();

                p.load(is);

                String version = p.getProperty("version");
                if (version != null) {
                    welcomeMessage += " - " + version;
                }

                is.close();

            } catch (IOException ex) {
                logger.error(null, ex);
            }

        }

        System.out.println(welcomeMessage);

    }
}
