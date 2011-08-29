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
import org.jdesktop.swingx.JXTitledSeparator;
import org.kevoree.tools.ui.framework.*;

import javax.swing.*;

/**
 *
 * @author ffouquet
 */
public class ComponentTypePanel extends ThreePartRoundedPanel implements TitledElement, ErrorHighlightableElement/* RoundPanel */ {
                /*
    public ComponentTypePanel(String ttitle) {
        String title = UITools.formatTitle(ttitle,18);
        this.setToolTipText("ComponentType " + ttitle);
        JXTitledSeparator titlebar = new JXTitledSeparator();
        titlebar.setForeground(Color.WHITE);
        titlebar.setTitle(title);
        add(titlebar);
    }      */

    JXTitledSeparator typebar = new JXTitledSeparator();

    public ComponentTypePanel() {
        typebar.setForeground(Color.WHITE);
        typebar.setTitle("");
        typebar.setHorizontalAlignment(SwingConstants.CENTER);
        typebar.setVisible(false);
        addCenter(typebar);

    }

    public void setTypeName(String title) {
        typebar.setTitle(" : "+title);
        typebar.setVisible(true);
    }

    private STATE _state = STATE.NO_ERROR;

    @Override
    public void setState(STATE state) {
        _state = state;
        if (_state.equals(STATE.IN_ERROR)) {
            this.setBackground(new Color(239, 50, 50, 150));
        } else {
            this.setBackground(new Color(0, 0, 0, 200));
        }
    }

    @Override
    public STATE getCurrentState() {
        return _state;
    }



}


