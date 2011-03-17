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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.listeners.Art2ElementDragSourceListener;
import org.kevoree.editor.component.creator.palettes.Library;
import org.kevoree.editor.component.creator.listeners.TransferableArt2Element;
import org.kevoree.editor.component.creator.palettes.Art2Element;
import org.kevoree.editor.component.creator.palettes.Component;
import org.kevoree.editor.component.creator.palettes.MessageInputPort;

/**
 *
 * @author gnain
 */
public class PalettePanel extends JPanel {

    private Collection<Art2Element> elements;

    //private JList elementList;
    private List<JPanel> elementPanels;
    private JPanel container;

    public PalettePanel() {
        initComponents();
        layoutComponents();

    }

    private void initComponents() {
        elements = new ArrayList<Art2Element>();
        elements.add(new Library());
        elements.add(new Component());
        elements.add(new MessageInputPort());
        

        
        elementPanels = new ArrayList<JPanel>();
        container = new JPanel();

        setOpaque(false);
    }

    private void layoutComponents() {
        setBorder(BorderFactory.createLineBorder(Color.blue));
        setLayout(new BorderLayout());
        
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

        for(Art2Element e : elements) {
            //container.add(new PaletteElementPanel(e,this));
            container.add(new PaletteElementPanel(e,
                    new Art2ElementDragSourceListener(e)));
        }
        
        //add(elementList, BorderLayout.NORTH);
        add(container, BorderLayout.NORTH);
    }

    
}
