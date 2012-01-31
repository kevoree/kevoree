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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.plaf.HudComboBoxUI;
import com.explodingpixels.macwidgets.plaf.HudTextFieldUI;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class FelixShell extends JPanel {

    private static FelixShell singleton = null;
    public static PrintStream STDwriter = null;
    public static PrintStream ERRwriter = null;
    //public static PrintStream SSTDwriter = null;
    // public static PrintStream SSTDwriter = null;

    private static String eol = System.getProperty("line.separator");

    private JScrollPane scrollShell = null;

    public FelixShell() {
        this.setBackground(new Color(57, 57, 57));
        //this.setBackground(Color.BLACK);

        setLayout(new BorderLayout());
        final RichTextArea textArea = new RichTextArea();
        textArea.setBackground(new Color(57, 57, 57));
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(500, 250));
        // textArea.setRows(15);
        // textArea.setColumns(60);


        singleton = null;
        STDwriter = new PrintStream(new TextOutputStream(textArea, Color.WHITE));
        ERRwriter = new PrintStream(new TextOutputStream(textArea, Color.ORANGE));

        scrollShell = new JScrollPane(textArea);
        IAppWidgetFactory.makeIAppScrollPane(scrollShell);
        add(scrollShell, BorderLayout.CENTER);

        final JTextField input = new JTextField();
        input.setUI(new HudTextFieldUI());

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {

                    try {
                        // textArea.append("==>" + input.getText() + eol);
                        textArea.append(input.getText() + eol, new Color(87, 145, 198), Color.white, true);

                       // shell.executeCommand(input.getText().trim(), STDwriter, ERRwriter);
                        input.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //  JScrollPane scrollInput = new JScrollPane(input);
        // IAppWidgetFactory.makeIAppScrollPane(scrollInput);
        //scrollInput.setOpaque(false);
        //scrollInput.setBorder(null);

        final JComboBox loglevels = new JComboBox(new DefaultComboBoxModel(new String[]{"WARN", "INFO", "DEBUG"}));
        loglevels.setUI(new HudComboBoxUI());
        loglevels.setFocusable(false);
        loglevels.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                if(loglevels.getSelectedItem().toString().equals("DEBUG")){root.setLevel(Level.DEBUG);}
                if(loglevels.getSelectedItem().toString().equals("WARN")){root.setLevel(Level.WARN);}
                if(loglevels.getSelectedItem().toString().equals("INFO")){root.setLevel(Level.INFO);}
            }
        });
        JPanel layoutBOTTOM = new JPanel();
        layoutBOTTOM.setLayout(new BorderLayout());
        layoutBOTTOM.add(input, BorderLayout.CENTER);
        layoutBOTTOM.add(loglevels, BorderLayout.WEST);
        layoutBOTTOM.setOpaque(false);
        layoutBOTTOM.setBorder(null);
        add(layoutBOTTOM, BorderLayout.SOUTH);

        System.setOut(STDwriter);
        System.setErr(ERRwriter);


    }


    private class TextOutputStream extends OutputStream {
        private RichTextArea _textArea = null;
        private Color _color = null;

        public TextOutputStream(final RichTextArea textArea, final Color color) {
            _textArea = textArea;
            _color = color;
        }

        StringBuilder currentLine = new StringBuilder();

        @Override
        public void write(final int i) throws IOException {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        currentLine.append((char) i);
                        if (((char) i) == '\n') {
                            _textArea.append(currentLine.toString(), _color, Color.white, false);
                            currentLine = new StringBuilder();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }

}
