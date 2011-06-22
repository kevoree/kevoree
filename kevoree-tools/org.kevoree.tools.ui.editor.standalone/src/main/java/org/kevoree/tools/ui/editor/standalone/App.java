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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import com.explodingpixels.macwidgets.*;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.kevoree.tools.ui.editor.KevoreeEditor;

/**
 * Hello world!
 */
public class App {

    static Boolean consoleShow = false;
    static int dividerPos = 0;

    public static void main(final String[] args) {


        //   System.setProperty("avr.bin", "/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/bin");
        //  System.setProperty("avrdude.config.path", "/Applications/Arduino.app/Contents/Resources/Java/hardware/tools/avr/etc/avrdude.conf");
        //  System.setProperty("serial.port", "/dev/tty.usbmodem621");

        /* TEMP */


        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                System.setProperty("apple.laf.useScreenMenuBar", "true");

                final KevoreeEditor artpanel = new KevoreeEditor();

                String frameName = "Kevoree Editor";

                if (!artpanel.getEditorVersion().equals("")) {
                    frameName += " - " + artpanel.getEditorVersion();
                }

                JFrame jframe = new JFrame(frameName);
                MacUtils.makeWindowLeopardStyle(jframe.getRootPane());


                UnifiedToolBar toolBar = new UnifiedToolBar();
               // JButton button = new JButton("Toogle console");
               // button.putClientProperty("JButton.buttonType", "textured");


                AbstractButton toogleConsole = null;
                try {
                    java.net.URL url = App.class.getClassLoader().getResource("terminal.png");
                    ImageIcon icon = new ImageIcon(url);
                    //button.setIcon(icon);


                    toogleConsole =
                            MacButtonFactory.makeUnifiedToolBarButton(
                                    new JButton("Console", icon));
                    toogleConsole.setEnabled(false);
                    toolBar.addComponentToLeft(toogleConsole);


                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

                final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        artpanel.getPanel(), new LogPanel());
                //splitPane.setResizeWeight(0.3);
                splitPane.setOneTouchExpandable(true);
                splitPane.setContinuousLayout(true);
                splitPane.setDividerSize(6);
                splitPane.setDividerLocation(500);
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
                toogleConsole.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent) {

                        finalToogleConsole.setEnabled(!finalToogleConsole.isEnabled());
                        if (consoleShow) {
                            dividerPos = splitPane.getDividerLocation();
                            p.removeAll();
                            p.add(artpanel.getPanel(), BorderLayout.CENTER);
                            p.repaint();
                            p.revalidate();

                        } else {
                            p.removeAll();
                            p.add(splitPane, BorderLayout.CENTER);
                            splitPane.setTopComponent(artpanel.getPanel());
                            splitPane.setDividerLocation(dividerPos);
                            p.repaint();
                            p.revalidate();

                        }
                        consoleShow = !consoleShow;
                    }
                });
                dividerPos = splitPane.getDividerLocation();

            }
        });


    }
}
