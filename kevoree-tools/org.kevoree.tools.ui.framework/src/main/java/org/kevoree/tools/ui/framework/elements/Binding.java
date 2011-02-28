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
import javax.swing.JComponent;
import org.kevoree.tools.ui.framework.AbstractSelectElement;
import org.kevoree.tools.ui.framework.ErrorHighlightableElement;

/**
 *
 * @author ffouquet
 */
public class Binding extends AbstractSelectElement implements ErrorHighlightableElement {

    private JComponent from = null;
    private JComponent to = null;
    private Color selectedcolor = null;
    private Color unselectedcolor = null;

    private STATE currentState = STATE.NO_ERROR;

    @Override
    public void setState(STATE state) {
        currentState = state;
    }

    @Override
    public STATE getCurrentState() {
        return currentState;
    }

    public enum Type {
        input, ouput
    };

    public Binding(Type t) {
        if (t.equals(Type.input)) {
            selectedcolor = new Color(254, 238, 100, 180);
            unselectedcolor = new Color(200, 238, 39, 180);
        }
        if (t.equals(Type.ouput)) {
            selectedcolor = new Color(254, 0, 0, 180);
            unselectedcolor = new Color(200, 0, 0, 180);
        }
    }

    public JComponent getFrom() {
        return from;
    }

    public void setFrom(JComponent fromPort) {
        this.from = fromPort;
    }

    public JComponent getTo() {
        return to;
    }

    public void setTo(JComponent toPort) {
        this.to = toPort;
    }

    public Color getActualColor() {
        return this.getSelected() ? selectedcolor : unselectedcolor;
    }
}
