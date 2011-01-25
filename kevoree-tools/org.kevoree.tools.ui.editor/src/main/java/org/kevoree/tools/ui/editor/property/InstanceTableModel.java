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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.kevoree.DictionaryValue;
import org.kevoree.Instance;
import org.kevoree.KevoreeFactory;

/**
 *
 * @author ffouquet
 */
public class InstanceTableModel implements TableModel {

    private Instance instance;

    public InstanceTableModel(Instance inst) {
        instance = inst;
    }

    @Override
    public int getRowCount() {
        try {
            return instance.getTypeDefinition().getDictionaryType().getAttributes().size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "key";
            case 1:
                return "value";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        org.kevoree.DictionaryAttribute att = instance.getTypeDefinition().getDictionaryType().getAttributes().get(rowIndex);
        switch (columnIndex) {
            case 0:
                return att.getName();
            case 1:
                DictionaryValue value = null;
                if (instance.getDictionary() == null) {
                    instance.setDictionary(KevoreeFactory.eINSTANCE.createDictionary());
                }
                for (DictionaryValue v : instance.getDictionary().getValues()) {
                    if (v.getAttribute().equals(att)) {
                        return v.getValue();
                    }
                }
                for (DictionaryValue v : instance.getTypeDefinition().getDictionaryType().getDefaultValues()) {
                    if (v.getAttribute().equals(att)) {
                        return v.getValue();
                    }
                }
                break;
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            org.kevoree.DictionaryAttribute att = instance.getTypeDefinition().getDictionaryType().getAttributes().get(rowIndex);
            DictionaryValue value = null;
            for (DictionaryValue v : instance.getDictionary().getValues()) {
                if (v.getAttribute().equals(att)) {
                    value = v;
                }
            }
            if (value == null) {
                value = KevoreeFactory.eINSTANCE.createDictionaryValue();
                value.setAttribute(att);
                instance.getDictionary().getValues().add(value);
            }
            value.setValue(aValue.toString());
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }
}
