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
package org.kevoree.tools.marShellGUI;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class RichTextArea extends JTextPane {

    SimpleAttributeSet style;
    Document doc;

    public RichTextArea() {
        style = new SimpleAttributeSet();
        this.setContentType("text/rtf");
        this.setEditorKit(new javax.swing.text.rtf.RTFEditorKit());
        doc = this.getDocument();
    }

    public void clear(){
        this.setText("");
    }

    public void append(String msg, Color color, boolean isBold) {
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, isBold);
        int len = doc.getLength();
        try {
            doc.insertString(len, msg, style);
        } catch (Exception e) {
            System.out.print("Failed to append msg [" + msg + "]");
        }
    }


}
