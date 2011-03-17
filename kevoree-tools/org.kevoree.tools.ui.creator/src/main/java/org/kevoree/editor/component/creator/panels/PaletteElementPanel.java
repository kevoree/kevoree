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
package org.kevoree.editor.component.creator.panels;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.palettes.Art2Element;

/**
 *
 * @author gnain
 */
public class PaletteElementPanel extends JPanel {

    private Art2Element elem;

    public Art2Element getArt2Element() {
        return elem;
    }

    public PaletteElementPanel(Art2Element elem, DragGestureListener lst) {
        this.elem = elem;
        add(elem.getGraphicalElement());
        add(new JLabel(elem.getElementName()));
        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, lst);
    }
}
