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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.ui;

import org.kevoree.annotation.*;
import org.kevoree.framework.KevoreeMessage;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 * @author ffouquet
 */
@Provides({
        @ProvidedPort(name = "showText", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "textEntered", type = PortType.MESSAGE, optional = true)
})
@ComponentType
public class FakeConsole extends AbstractFakeStuffComponent {
    private static final Logger logger = LoggerFactory.getLogger(FakeConsole.class);

    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 600;
    private MyFrame frame = null;

    @Override
    public void start() {
        frame = new MyFrame();
        frame.setVisible(true);
        frame.appendSystem("/***** CONSOLE INITIALIZED ********/ ");
    }

    @Override
    public void stop() {
        frame.dispose();
        frame = null;
    }

    public void appendSystem(String text) {
        frame.appendSystem(text);
    }

    public void appendOutgoing(String text) {
        frame.appendOutgoing(text);
        if (isPortBinded("textEntered")) {
            getPortByName("textEntered", MessagePort.class).process(text);
        }
    }

    @Port(name = "showText")
    public void appendIncoming(Object text) {
        if (text != null) {
            if (text instanceof KevoreeMessage) {
                KevoreeMessage kmsg = (KevoreeMessage) text;
                frame.appendIncomming("->");
                for(String key : kmsg.getKeys()){
                    frame.appendIncomming(key+"="+kmsg.getValue(key).get());
                }
            } else {
                frame.appendIncomming("->"+text.toString());
            }
        }
    }

    private class MyFrame extends JFrame {

        private JTextPane screen;
        private JTextArea inputTextField;
        private JButton send;

        public MyFrame() {

            setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
            setLayout(new BorderLayout());
            send = new JButton("Send");
            send.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (inputTextField.getText().length() > 1) {
                        FakeConsole.this.appendOutgoing(inputTextField.getText());
                    }

                }
            });

            screen = new JTextPane();
            screen.setFocusable(false);
            screen.setEditable(false);
            StyledDocument doc = screen.getStyledDocument();
            Style def = StyleContext.getDefaultStyleContext().
                    getStyle(StyleContext.DEFAULT_STYLE);
            Style system = doc.addStyle("system", def);
            StyleConstants.setForeground(system, Color.GRAY);

            Style incoming = doc.addStyle("incoming", def);
            StyleConstants.setForeground(incoming, Color.BLUE);

            Style outgoing = doc.addStyle("outgoing", def);
            StyleConstants.setForeground(outgoing, Color.GREEN);


            final String INITIAL_MESSAGE = "Type your text here";
            inputTextField = new JTextArea();
            inputTextField.setText(INITIAL_MESSAGE);
            inputTextField.setFocusable(true);
            inputTextField.setRequestFocusEnabled(true);
            inputTextField.requestFocus();
            inputTextField.setCaretPosition(0);
            inputTextField.setSelectionStart(0);
            inputTextField.setSelectionEnd(INITIAL_MESSAGE.length());

            inputTextField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (e.isControlDown()) {
                            inputTextField.append("\n");
                        } else {
                            if (inputTextField.getText().length() > 1) {
                                FakeConsole.this.appendOutgoing(inputTextField.getText());
                            }
                            inputTextField.setText("");
                        }
                    }
                }
            });

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(inputTextField, BorderLayout.CENTER);
            bottomPanel.add(send, BorderLayout.EAST);

            add(new JScrollPane(screen), BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            pack();
            setVisible(true);
        }

        public void appendSystem(String text) {
            try {
                StyledDocument doc = screen.getStyledDocument();
                doc.insertString(doc.getLength(), formatForPrint(text), doc.getStyle("system"));
            } catch (BadLocationException ex) {
//                ex.printStackTrace();
                logger.error("Error while trying to append system message in the " + this.getName(), ex);
            }
        }

        public void appendIncomming(String text) {
            try {
                StyledDocument doc = screen.getStyledDocument();
                doc.insertString(doc.getLength(), formatForPrint(text), doc.getStyle("incoming"));
                screen.setCaretPosition(doc.getLength());
            } catch (BadLocationException ex) {
//                ex.printStackTrace();
                logger.error("Error while trying to append incoming message in the " + this.getName(), ex);
                //getLoggerLocal().error(ex.getClass().getSimpleName() + " occured while trying to append text in the terminal.", ex);
            }
        }

        public void appendOutgoing(String text) {
            try {
                StyledDocument doc = screen.getStyledDocument();
                doc.insertString(doc.getLength(), ">" + formatForPrint(text), doc.getStyle("outgoing"));
            } catch (BadLocationException ex) {
//                ex.printStackTrace();
                logger.error("Error while trying to append local message in the " + this.getName(), ex);
                //getLoggerLocal().error(ex.getClass().getSimpleName() + " occured while trying to append text in the terminal.", ex);
            }
        }

        private String formatForPrint(String text) {
            return (text.endsWith("\n") ? text : text + "\n");
        }
    }

    /*public static void main(String[] args) {
        FakeConsole console = null;
        try {
            console = new FakeConsole();
            console.start();
            console.appendSystem("Ceci est un append system");
            Thread.sleep(2 * 1000);
            console.appendIncoming("Ceci est un message entrant");
            Thread.sleep(2 * 1000);
            console.appendOutgoing("Ceci est un message sortant");
            Thread.sleep(1 * 60 * 1000);
            console.stop();
        } catch (InterruptedException ex) {
            if (console != null) {
                //console.getLoggerLocal().error(ex.getClass().getSimpleName(), ex);
            } else {
                System.out.println(ex.toString());
            }
        }

    }*/
}
