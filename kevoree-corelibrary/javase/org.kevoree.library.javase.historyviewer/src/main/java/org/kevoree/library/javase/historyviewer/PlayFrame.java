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
package org.kevoree.library.javase.historyviewer;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

/**
* Created with IntelliJ IDEA.
* User: dvojtise
* Date: 09/11/12
* Time: 11:29
* To change this template use File | Settings | File Templates.
*/
class PlayFrame extends JPanel {

    private JTextPane screenTP;
    private JButton clearBtn;

    public PlayFrame() {
        setPreferredSize(new Dimension(ModelHistoryViewer.FRAME_WIDTH, ModelHistoryViewer.FRAME_HEIGHT));
        setLayout(new BorderLayout());
        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //StyledDocument doc = screenTP.getStyledDocument();
                screenTP.setText("");

            }
        });

        screenTP = new JTextPane();
        screenTP.setFocusable(false);
        screenTP.setEditable(false);
        StyledDocument doc = screenTP.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);
        Style system = doc.addStyle("system", def);
        StyleConstants.setForeground(system, Color.GRAY);

        Style incoming = doc.addStyle("modelEvent", def);
        StyleConstants.setForeground(incoming, Color.BLUE);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(clearBtn, BorderLayout.EAST);

        add(new JScrollPane(screenTP), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void appendSystem(String text) {
        try {
            StyledDocument doc = screenTP.getStyledDocument();
            doc.insertString(doc.getLength(), formatForPrint(text), doc.getStyle("system"));
        } catch (BadLocationException ex) {
//                ex.printStackTrace();
            ModelHistoryViewer.logger.error("Error while trying to append system message in the " + this.getName(), ex);
        }
    }

    public void appendModelEvent(String text) {
        try {
            DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            StyledDocument doc = screenTP.getStyledDocument();
            doc.insertString(doc.getLength(), formatForPrint(dateFormat.format(date) + " - " + text), doc.getStyle("modelEvent"));
            screenTP.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            ModelHistoryViewer.logger.error("Error while trying to append incoming message in the " + this.getName(), ex);
        }
    }

    private String formatForPrint(String text) {
        return (text.endsWith("\n") ? text : text + "\n");
    }
}
