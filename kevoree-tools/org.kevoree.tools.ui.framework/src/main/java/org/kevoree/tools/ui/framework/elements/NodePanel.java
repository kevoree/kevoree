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
package org.kevoree.tools.ui.framework.elements;

import java.awt.Color;

import org.kevoree.tools.ui.framework.ErrorHighlightableElement;
import org.kevoree.tools.ui.framework.RoundedTitledPanel;
import org.kevoree.tools.ui.framework.SelectElement;

/**
 * @author ffouquet
 */
public class NodePanel extends RoundedTitledPanel implements SelectElement, ErrorHighlightableElement {

    public NodePanel() {

        this.setBackground(new Color(100, 100, 100, 150));
    }

    private Boolean selected = false;

    @Override
    public void setSelected(Boolean _selected) {
        selected = _selected;
        active = _selected;
    }

    @Override
    public Boolean getSelected() {
        return selected;
    }


/*
@Override
public Dimension getPreferredSize() {
    Dimension parentsize = this.getParent().getSize();

//    System.out.println(this.get"")

    Dimension preferedDim = new Dimension(0, 0);
    for (Component child : content.getComponents()) {
        preferedDim.width = Math.max(child.getMinimumSize().width, preferedDim.width);
        preferedDim.height = Math.max(child.getLocation().y + child.getSize().height, preferedDim.height);
    }
    return preferedDim;
}*/

    private STATE _state = STATE.NO_ERROR;

    @Override
    public void setState(STATE state) {
        _state = state;
        if(_state.equals(STATE.IN_ERROR)){
             this.setBackground(new Color(239, 50, 50, 150));
        } else {
             this.setBackground(new Color(100, 100, 100, 150));
        }
    }

    @Override
    public STATE getCurrentState() {return _state;}

}
