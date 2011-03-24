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

package org.kevoree.editor.component.creator.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.Kernel;
import org.kevoree.editor.component.creator.layout.DragDropLayout;
import org.kevoree.editor.component.creator.listeners.ComponentModelElementDropTargetListener;
import org.kevoree.editor.component.creator.listeners.ModelElementDragSourceListener;

/**
 *
 * @author gnain
 */
public class ComponentModelElement extends Art2ModelElement {

    private String name;
    private RepresentationPanel panel;


    public ComponentModelElement(Kernel kernel,String name) {
        this.name = name;
        panel = new RepresentationPanel(this);
        new ModelElementDragSourceListener(this);
        new ComponentModelElementDropTargetListener(kernel, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Point p) {
        panel.setLocation(p);
    }

    public JPanel getGraphicalRepresentation() {
       return panel;
    }

    private class RepresentationPanel extends JPanel {

        public RepresentationPanel(ComponentModelElement element) {

            setLayout(new DragDropLayout());
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.red),
                    element.getName()));
            setPreferredSize(new Dimension(70,120));
        }

    }

}
