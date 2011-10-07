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
package org.kevoree.tools.ui.editor.property;

import com.explodingpixels.macwidgets.HudWidgetFactory;
import com.explodingpixels.macwidgets.plaf.HudLabelUI;
import com.explodingpixels.macwidgets.plaf.HudTextFieldUI;
import org.kevoree.Dictionary;
import org.kevoree.DictionaryValue;
import org.kevoree.KevoreeFactory;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import scala.Some;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author ffouquet
 * @contibutor gnain
 */
public class InstancePropertyEditor extends NamedElementPropertyEditor {

    public InstancePropertyEditor(final org.kevoree.Instance elem, KevoreeUIKernel _kernel) {
        super(elem, _kernel);

        JPanel p = new JPanel(new SpringLayout());
        p.setBorder(null);
        if (elem.getTypeDefinition().getDictionaryType().isDefined()) {
            for (final org.kevoree.DictionaryAttribute att : elem.getTypeDefinition().getDictionaryType().get().getAttributesForJ()) {
                JLabel l = new JLabel(att.getName(), JLabel.TRAILING);
                l.setUI(new HudLabelUI());
                //l.setOpaque(false);
                //l.setForeground(Color.WHITE);
                p.add(l);

                if (att.getDatatype() != "") {
                    if (att.getDatatype().startsWith("enum=")) {
                        String values = att.getDatatype().replaceFirst("enum=", "");
                        final JComboBox comboBox = HudWidgetFactory.createHudComboBox(new DefaultComboBoxModel(values.split(",")));
                        l.setLabelFor(comboBox);
                        p.add(comboBox);
                        comboBox.setSelectedItem(getValue(elem, att));
                        comboBox.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                setValue(comboBox.getSelectedItem().toString(), elem, att);
                            }
                        });


                    }
                } else {
                    JTextField textField = new JTextField(10);
                    textField.setUI(new HudTextFieldUI());
                    textField.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent documentEvent) {
                            try {
                                setValue(documentEvent.getDocument().getText(0, documentEvent.getDocument().getLength()), elem, att);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void removeUpdate(DocumentEvent documentEvent) {
                            try {
                                setValue(documentEvent.getDocument().getText(0, documentEvent.getDocument().getLength()), elem, att);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void changedUpdate(DocumentEvent documentEvent) {
                            try {
                                setValue(documentEvent.getDocument().getText(0, documentEvent.getDocument().getLength()), elem, att);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    //textField.setOpaque(false);
                    l.setLabelFor(textField);
                    p.add(textField);
                    textField.setText(getValue(elem, att));
                }


            }

            SpringUtilities.makeCompactGrid(p,
                    elem.getTypeDefinition().getDictionaryType().get().getAttributesForJ().size(), 2, //rows, cols
                    6, 6,        //initX, initY
                    6, 6);       //xPad, yPad
        }
        p.setOpaque(false);


        // JTable table = new JTable(new InstanceTableModel(elem));
        // table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);


        //Column resizing management on property editor
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // table.getColumnModel().getColumn(0).setResizable(true);

        JScrollPane scrollPane = new JScrollPane(p);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(null);
        // p.setFillsViewportHeight(true);

        scrollPane.setPreferredSize(new Dimension(250, 150));

        this.addCenter(scrollPane);

    }

    public String getValue(org.kevoree.Instance instance, org.kevoree.DictionaryAttribute att) {
        DictionaryValue value = null;
        if (instance.getDictionary().isEmpty()) {
            instance.setDictionary(new Some<Dictionary>(KevoreeFactory.createDictionary()));
        }
        for (DictionaryValue v : instance.getDictionary().get().getValuesForJ()) {
            if (v.getAttribute().equals(att)) {
                return v.getValue();
            }
        }
        for (DictionaryValue v : instance.getTypeDefinition().getDictionaryType().get().getDefaultValuesForJ()) {
            if (v.getAttribute().equals(att)) {
                return v.getValue();
            }
        }
        return "";
    }

    public void setValue(Object aValue, org.kevoree.Instance instance, org.kevoree.DictionaryAttribute att) {
        DictionaryValue value = null;
        for (DictionaryValue v : instance.getDictionary().get().getValuesForJ()) {
            if (v.getAttribute().equals(att)) {
                value = v;
            }
        }
        if (value == null) {
            value = KevoreeFactory.createDictionaryValue();
            value.setAttribute(att);
            instance.getDictionary().get().addValues(value);
        }
        value.setValue(aValue.toString());
    }


}
