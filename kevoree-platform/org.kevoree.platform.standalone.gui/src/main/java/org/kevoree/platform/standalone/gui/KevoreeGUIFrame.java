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
package org.kevoree.platform.standalone.gui;

import com.explodingpixels.macwidgets.MacButtonFactory;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.ModelListener;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.api.handler.UpdateContext;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.impl.DefaultKevoreeFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class KevoreeGUIFrame extends JFrame {

    private static final String defaultNodeName = "node0";
    public static KevoreeGUIFrame singleton = null;

    private static KevoreeLeftModel left = null;
    final Bootstrap[] bootstrap = new Bootstrap[1];

    public KevoreeGUIFrame() {
        singleton = this;
        MacUtils.makeWindowLeopardStyle(this.getRootPane());
        UnifiedToolBar toolBar = new UnifiedToolBar();
        //toolBar.disableBackgroundPainter();
        add(toolBar.getComponent(), BorderLayout.NORTH);


        Icon ico = new ImageIcon(KevoreeGUIFrame.class.getClassLoader().getResource("android-share.png"));

        AbstractButton macWidgetsButton = MacButtonFactory.makePreferencesTabBarButton(new JButton("Editor", ico));
        toolBar.addComponentToRight(macWidgetsButton);
        macWidgetsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Bootstrap b = bootstrap[0];
                if (b != null) {
                    KevoreeCoreBean bean = b.getCore();
                    if (bean != null) {
                        String version = "release";
                        if (bean.getFactory().getVersion().contains("SNAPSHOT")) {
                            version = "latest";
                        }
                        bean.submitScript("include mvn:org.kevoree.library.java:org.kevoree.library.java.editor:" + version + "\nadd " + bean.getNodeName() + ".editor : WebEditor", new UpdateCallback() {
                            @Override
                            public void run(Boolean aBoolean) {
                                if (aBoolean) {
                                    try {
                                        Desktop.getDesktop().browse(new URI("http://localhost:3042"));
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }, "/");
                    }
                }
            }
        });


        URL urlSmallIcon = getClass().getClassLoader().getResource("kev-logo-full.png");
        final ImageIcon smallIcon = new ImageIcon(urlSmallIcon);
        this.setIconImage(smallIcon.getImage());

        URL urlIcon = getClass().getClassLoader().getResource("kevoree-logo-full.png");
        ImageIcon topIIcon = new ImageIcon(urlIcon);
        JLabel topImage = new JLabel(topIIcon);
        topImage.setOpaque(false);
        toolBar.addComponentToLeft(topImage);

        left = new KevoreeLeftModel();
        new Thread() {
            @Override
            public void run() {
                startNode();
            }
        }.start();
        setVisible(true);

        setPreferredSize(new Dimension(1024, 768));
        setSize(getPreferredSize());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void startNode() {
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {

            public void run() {
                try {
                    bootstrap[0].stop();
                } catch (Throwable ex) {
                    System.out.println("Error stopping framework: " + ex.getMessage());
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            bootstrap[0].stop();
                            dispose();
                            Runtime.getRuntime().exit(0);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        try {
            ConsoleShell shell = new ConsoleShell();
            String nodeName = System.getProperty("node.name");
            if (nodeName == null) {
                nodeName = defaultNodeName;
            }

            bootstrap[0] = new Bootstrap(nodeName);
            KevoreeGUIFrame.showShell(shell);
            bootstrap[0].getCore().registerModelListener(new ModelListener() {
                @Override
                public boolean preUpdate(UpdateContext context) {
                    return true;
                }

                @Override
                public boolean initUpdate(UpdateContext context) {
                    return true;
                }

                @Override
                public boolean afterLocalUpdate(UpdateContext context) {
                    return true;
                }

                @Override
                public void modelUpdated() {

                    ContainerRoot currentModel = bootstrap[0].getCore().getCurrentModel().getModel();
                    ContainerNode currentNode = currentModel.findNodesByID(bootstrap[0].getCore().getNodeName());
                    if (currentNode != null) {
                        left.reload(currentNode);
                    } else {
                        System.out.println("Node not found for refresh in model");
                    }
                }

                @Override
                public void preRollback(UpdateContext context) {

                }

                @Override
                public void postRollback(UpdateContext context) {

                }
            }, "/");

            String bootstrapModel = System.getProperty("node.bootstrap");
            if (bootstrapModel != null) {
                bootstrap[0].bootstrapFromFile(new File(bootstrapModel));
            } else {
                bootstrap[0].bootstrapFromKevScript(createBootstrapScript(nodeName));
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static InputStream createBootstrapScript(String nodeName) {
        StringBuilder buffer = new StringBuilder();
        String versionRequest;
        if (new DefaultKevoreeFactory().getVersion().toLowerCase().contains("snapshot")) {
            buffer.append("repo \"https://oss.sonatype.org/content/groups/public/\"\n");
            versionRequest = "latest";
        } else {
            buffer.append("repo \"http://repo1.maven.org/maven2/\"\n");
            versionRequest = "release";
        }
        buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.javaNode:");
        buffer.append(versionRequest);
        buffer.append("\n");
        buffer.append("include mvn:org.kevoree.library.java:org.kevoree.library.java.ws:");
        buffer.append(versionRequest);
        buffer.append("\n");
        buffer.append("add node0 : JavaNode".replace("node0", nodeName) + "\n");
        buffer.append("add sync : WSGroup\n");
        buffer.append("attach node0 sync\n".replace("node0", nodeName));

        int groupPort = FreeSocketDetector.detect(9000, 9999);

        buffer.append("set sync.port/node0 = \"" + groupPort + "\"");

        return new ByteArrayInputStream(buffer.toString().getBytes());
    }

    public static void showShell(JComponent shell) {
        JSplitPane splitPane = MacWidgetFactory.createSplitPaneForSourceList(left.getSourceList(), shell);
        splitPane.setDividerLocation(200);
        singleton.add(splitPane, BorderLayout.CENTER);
        singleton.pack();
    }


}
