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

import com.explodingpixels.macwidgets.*;
import com.explodingpixels.macwidgets.plaf.HudButtonUI;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.kevoree.platform.standalone.KevoreeBootStrap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class KevoreeGUIFrame extends JFrame {

    public static KevoreeGUIFrame singleton = null;

    private static KevoreeLeftModel left = null;

    public KevoreeGUIFrame(final ContainerRoot model) {
        singleton = this;

        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        UnifiedToolBar toolBar = new UnifiedToolBar();
        add(toolBar.getComponent(), BorderLayout.NORTH);

        URL urlSmallIcon = getClass().getClassLoader().getResource("kev-logo-full.png");
        final ImageIcon smallIcon = new ImageIcon(urlSmallIcon);
        this.setIconImage(smallIcon.getImage());

        URL urlIcon = getClass().getClassLoader().getResource("kevoree-logo-full.png");
        ImageIcon topIIcon = new ImageIcon(urlIcon);
        JLabel topImage = new JLabel(topIIcon);
        topImage.setOpaque(false);
        toolBar.addComponentToLeft(topImage);

        left = new KevoreeLeftModel();
     //   this.add(left, BorderLayout.WEST);





        /*
        File mavenDir = new File(System.getProperty("user.home") + "/.m2/repository");
        if (mavenDir.exists() && mavenDir.isDirectory()) {
            System.out.println("use mavenDir=file:///" + mavenDir.getAbsoluteFile().getAbsolutePath());
            System.setProperty("org.kevoree.remote.provisioning", "file:///" + mavenDir.getAbsolutePath());
        }*/

        String guiConfig = System.getProperty("node.gui.config");
        if (guiConfig == null || guiConfig.equalsIgnoreCase("true")) {
            final HudWindow bootstrapPopup = new HudWindow("Kevoree runtime : node properties");
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
            JScrollPane scrollPane = new JScrollPane(nodeUI);
            IAppWidgetFactory.makeIAppScrollPane(scrollPane);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setOpaque(false);
            scrollPane.setBorder(null);

            layoutPopup.add(scrollPane, BorderLayout.CENTER);
            layoutPopup.add(btOk, BorderLayout.SOUTH);

            bootstrapPopup.setContentPane(layoutPopup);
            bootstrapPopup.getJDialog().getRootPane().setDefaultButton(btOk);
            bootstrapPopup.getJDialog().pack();
            bootstrapPopup.getJDialog().setLocationRelativeTo(null);
            bootstrapPopup.getJDialog().setVisible(true);


            btOk.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    bootstrapPopup.getJDialog().dispose();
                    String response = nodeUI.getKevName();
                    final String nodeName = response;
                    System.setProperty("node.name", response);
                    setTitle(nodeName + " : " + nodeUI.getKevTypeName() + " / Kevoree-" + new DefaultKevoreeFactory().getVersion());
                    new Thread() {
                        @Override
                        public void run() {
                            NodeTypeBootStrapModel
                                    .checkAndCreate(nodeUI.getCurrentModel(), nodeName, nodeUI.getKevTypeName().toString(), nodeUI.getKevGroupTypeName().toString(), nodeUI.getKevGroupName(),
                                            nodeUI.nodeInstancePanel().currentProperties(), nodeUI.groupInstancePanel().currentProperties());
                            startNode(nodeUI.getCurrentModel());
                        }
                    }.start();

                    setSize(800, 600);
                    setPreferredSize(getSize());
                    setVisible(true);
                    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                }
            });
        } else {
            new Thread() {
                @Override
                public void run() {
                    startNode(model);
                }
            }.start();
            setVisible(true);
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }

    }

    private void startNode(ContainerRoot model) {
        final KevoreeBootStrap btA = new KevoreeBootStrap();
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {

            public void run() {
                try {
                    btA.stop();
                } catch (Exception ex) {
                    System.out.println("Error stopping framework: " + ex.getMessage());
                }
            }
        });
        btA.setBootstrapModel(model);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            btA.stop();
                            DefaultSystem.resetSystemFlux();
                            dispose();
                            Runtime.getRuntime().exit(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        try {
            FelixShell shell = null;
            shell = new FelixShell();
            KevoreeGUIFrame.showShell(shell);

            btA.start();

            btA.getCore().registerModelListener(new ModelListener() {
                @Override
                public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
                    return true;
                }

				@Override
				public boolean initUpdate (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
					return true;
				}

                @Override
                public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
                    return true;
                }

                @Override
                public void modelUpdated() {
                    for (ContainerNode node : btA.getCore().getLastModel().getNodes()) {
                        if (node.getName().equals(System.getProperty("node.name"))) {
                            left.reload(node);
                        }
                    }
                }

                @Override
                public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {

                }

                @Override
                public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showShell(JComponent shell) {
        JSplitPane splitPane = MacWidgetFactory.createSplitPaneForSourceList(left.getSourceList(), shell);
        splitPane.setDividerLocation(200);
        singleton.add(splitPane, BorderLayout.CENTER);
        singleton.pack();
    }


}
