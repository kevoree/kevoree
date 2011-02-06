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

import org.kevoree.platform.osgi.standalone.EmbeddedActivators;
import org.kevoree.platform.osgi.standalone.EmbeddedFelix;
import org.osgi.framework.BundleActivator;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        new KevoreeGUIFrame();

        EmbeddedActivators.setActivators(Arrays.asList(
                (BundleActivator) new org.ops4j.pax.url.mvn.internal.Activator(),
                (BundleActivator) new org.apache.felix.shell.impl.Activator(),
                //(BundleActivator) new org.apache.felix.shell.tui.Activator(),
                (BundleActivator) new ConsoleActivator(),
                (BundleActivator) new org.ops4j.pax.url.assembly.internal.Activator(),
                (BundleActivator) new org.kevoree.platform.osgi.standalone.BootstrapActivator()
        ));

        EmbeddedFelix felix = new EmbeddedFelix();
        felix.run();

        try {
            felix.getM_fwk().waitForStop(0);

        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);

    }
}