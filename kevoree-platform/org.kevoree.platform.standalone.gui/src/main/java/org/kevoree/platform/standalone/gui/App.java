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
import org.kevoree.log.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class App {

    public static void main(String[] args) throws Exception {

        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
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

        hostname = hostname.replace(".local", "");
        hostname = hostname.replace(".", "_");

        final JFrame popup = new JFrame("Kevoree Public Runtime");
        popup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        popup.setBackground(Color.BLACK);
        popup.setSize(300, 300);
        popup.setPreferredSize(popup.getSize());

        final JPanel popupPanel = new JPanel();
        popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.PAGE_AXIS));
        popupPanel.setBackground(Color.BLACK);
        popupPanel.setOpaque(true);

        if (hostname.length() > 20) {
            hostname = hostname.substring(0, 20);
        }

        popupPanel.add(HudWidgetFactory.createHudLabel("Node name"));
        final JTextField nodeNameField = HudWidgetFactory.createHudTextField(hostname);
        popupPanel.add(nodeNameField);
        popupPanel.add(HudWidgetFactory.createHudLabel("Group name"));
        final JTextField groupNameField = HudWidgetFactory.createHudTextField("sync_" + new Random().nextInt(10000));
        popupPanel.add(groupNameField);
        popupPanel.add(HudWidgetFactory.createHudLabel("Host"));
        final JTextField hostField = HudWidgetFactory.createHudTextField("tcp://mqtt.kevoree.org:81");
        popupPanel.add(hostField);

        JButton startButton = HudWidgetFactory.createHudButton("Start");
        popupPanel.add(startButton);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final String nodeName = nodeNameField.getText();
                String hostName = hostField.getText();
                final String groupName = groupNameField.getText();
                if ("".equals(hostName)) {
                    hostName = null;
                }
                popup.dispose();
                final String finalHostName = hostName;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            final KevoreeGUIFrame frame = new KevoreeGUIFrame(nodeName, groupName, finalHostName);
                        } catch (NoSuchMethodException e1) {
                            e1.printStackTrace();
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                        } catch (InvocationTargetException e1) {
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        popup.getContentPane().add(popupPanel, BorderLayout.CENTER);
        popup.setVisible(true);
    }

}