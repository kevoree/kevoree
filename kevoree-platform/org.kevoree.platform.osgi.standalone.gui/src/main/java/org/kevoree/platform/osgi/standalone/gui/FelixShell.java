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


import org.apache.felix.shell.ShellService;

import javax.swing.*;
import java.awt.*;
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

    public FelixShell(final ShellService shell) {

        setLayout(new BorderLayout());
        final RichTextArea textArea = new RichTextArea();
         textArea.setBackground(new Color(57,57,57));
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(500,250));
       // textArea.setRows(15);
       // textArea.setColumns(60);


        singleton = null;
        STDwriter = new PrintStream(new TextOutputStream(textArea,Color.WHITE));
        ERRwriter = new PrintStream(new TextOutputStream(textArea,Color.ORANGE));

        scrollShell =  new JScrollPane(textArea);
        add(scrollShell, BorderLayout.CENTER);

        final JTextField input = new JTextField();

        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
               
                    try {
                       // textArea.append("==>" + input.getText() + eol);
                        textArea.append(input.getText() + eol, new Color(87,145,198), Color.white, true);

                        shell.executeCommand(input.getText().trim(), STDwriter, ERRwriter);
                        input.setText("");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        add(new JScrollPane(input), BorderLayout.SOUTH);

        System.setOut(STDwriter);
        System.setErr(ERRwriter);


    }


    private class TextOutputStream extends OutputStream {
        private RichTextArea _textArea = null;
        private Color _color = null;

        public TextOutputStream(final RichTextArea textArea,final Color color) {
            _textArea = textArea;
            _color = color;
        }

        @Override
        public void write(final int i) throws IOException {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        _textArea.append(((char) i) + "", _color, Color.white, false);
                         _textArea.selectAll();
                        _textArea.setSelectionStart(_textArea.getDocument().getEndPosition().getOffset());
                       // _textArea.append( ((char) i) + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
