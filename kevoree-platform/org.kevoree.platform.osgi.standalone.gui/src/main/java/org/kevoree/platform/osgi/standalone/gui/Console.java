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

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

public class Console extends JPanel {


    PipedInputStream piOut;
    PipedInputStream piErr;
    PipedOutputStream poOut;
    PipedOutputStream poErr;
    JTextArea textArea = new JTextArea();

    public Console() throws IOException {

        this.setOpaque(false);
        // Set up System.out
        piOut = new PipedInputStream();
        poOut = new PipedOutputStream(piOut);
        System.setOut(new PrintStream(poOut, true));

        // Set up System.err
        piErr = new PipedInputStream();
        poErr = new PipedOutputStream(piErr);
        System.setErr(new PrintStream(poErr, true));

        // Add a scrolling text area
        textArea.setEditable(true);
        textArea.setRows(20);
        textArea.setColumns(50);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Create reader threads
        new ReaderThread(piOut).start();
        new ReaderThread(piErr).start();

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                input.fifo.add(e.getKeyChar());
            }
        });

        input = new TextFieldInputStream();

        KevoreeGUIFrame.showConsole(this);

    }

    private TextFieldInputStream input = null;

    public TextFieldInputStream getInputStream() {
        return input;
    }

    class TextFieldInputStream extends InputStream {

        public java.util.concurrent.ArrayBlockingQueue<Character> fifo = new java.util.concurrent.ArrayBlockingQueue<Character>(1000);

        public int read() throws IOException {
          //  System.out.println("READ");
            try {
                return fifo.take();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        public int available() throws IOException {
            return fifo.size();
        }

        public void close() throws IOException {
        }
    }


    class ReaderThread extends Thread {
        PipedInputStream pi;

        ReaderThread(PipedInputStream pi) {
            this.pi = pi;
        }

        public void run() {
            final byte[] buf = new byte[1024];
            try {
                while (true) {
                    final int len = pi.read(buf);
                    if (len == -1) {
                        break;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            textArea.append(new String(buf, 0, len));

                            // Make sure the last line is always visible
                            textArea.setCaretPosition(textArea.getDocument().getLength());

                            // Keep the text area down to a certain character size
                            /*
                            int idealSize = 1000;
                            int maxExcess = 500;
                            int excess = textArea.getDocument().getLength() - idealSize;
                            if (excess >= maxExcess) {
                                textArea.replaceRange("", 0, excess);
                            }   */
                        }
                    });
                }
            } catch (IOException e) {
            }
        }
    }
}