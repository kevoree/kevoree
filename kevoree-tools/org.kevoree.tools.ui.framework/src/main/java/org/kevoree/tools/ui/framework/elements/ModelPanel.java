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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.kevoree.tools.ui.framework.listener.DragDropLayout;
import org.kevoree.tools.ui.framework.listener.InstanceDragSourceListener;

/**
 *
 * @author ffouquet
 */
public class ModelPanel extends JLayeredPane {

    // private JLayeredPane layeredPane;
    private BindingPanel bindingPanel;
    private JPanel dragPanel;
    private JXPanel nodePanel;

    public void update() {
        bindingPanel.repaint();
        bindingPanel.revalidate();
        nodePanel.repaint();
        nodePanel.revalidate();

    }





    public ModelPanel() {

        nodePanel = new JXPanel();
        nodePanel.setLayout(new DragDropLayout());
        dragPanel = new JPanel();
        dragPanel.setOpaque(false);
        bindingPanel = new BindingPanel();
        //layeredPane = new JLayeredPane();

        setOpaque(false);

        add(nodePanel, JLayeredPane.DEFAULT_LAYER);
        add(dragPanel, JLayeredPane.DRAG_LAYER);
        add(bindingPanel, JLayeredPane.PALETTE_LAYER);


        GradientPaint grad = new GradientPaint(new Point(0, 0), new Color(63, 128, 187), new Point(0, getHeight()), new Color(51, 51, 51));
        MattePainter matte = new MattePainter(grad);
        CompoundPainter p = new CompoundPainter(matte);
        nodePanel.setBackgroundPainter(p);


        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                nodePanel.setBounds(0, 0, getWidth(), getHeight());
                bindingPanel.setBounds(0, 0, getWidth(), getHeight());
                dragPanel.setBounds(0, 0, getWidth(), getHeight());
                repaint();
                revalidate();
            }
        });
    }

    public void addNode(JPanel p) {
        nodePanel.add(p);
        p.setBounds(50, 50, (int) p.getLayout().preferredLayoutSize(p).getWidth(), (int) p.getLayout().preferredLayoutSize(p).getHeight());
        InstanceDragSourceListener listener = new InstanceDragSourceListener(p);
        repaint();
        revalidate();
    }

    public void addHub(JPanel p) {
        nodePanel.add(p);
        p.setBounds(50, 50, (int) p.getPreferredSize().getWidth(), (int) p.getPreferredSize().getHeight());
        InstanceDragSourceListener listener = new InstanceDragSourceListener(p);
        repaint();
        revalidate();
    }

    public void addBinding(Binding b) {
        bindingPanel.addBinding(b);
        repaint();
        revalidate();
    }

    public void removeInstance(JPanel panel) {
        nodePanel.remove(panel);
        repaint();
        revalidate();
    }

    public void removeBinding(Binding b) {
        bindingPanel.removeBinding(b);
    }

    public void setFlightObject(JPanel fobject) {
        dragPanel.add(fobject);
    }

    public void unsetFlightObject(JPanel fobject) {
        dragPanel.remove(fobject);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension parentsize = this.getParent().getSize();
        Dimension preferedDim = new Dimension(0, 0);
        for (Component child : nodePanel.getComponents()) {
            // preferedDim.width = Math.max(child.getMinimumSize().width, preferedDim.width);
            preferedDim.height = Math.max(child.getLocation().y + child.getSize().height, preferedDim.height);
            preferedDim.width = Math.max(child.getLocation().x + child.getSize().width, preferedDim.width);
        }
        return preferedDim;
    }

    public void clear() {
        bindingPanel.clear();
        nodePanel.removeAll();
    }
}
