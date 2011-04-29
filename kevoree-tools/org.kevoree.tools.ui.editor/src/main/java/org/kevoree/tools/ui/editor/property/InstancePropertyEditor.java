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

import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;

/**
 *
 * @author ffouquet
 * @contibutor gnain
 */
public class InstancePropertyEditor extends NamedElementPropertyEditor {

    public InstancePropertyEditor(org.kevoree.Instance elem, KevoreeUIKernel _kernel) {
        super(elem, _kernel);

        JTable table = new JTable(new InstanceTableModel(elem));

        //Column resizing management on property editor
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setResizable(true);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        scrollPane.setPreferredSize(new Dimension(300,150));

        this.addCenter(scrollPane);

    }
}
