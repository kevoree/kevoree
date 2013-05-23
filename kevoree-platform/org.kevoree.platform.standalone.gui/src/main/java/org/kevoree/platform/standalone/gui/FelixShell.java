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

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.plaf.HudButtonUI;
import com.explodingpixels.macwidgets.plaf.HudTextFieldUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class FelixShell extends JPanel {

    private static FelixShell singleton = null;
    public static PrintStream STDwriter = null;
    public static PrintStream ERRwriter = null;
    private JScrollPane scrollShell = null;

    public FelixShell() {

        final PipedOutputStream pouts = new PipedOutputStream();
        try {
            PipedInputStream pins = new PipedInputStream(pouts);
            System.setIn(pins);
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.setBackground(new Color(57, 57, 57));
        //this.setBackground(Color.BLACK);

        setLayout(new BorderLayout());
        final RichTextArea textArea = new RichTextArea();
        textArea.setBackground(new Color(57, 57, 57));
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(500, 300));
        // textArea.setRows(15);
        // textArea.setColumns(60);


        singleton = null;
        STDwriter = new PrintStream(new TextOutputStream(textArea, Color.WHITE));
        ERRwriter = new PrintStream(new TextOutputStream(textArea, Color.ORANGE));

        scrollShell = new JScrollPane(textArea);
        IAppWidgetFactory.makeIAppScrollPane(scrollShell);
        add(scrollShell, BorderLayout.CENTER);

        //JPanel layoutBOTTOM = new JPanel();
        //layoutBOTTOM.setLayout(new BorderLayout());
        //layoutBOTTOM.add(loglevels, BorderLayout.WEST);
        //JButton clearBt = new JButton("Clear");
        //clearBt.setUI(new HudButtonUI());
        //layoutBOTTOM.add(clearBt, BorderLayout.WEST);
        /*
        clearBt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                textArea.setText("");
                textArea.repaint();
                textArea.revalidate();
            }
        });
        */
        //layoutBOTTOM.setOpaque(false);
        //layoutBOTTOM.setBorder(null);
        //add(layoutBOTTOM, BorderLayout.SOUTH);
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
//                    try {
                        currentLine.append((char) i);
                        if (((char) i) == '\n') {
                            _textArea.append(currentLine.toString(), _color, Color.white, false);
                            currentLine = new StringBuilder();
                        }
//                    } /*catch (Exception e) {
//                        e.printStackTrace();
//                    }*/
                }
            });
        }
    }

}
