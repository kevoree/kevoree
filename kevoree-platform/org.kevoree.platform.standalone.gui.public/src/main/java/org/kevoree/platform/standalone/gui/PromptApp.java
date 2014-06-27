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

import com.explodingpixels.macwidgets.HudWidgetFactory;
import org.kevoree.impl.DefaultKevoreeFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class PromptApp {

    public static void main(String[] args) throws Exception {

        String hostname = System.getenv("COMPUTERNAME");
        if (hostname == null || hostname.equals("")) {
            try {
                InetAddress addr;
                addr = InetAddress.getLocalHost();
                hostname = addr.getHostName();
            } catch (UnknownHostException ex) {
                hostname = "node_" + new Random().nextInt(1000);
            }
        }

        hostname=hostname.replace(".local","");
        hostname=hostname.replace(".","_");

        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        final JFrame popup = new JFrame("Kevoree Public Runtime");
        popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        popup.setBackground(Color.BLACK);
        popup.setSize(300, 300);
        popup.setPreferredSize(popup.getSize());


        final JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.PAGE_AXIS));
        popupPanel.setBackground(Color.BLACK);
        popupPanel.setOpaque(true);

        if(hostname.length() > 20){
            hostname = hostname.substring(0,20);
        }

        popupPanel.add(HudWidgetFactory.createHudLabel("Node name"));
        final JTextField nodeNameField = HudWidgetFactory.createHudTextField(hostname);
        popupPanel.add(nodeNameField);
        popupPanel.add(HudWidgetFactory.createHudLabel("Group name"));
        final JTextField groupNameField = HudWidgetFactory.createHudTextField("sync_"+new Random().nextInt(100));
        popupPanel.add(groupNameField);
        popupPanel.add(HudWidgetFactory.createHudLabel("Host"));
        final JTextField hostField = HudWidgetFactory.createHudTextField("tcp://mqtt.kevoree.org:81");
        popupPanel.add(hostField);

        JButton startButton = HudWidgetFactory.createHudButton("Start");
        popupPanel.add(startButton);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("repo \"http://oss.sonatype.org/content/groups/public/\"\n");

                if (new DefaultKevoreeFactory().getVersion().toLowerCase().contains("snapshot")) {
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:latest\n");
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:latest\n");
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.mqtt:latest\n");
                } else {
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:release\n");
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:release\n");
                    buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.mqtt:release\n");
                }

                String nodeName = nodeNameField.getText();
                String hostName = hostField.getText();
                String groupName = groupNameField.getText();


                buffer.append("add " + nodeName + " : JavaNode\n");
                buffer.append("add "+groupName+" : MQTTGroup\n");
                buffer.append("attach " + nodeName + " "+groupName+"\n");
                buffer.append("set "+groupName+".broker = \"" + hostName + "\"");

                popup.dispose();
                final KevoreeGUIFrame frame = new KevoreeGUIFrame(buffer.toString(), nodeName);
            }
        });

        popup.getContentPane().add(popupPanel, BorderLayout.CENTER);
        popup.setVisible(true);
    }

}