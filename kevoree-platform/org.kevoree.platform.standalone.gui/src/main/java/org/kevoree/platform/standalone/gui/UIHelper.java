/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.platform.standalone.gui;

import com.explodingpixels.macwidgets.plaf.HudComboBoxUI;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/11/12
 * Time: 19:26
 */
public class UIHelper {

    public static JComponent createJComboBox(DefaultComboBoxModel model){
        JComboBox cb = new JComboBox(model) ;
        cb.setUI(new HudComboBoxUI()) ;
        return cb;
    }

    public static void addItem(DefaultComboBoxModel model,String elem){
        model.addElement(elem);
    }

}
