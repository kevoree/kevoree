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
package org.kevoree.tools.ui.editor.standalone;

import com.explodingpixels.macwidgets.*;
import org.kevoree.tools.ui.editor.KevoreeEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Hello world!
 */
public class App {

    static Boolean consoleShow = false;
    static Boolean kevsShow = false;
    static int dividerPos = 0;
    static LocalKevsShell kevsPanel = new LocalKevsShell();

    public static void main(final String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                System.setProperty("apple.laf.useScreenMenuBar", "true");

                final KevoreeEditor artpanel = new KevoreeEditor();
                kevsPanel.setKernel(artpanel.getPanel().getKernel());


                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    // System.out.println("Mac detected");
                    // MacIntegration.addOSXIntegration(artpanel);
                }


                String frameName = "Kevoree Editor";

                if (!artpanel.getEditorVersion().equals("")) {
                    frameName += " - " + artpanel.getEditorVersion();
                }

                //frameName += ("Argument "+args[0]);


                JFrame jframe = new JFrame(frameName);
                MacUtils.makeWindowLeopardStyle(jframe.getRootPane());


                UnifiedToolBar toolBar = new UnifiedToolBar();
                // JButton button = new JButton("Toogle console");
                // button.putClientProperty("JButton.buttonType", "textured");

                AbstractButton toogleConsole = null;
                try {
                    java.net.URL url = App.class.getClassLoader().getResource("terminal.png");
                    ImageIcon icon = new ImageIcon(url);
                    toogleConsole =
                            MacButtonFactory.makeUnifiedToolBarButton(
                                    new JButton("Console", icon));
                    toogleConsole.setEnabled(false);
                    toolBar.addComponentToLeft(toogleConsole);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                AbstractButton toogleKevScriptEditor = null;
                try {
                    java.net.URL url = App.class.getClassLoader().getResource("runprog.png");
                    ImageIcon icon = new ImageIcon(url);
                    toogleKevScriptEditor =
                            MacButtonFactory.makeUnifiedToolBarButton(
                                    new JButton("KevScript", icon));
                    toogleKevScriptEditor.setEnabled(false);
                    toolBar.addComponentToLeft(toogleKevScriptEditor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                AbstractButton toogleTypeEditionMode = null;
                try {
                    java.net.URL url = App.class.getClassLoader().getResource("package.png");
                    ImageIcon icon = new ImageIcon(url);
                    toogleTypeEditionMode = MacButtonFactory.makeUnifiedToolBarButton(new JButton("TypeMode", icon));
                    toogleTypeEditionMode.setEnabled(false);
                    toolBar.addComponentToLeft(toogleTypeEditionMode);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                jframe.add(toolBar.getComponent(), BorderLayout.NORTH);
                toolBar.installWindowDraggerOnWindow(jframe);
                toolBar.disableBackgroundPainter();

                jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jframe.setPreferredSize(new Dimension(800, 600));

                jframe.setJMenuBar(artpanel.getMenuBar());

                // jframe.add(artpanel.getPanel(), BorderLayout.CENTER);


                // jframe.add(new LogPanel(), BorderLayout.SOUTH);

                /*
              String layoutDef =
                      "(COLUMN (LEAF name=center weight=0.95) (LEAF name=bottom weight=0.05))";
              MultiSplitLayout.Node modelRoot =
                      MultiSplitLayout.parseModel(layoutDef);
              JXMultiSplitPane multiSplitPane = new JXMultiSplitPane();
              multiSplitPane.getMultiSplitLayout().setModel(modelRoot);

              multiSplitPane.add(artpanel.getPanel(), "center");
              multiSplitPane.add(new LogPanel(), "bottom");
                */

                final LogPanel logPanel = new LogPanel();

                final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        artpanel.getPanel(), logPanel);
                //splitPane.setResizeWeight(0.3);
                splitPane.setOneTouchExpandable(true);
                splitPane.setContinuousLayout(true);
                splitPane.setDividerSize(6);
                splitPane.setDividerLocation(-100);
                splitPane.setResizeWeight(1.0);
                splitPane.setBorder(null);


                final JPanel p = new JPanel();
                p.setOpaque(false);
                p.setLayout(new BorderLayout());
                p.add(artpanel.getPanel(), BorderLayout.CENTER);
                jframe.add(p, BorderLayout.CENTER);


                BottomBar bottomBar = new BottomBar(BottomBarSize.EXTRA_SMALL);
                //bottomBar.addComponentToCenter(MacWidgetFactory.createEmphasizedLabel("Kevoree Model"));
                jframe.add(bottomBar.getComponent(), BorderLayout.SOUTH);
                bottomBar.installWindowDraggerOnWindow(jframe);


                jframe.pack();
                jframe.setVisible(true);


                assert toogleConsole != null;
                final AbstractButton finalToogleConsole = toogleConsole;
                assert toogleKevScriptEditor != null;
                final AbstractButton finalToogleKevScriptEditor = toogleKevScriptEditor;
                toogleConsole.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {

                        finalToogleConsole.setEnabled(!finalToogleConsole.isEnabled());
                        finalToogleKevScriptEditor.setEnabled(false);
                        kevsShow = false;
                        if (consoleShow) {
                            dividerPos = splitPane.getDividerLocation();
                            p.removeAll();
                            p.add(artpanel.getPanel(), BorderLayout.CENTER);
                            p.repaint();
                            p.revalidate();

                        } else {
                            dividerPos = splitPane.getDividerLocation();
                            p.removeAll();
                            p.add(splitPane, BorderLayout.CENTER);
                            splitPane.setTopComponent(artpanel.getPanel());
                            splitPane.setBottomComponent(logPanel);
                            splitPane.setDividerLocation(dividerPos);
                            p.repaint();
                            p.revalidate();

                        }
                        consoleShow = !consoleShow;
                    }
                });
                toogleKevScriptEditor.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                        finalToogleKevScriptEditor.setEnabled(!finalToogleKevScriptEditor.isEnabled());
                        finalToogleConsole.setEnabled(false);
                        consoleShow = false;
                        if (kevsShow) {
                            dividerPos = splitPane.getDividerLocation();
                            p.removeAll();
                            p.add(artpanel.getPanel(), BorderLayout.CENTER);
                            p.repaint();
                            p.revalidate();

                        } else {
                            dividerPos = splitPane.getDividerLocation();
                            p.removeAll();
                            p.add(splitPane, BorderLayout.CENTER);
                            splitPane.setTopComponent(artpanel.getPanel());
                            //LocalKevsShell kevsPanel = new LocalKevsShell();

                            splitPane.setBottomComponent(kevsPanel);
                            splitPane.setDividerLocation(dividerPos);
                            p.repaint();
                            p.revalidate();

                        }
                        kevsShow = !kevsShow;
                    }
                });

                final AbstractButton finalToogleTypeEditionMode = toogleTypeEditionMode;
                toogleTypeEditionMode.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {
                        finalToogleTypeEditionMode.setEnabled(!finalToogleTypeEditionMode.isEnabled());
                        if (finalToogleTypeEditionMode.isEnabled()) {
                            artpanel.getPanel().setTypeEditor();
                        } else {
                            artpanel.getPanel().unsetTypeEditor();
                        }
                        p.repaint();
                        p.revalidate();
                    }
                });
                dividerPos = splitPane.getDividerLocation();


            }
        });


    }
}
