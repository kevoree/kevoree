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


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogPanel extends JPanel {

    public static PrintStream STDwriter = null;
    public static PrintStream ERRwriter = null;
    //public static PrintStream SSTDwriter = null;
   // public static PrintStream SSTDwriter = null;

    private static String eol = System.getProperty("line.separator");

    private JScrollPane scrollShell = null;

    public LogPanel() {
        setLayout(new BorderLayout());
        final RichTextArea textArea = new RichTextArea();
         textArea.setBackground(new Color(57,57,57));
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(500,250));
       // textArea.setRows(15);
       // textArea.setColumns(60);

        STDwriter = new PrintStream(new TextOutputStream(textArea,Color.WHITE));
        ERRwriter = new PrintStream(new TextOutputStream(textArea,Color.ORANGE));

        scrollShell =  new JScrollPane(textArea);
        add(scrollShell, BorderLayout.CENTER);
        //scrollShell.setPreferredSize(new Dimension(250,250));

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
                        _textArea.append(((char) i) + "", _color, false);
                         _textArea.selectAll();
                        _textArea.setSelectionStart(_textArea.getDocument().getEndPosition().getOffset());
                       // _textArea.setSelectionColor(_color);
                       // _textArea.append( ((char) i) + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
