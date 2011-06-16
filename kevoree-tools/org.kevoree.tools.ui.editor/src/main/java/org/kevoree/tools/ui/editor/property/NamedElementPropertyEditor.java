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
/* $Id: NamedElementPropertyEditor.java 12992 2010-10-14 10:25:01Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.property;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.RemoveInstanceCommand;
import org.kevoree.tools.ui.editor.widget.JCommandButton;
import org.kevoree.tools.ui.framework.ThreePartRoundedPanel;
import org.kevoree.tools.ui.framework.TitledElement;

/**
 * @author ffouquet
 */
public class NamedElementPropertyEditor extends ThreePartRoundedPanel {

    private org.kevoree.NamedElement namedElem = null;
    private TitledElement gui = null;
    protected KevoreeUIKernel kernel;

    public NamedElementPropertyEditor(org.kevoree.NamedElement elem, KevoreeUIKernel _kernel) {
        namedElem = elem;
        kernel = _kernel;
        gui = (TitledElement) kernel.getUifactory().getMapping().get(namedElem);



        JPanel p = new JPanel(new SpringLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("Name", JLabel.TRAILING);
        l.setOpaque(false);
        l.setForeground(Color.WHITE);
        p.add(l);
        JTextField textField = new JTextField(10);
        textField.setOpaque(false);
        l.setLabelFor(textField);
        p.add(textField);
        textField.setText(namedElem.getName());

        this.addCenter(p);
        SpringUtilities.makeCompactGrid(p,
                1, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad


        JCommandButton btDelete = new JCommandButton("Delete");
        RemoveInstanceCommand removecmd = new RemoveInstanceCommand(elem);
        removecmd.setKernel(kernel);
        btDelete.setCommand(removecmd);
        this.addCenter(btDelete);

        textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    updateName(e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(NamedElementPropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    updateName(e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(NamedElementPropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    updateName(e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(NamedElementPropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });


    }

    public void updateName(String newname) {
        namedElem.setName(newname);
        gui.setTitle(newname);
        //kernel.getModelPanel().revalidate();

    }
}
