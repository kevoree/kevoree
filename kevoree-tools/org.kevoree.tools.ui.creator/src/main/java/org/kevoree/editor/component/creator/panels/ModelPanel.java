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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.Kernel;
import org.kevoree.editor.component.creator.layout.DragDropLayout;
import org.kevoree.editor.component.creator.listeners.ModelPanelDropTargetListener;

/**
 *
 * @author gnain
 */
public class ModelPanel extends JPanel {

    private Kernel kernel;
    private Map<Object,JPanel> mapping;

    public ModelPanel(Kernel k) {
        kernel = k;
        initComponents();
    }

    private void initComponents() {

        mapping = new HashMap<Object,JPanel>();

        new ModelPanelDropTargetListener(kernel, this);

        setLayout(new DragDropLayout());
        setPreferredSize(new Dimension(400,400));
        setBackground(new Color(0, 0, 0, 200));
        setOpaque(true);
    }

    public void addLibrary(JPanel element) {
        add(element);
    }

    public void addComponent(JPanel parent, JPanel element) {
        parent.add(element);
        parent.revalidate();
        parent.repaint();
    }

    public void clearAll() {
        removeAll();
        revalidate();
        repaint();
    }

}
