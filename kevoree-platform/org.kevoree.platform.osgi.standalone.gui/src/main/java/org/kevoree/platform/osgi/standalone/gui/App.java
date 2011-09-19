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
import org.kevoree.ContainerRoot;
import org.kevoree.api.configuration.ConfigConstants;
import org.kevoree.framework.KevoreeXmiHelper;
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
        ContainerRoot model = null;

        //System.setProperty("node.bootstrap","/Users/ffouquet/Desktop/test.kev");

        Object param = System.getProperty("node.bootstrap");
        if(param != null ){
            model = KevoreeXmiHelper.load(param.toString());
        } else {
            model = KevoreeXmiHelper.loadStream(App.class.getClassLoader().getResourceAsStream("defaultLibrary.kev")) ;

        }
        final KevoreeGUIFrame frame = new KevoreeGUIFrame(model);
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