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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.regex.PatternSyntaxException;

public class KevoreeGUIFrame extends JFrame {

    public static KevoreeGUIFrame singleton = null;

    public KevoreeGUIFrame() {
        singleton = this;
        this.setBackground(new Color(63, 128, 187));

        URL urlIcon = getClass().getClassLoader().getResource("kevoree-logo-full.png");
        URL urlSmallIcon = getClass().getClassLoader().getResource("kev-logo-full.png");
        ImageIcon topIIcon = new ImageIcon(urlIcon);
        ImageIcon smallIcon = new ImageIcon(urlSmallIcon);
        this.setIconImage(smallIcon.getImage());
        JLabel topImage = new JLabel(topIIcon);
        topImage.setOpaque(true);
        topImage.setBackground(new Color(63, 128, 187));
        this.add(topImage, BorderLayout.NORTH);
        


        File mavenDir = new File(System.getProperty("user.home") + "/.m2/repository");
        if (mavenDir.exists() && mavenDir.isDirectory()) {
            System.out.println("use mavenDir=file:///" + mavenDir.getAbsoluteFile().getAbsolutePath());
            System.setProperty("org.kevoree.remote.provisioning", "file:///"+mavenDir.getAbsolutePath());
        }

        String response = (String) JOptionPane.showInputDialog(this,
                "Kevoree node name ? (format : name[:port])",
                "Kevoree node runtime",
                JOptionPane.QUESTION_MESSAGE, smallIcon, null, "");


        if (response == null) {
            System.exit(0);
        }

		if (response.contains(":")) {
			try {
				String[] splitted = response.split(":");
				Integer.parseInt(splitted[1]); // use to check if the port parameter is valid
				System.setProperty("node.port", splitted[1]);
				response = splitted[0];
			} catch (PatternSyntaxException e) {}
			catch (NumberFormatException e){}
		}
		String nodeName = response;

        System.setProperty("node.name", response);


		/*//////// add to allow user to set the port number
		 response = (String) JOptionPane.showInputDialog(this,
                "Kevoree node port number ?",
                "Kevoree node runtime",
                JOptionPane.QUESTION_MESSAGE, smallIcon, null, "");

		if (response == null) {
            System.exit(0);
        }

        System.setProperty("node.port", response);*/

        this.setTitle(nodeName + " : KevoreeNode");
        singleton.setVisible(true);
    }

    public static void showShell(JComponent shell) {
        singleton.add(shell, BorderLayout.CENTER);
        singleton.pack();
    }


}
