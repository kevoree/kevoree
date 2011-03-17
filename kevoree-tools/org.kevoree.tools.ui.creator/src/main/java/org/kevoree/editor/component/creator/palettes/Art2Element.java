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

package org.kevoree.editor.component.creator.palettes;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.listeners.Art2ElementDragSourceListener;

/**
 *
 * @author gnain
 */
public abstract class Art2Element extends JPanel {

    public Art2Element() {
        setOpaque(false);
        //new Art2ElementDragSourceListener(this);
    }

    public abstract JPanel getGraphicalElement();
    public abstract String getElementName();


}
