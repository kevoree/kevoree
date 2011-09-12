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

import com.explodingpixels.macwidgets.HudWindow;
import com.explodingpixels.widgets.WindowUtils;
import org.kevoree.platform.osgi.standalone.EmbeddedActivators;
import org.kevoree.platform.osgi.standalone.EmbeddedFelix;
import org.osgi.framework.BundleActivator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        try {
            File cacheFolder = createTempDirectory();
            cacheFolder.deleteOnExit();
            System.setProperty("osgi.base", cacheFolder.getAbsolutePath());
        } catch (IOException io) {
            io.printStackTrace();
        }

        final JFrame frame = new KevoreeGUIFrame();
        //WindowUtils.makeWindowNonOpaque(frame);
        frame.setBackground(Color.BLACK);
        //frame.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);

        EmbeddedActivators.setActivators(Arrays.asList(
                //      (BundleActivator) new org.ops4j.pax.url.mvn.internal.Activator(),
                (BundleActivator) new org.apache.felix.shell.impl.Activator(),
                (BundleActivator) new ConsoleActivator(),
                (BundleActivator) new org.ops4j.pax.url.assembly.internal.Activator(),
                (BundleActivator) new org.kevoree.platform.osgi.standalone.BootstrapActivator()
        ));

        final EmbeddedFelix felix = new EmbeddedFelix();
        felix.run();


        /*
        felix.run();

        try {
            felix.getM_fwk().waitForStop(0);

        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);      */


        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                try {
                    felix.getM_fwk().stop();

                    frame.setVisible(false);
                    frame.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);


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