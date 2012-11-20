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
package org.kevoree.tools.ui.editor;

import com.explodingpixels.macwidgets.plaf.HudComboBoxUI;

import javax.swing.*;
import javax.swing.event.ListDataListener;

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
    public static JComponent createJComboBox(DefaultComboBoxModel model,HudComboBoxUI hui){
        JComboBox cb = new JComboBox(model) ;
        cb.setUI(hui) ;
        return cb;
    }


    public static void addItemToBox(JComboBox box,Object o){
        box.addItem(o);
    }

    public static void addListenerToModel(Object box, ListDataListener list){
        ((JComboBox)box).getModel().addListDataListener(list);
    }


    public static void addItem(DefaultComboBoxModel model,String elem){
        model.addElement(elem);
    }

    public static void addItem(Object model,String elem){
        ((DefaultComboBoxModel)model).addElement(elem);
    }
    public static void setSelectedItem(Object obj,Object value){
        ((JComboBox)obj).setSelectedItem(value);
    }

    public static Object getSelectedItem(Object combo){
        return ((JComboBox)combo).getSelectedItem();
    }


    public static Object getSelectedItemfromModel(Object combo){
        return ((ComboBoxModel)combo).getSelectedItem();
    }

}
