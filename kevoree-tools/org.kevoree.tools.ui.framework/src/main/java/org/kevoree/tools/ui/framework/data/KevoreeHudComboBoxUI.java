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

import com.explodingpixels.macwidgets.plaf.*;
import java.awt.*;
import javax.swing.plaf.basic.*;
import org.kevoree.*;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 26/10/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeHudComboBoxUI extends HudComboBoxUI {


    @Override
    protected ComboPopup createPopup() {
        KevoreeComboPopup popup = new KevoreeComboPopup(comboBox);
        popup.setFont(HudPaintingUtils.getHudFont().deriveFont(Font.PLAIN));
        // install a custom ComboBoxVerticalCenterProvider that takes into account the size of the
        // drop shadow.
        popup.setVerticalComponentCenterProvider(createComboBoxVerticalCenterProvider());
        return popup;
    }

    @Override
    protected void updateDisplayedItem() {
        String displayValue;
        Object o = comboBox.getSelectedItem();
        if(DeployUnit.class.isAssignableFrom(o.getClass())) {
            DeployUnit elem = (DeployUnit)o;
            if(!elem.getName().equals("")) {
                displayValue = elem.getName();
            } else {
                displayValue = elem.getGroupName() + ":" + elem.getUnitName() + ":" + elem.getVersion();
            }
        } else if(NamedElement.class.isAssignableFrom(o.getClass())) {
           displayValue = ((NamedElement)o).getName();
        } else {
            displayValue = o.toString();
        }
        arrowButton.setText(displayValue);
    }

}
