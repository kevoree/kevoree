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

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;
import org.kevoree.*;

import java.awt.Component;


/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 26/10/11
 * Time: 08:50
 * To change this template use File | Settings | File Templates.
 */
public class NamedElementListRenderer implements ListCellRenderer {


    @Override
    public JLabel getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
        JLabel renderer = null;
        System.out.println("Redering " + o.getClass());
        if(DeployUnit.class.isAssignableFrom(o.getClass())) {
            System.out.println("Redering DU");
            DeployUnit elem = (DeployUnit)o;
            if(!elem.getName().equals("")) {
                renderer = new JLabel(elem.getName());
            } else {
                renderer = new JLabel(elem.getGroupName() + ":" + elem.getUnitName() + ":" + elem.getVersion());
            }
        } else if(NamedElement.class.isAssignableFrom(o.getClass())) {
            System.out.println("Redering NamedElem");
            renderer = new JLabel(((NamedElement)o).getName());
        } else {
            System.out.println("Redering Default toString");
            renderer = new JLabel(o.toString());
        }

        return renderer;
    }
}
