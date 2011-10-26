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
package org.kevoree.tools.ui.framework.data;

import com.explodingpixels.widgets.plaf.*;
import javax.swing.*;
import org.kevoree.*;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 26/10/11
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeComboPopup extends EPComboPopup {

    public KevoreeComboPopup(JComboBox comboBox) {
        super(comboBox);
    }

    @Override
    protected void clearAndFillMenu() {
        fPopupMenu.removeAll();

        ButtonGroup buttonGroup = new ButtonGroup();

        // add the given items to the popup menu.
        for (int i = 0; i < fComboBox.getModel().getSize(); i++) {
            Object item = fComboBox.getModel().getElementAt(i);
            JMenuItem menuItem = null;

            //Kevoree element popup rendering hack :-)
            if (DeployUnit.class.isAssignableFrom(item.getClass())) {
                DeployUnit elem = (DeployUnit) item;
                if (!elem.getName().equals("")) {
                    menuItem = new JCheckBoxMenuItem(elem.getName());
                } else {
                    menuItem = new JCheckBoxMenuItem(elem.getGroupName() + ":" + elem.getUnitName() + ":" + elem.getVersion());
                }
            } else if (NamedElement.class.isAssignableFrom(item.getClass())) {
                menuItem = new JCheckBoxMenuItem(((NamedElement) item).getName());
            } else {
                menuItem = new JCheckBoxMenuItem(item.toString());
            }
            // END OF HACK

            menuItem.setFont(fFont);
            menuItem.addActionListener(createMenuItemListener(item));
            buttonGroup.add(menuItem);
            fPopupMenu.add(menuItem);

            // if the current item is selected, make the menu item reflect that.
            if (item.equals(fComboBox.getModel().getSelectedItem())) {
                menuItem.setSelected(true);
                fPopupMenu.setSelected(menuItem);
            }
        }

        fPopupMenu.pack();
        int popupWidth = fComboBox.getWidth() + 5;
        // adjust the width to be slightly wider than the associated combo box.
        fPopupMenu.setSize(popupWidth, fPopupMenu.getHeight());
    }
}
