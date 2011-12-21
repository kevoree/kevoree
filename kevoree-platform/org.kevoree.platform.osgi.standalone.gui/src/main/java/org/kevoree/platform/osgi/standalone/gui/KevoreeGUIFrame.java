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
import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.plaf.HudButtonUI;
import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.platform.osgi.standalone.BootstrapActivator;
import org.kevoree.platform.osgi.standalone.EmbeddedActivators;
import org.kevoree.platform.osgi.standalone.EmbeddedFelix;
import org.kevoree.platform.osgi.standalone.shell.ShellActivator;
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

public class KevoreeGUIFrame extends JFrame {

    public static KevoreeGUIFrame singleton = null;

    public KevoreeGUIFrame(final ContainerRoot model) {
        singleton = this;

        URL urlIcon = getClass().getClassLoader().getResource(GuiConstantsHandler.getGuiConstantValuesProvider().getIconUrl());//"kevoree-logo-full.png");
        URL urlSmallIcon = getClass().getClassLoader().getResource(GuiConstantsHandler.getGuiConstantValuesProvider().getSmallIconUrl());//"kev-logo-full.png");
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

        final HudWindow bootstrapPopup = new HudWindow(GuiConstantsHandler.getGuiConstantValuesProvider().getBootstrapWindowTitle());//"Kevoree runtime : node properties");
        bootstrapPopup.getJDialog().setSize(400, 210);
        bootstrapPopup.getJDialog().setLocationRelativeTo(null);
        bootstrapPopup.getJDialog().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        JPanel layoutPopup = new JPanel();
        layoutPopup.setOpaque(false);
        layoutPopup.setLayout(new BorderLayout());

        JLabel iconLabel = new JLabel(smallIcon);
        iconLabel.setOpaque(false);

        JButton btOk = new JButton("Ok");
        btOk.setUI(new HudButtonUI());

        layoutPopup.add(iconLabel, BorderLayout.WEST);
        final NodeTypeBootStrapUI nodeUI = new NodeTypeBootStrapUI(model);

        nodeUI.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(nodeUI) ;
        IAppWidgetFactory.makeIAppScrollPane(scrollPane);
        scrollPane.getViewport().setOpaque(false) ;
        scrollPane.setOpaque(false);
        scrollPane.setBorder(null) ;

        layoutPopup.add(scrollPane, BorderLayout.CENTER);
        layoutPopup.add(btOk, BorderLayout.SOUTH);

        bootstrapPopup.setContentPane(layoutPopup);
        bootstrapPopup.getJDialog().setVisible(true);
        bootstrapPopup.getJDialog().getRootPane().setDefaultButton(btOk);

        btOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                bootstrapPopup.getJDialog().dispose();
                String response = nodeUI.getKevName();
                final String nodeName = response;
                System.setProperty("node.name", response);
                setTitle(nodeName + " : " + nodeUI.getKevTypeName()+" / Kevoree-"+ KevoreeFactory.getVersion());
                new Thread() {
                    @Override
                    public void run() {

                        NodeTypeBootStrapModel.checkAndCreate(nodeUI.getCurrentModel(), nodeName, nodeUI.getKevTypeName().toString(), nodeUI.getKevGroupTypeName().toString(), nodeUI.getKevGroupName().toString(), nodeUI.nodeInstancePanel().currentProperties(), nodeUI.groupInstancePanel().currentProperties());
                        final BootstrapActivator btA = new org.kevoree.platform.osgi.standalone.BootstrapActivator();

                        btA.setBootstrapModel(nodeUI.getCurrentModel());

                        EmbeddedActivators.setActivators(Arrays.asList(
                                (BundleActivator) new ShellActivator(),
                                (BundleActivator) new ConsoleActivator(),
                                btA
                        ));
                        EmbeddedActivators.setBootstrapActivator(btA);

                        final EmbeddedFelix felix = new EmbeddedFelix();
                        addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent windowEvent) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            DefaultSystem.resetSystemFlux();
                                            dispose();

                                            //System.setOut(System.);

                                            Runtime.getRuntime().exit(0);


                                            //CLOSE KEVOREE BTActivator first ...
                                            /*
                                            btA.stop(felix.getM_fwk().getBundleContext());
                                            felix.getM_fwk().stop();
                                            //setVisible(false);
                                            dispose();     */
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
