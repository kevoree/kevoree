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
import com.explodingpixels.macwidgets.plaf.HudButtonUI;
import org.kevoree.ContainerRoot;
import org.kevoree.platform.osgi.standalone.BootstrapActivator;
import org.kevoree.platform.osgi.standalone.EmbeddedActivators;
import org.kevoree.platform.osgi.standalone.EmbeddedFelix;
import org.osgi.framework.BundleActivator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class KevoreeGUIFrame extends JFrame {

    public static KevoreeGUIFrame singleton = null;

    public KevoreeGUIFrame(final ContainerRoot model) {
        singleton = this;

        URL urlIcon = getClass().getClassLoader().getResource("kevoree-logo-full.png");
        URL urlSmallIcon = getClass().getClassLoader().getResource("kev-logo-full.png");
        ImageIcon topIIcon = new ImageIcon(urlIcon);
        final ImageIcon smallIcon = new ImageIcon(urlSmallIcon);
        this.setIconImage(smallIcon.getImage());
        JLabel topImage = new JLabel(topIIcon);
        topImage.setOpaque(true);
        topImage.setBackground(new Color(63, 128, 187));
        this.add(topImage, BorderLayout.NORTH);

        File mavenDir = new File(System.getProperty("user.home") + "/.m2/repository");
        if (mavenDir.exists() && mavenDir.isDirectory()) {
            System.out.println("use mavenDir=file:///" + mavenDir.getAbsoluteFile().getAbsolutePath());
            System.setProperty("org.kevoree.remote.provisioning", "file:///" + mavenDir.getAbsolutePath());
        }

        final HudWindow bootstrapPopup = new HudWindow("Kevoree runtime : node properties");
        bootstrapPopup.getJDialog().setSize(400, 200);
        bootstrapPopup.getJDialog().setLocationRelativeTo(null);
        bootstrapPopup.getJDialog().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        JPanel layoutPopup = new JPanel();
        layoutPopup.setOpaque(false);
        layoutPopup.setLayout(new BorderLayout());

        JLabel iconLabel = new JLabel(smallIcon);
        iconLabel.setOpaque(false);

        JButton btOk = new JButton("Ok");
        // btOk.setOpaque(false);
        btOk.setUI(new HudButtonUI());

        layoutPopup.add(iconLabel, BorderLayout.WEST);
        final NodeTypeBootStrapUI nodeUI = new NodeTypeBootStrapUI(model);
        layoutPopup.add(nodeUI, BorderLayout.CENTER);
        layoutPopup.add(btOk, BorderLayout.SOUTH);

        bootstrapPopup.setContentPane(layoutPopup);
        bootstrapPopup.getJDialog().setVisible(true);
        bootstrapPopup.getJDialog().getRootPane().setDefaultButton(btOk);

        btOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                bootstrapPopup.getJDialog().dispose();
                String response = nodeUI.getKevName();//instanceName.getText();
                String[] splitted = null;
                if (response.contains(":")) {
                    try {
                        splitted = response.split(":");
                        Integer.parseInt(splitted[1]); // use to check if the port parameter is valid
                        System.setProperty("node.port", splitted[1]);
                        response = splitted[0];
                    } catch (PatternSyntaxException e) {
                    } catch (NumberFormatException e) {
                    }
                }
                final String nodeName = response;
                System.setProperty("node.name", response);
                setTitle(nodeName + " : " + nodeUI.getKevTypeName());

                final String[] finalSplitted = splitted;
                new Thread() {
                    @Override
                    public void run() {

                        NodeTypeBootStrapModel.checkAndCreate(model, nodeName, nodeUI.getKevTypeName().toString(), nodeUI.currentProperties());
                        final BootstrapActivator btA = new org.kevoree.platform.osgi.standalone.BootstrapActivator();
                        btA.setBootstrapModel(model);

                        EmbeddedActivators.setActivators(Arrays.asList(
                                //      (BundleActivator) new org.ops4j.pax.url.mvn.internal.Activator(),
                                (BundleActivator) new org.apache.felix.shell.impl.Activator(),
                                (BundleActivator) new ConsoleActivator(),
                                (BundleActivator) new org.ops4j.pax.url.assembly.internal.Activator(),
                                btA
                        ));
                        final EmbeddedFelix felix = new EmbeddedFelix();
                        addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent windowEvent) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            //CLOSE KEVOREE BTActivator first ...
                                            btA.stop(felix.getM_fwk().getBundleContext());
                                            felix.getM_fwk().stop();
                                            //setVisible(false);
                                            dispose();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.start();
                            }
                        });
                        felix.run();
                    }
                }.start();

                setVisible(true);


                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            }
        });


    }

    public static void showShell(JComponent shell) {
        singleton.add(shell, BorderLayout.CENTER);
        singleton.pack();
    }


}
