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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.Kernel;
import org.kevoree.editor.component.creator.layout.DragDropLayout;
import org.kevoree.editor.component.creator.listeners.LibraryModelElementDropTargetListener;
import org.kevoree.editor.component.creator.listeners.ModelElementDragSourceListener;

/**
 *
 * @author gnain
 */
public class LibraryModelElement extends Art2ModelElement {

    private String name;
    private RepresentationPanel panel;

    public LibraryModelElement(Kernel kernel, String name) {
        this.name = name;
        panel = new RepresentationPanel(this);
        new ModelElementDragSourceListener(this);
        new LibraryModelElementDropTargetListener(kernel, this);
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

        private class MyMouseListener extends MouseAdapter implements MouseMotionListener {

            private JPanel repPanel;
            private boolean resizeMode = false;
            private Point resizeOrigin;

            public MyMouseListener(JPanel p) {
                this.repPanel = p;
            }

            @Override
            public void mousePressed(MouseEvent me) {
                super.mousePressed(me);
                Point p2 = (Point) me.getPoint().clone();
                Point bottomRight = new Point();
                System.out.println("MousePressed");
                bottomRight.setLocation(
                        repPanel.getWidth(),
                        repPanel.getHeight());
                if (p2.distance(bottomRight) < 7) {
                    System.out.println("ResizeOn");
                    resizeMode = true;
                    resizeOrigin = p2;
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);
                System.out.println("ResizeOff");
                resizeMode = false;
            }

            @Override
            public void mouseMoved(MouseEvent me) {
                super.mouseMoved(me);
                Point p2 = (Point) me.getPoint().clone();
                System.out.println("MouseMoved");
                Point bottomRight = new Point();
                bottomRight.setLocation(
                        repPanel.getWidth(),
                        repPanel.getHeight());

                if (p2.distance(bottomRight) < 7) {
                    repPanel.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else {
                    repPanel.setCursor(Cursor.getDefaultCursor());
                }

                if (resizeMode) {
                    System.out.println("Resizing");
                    int depX = p2.x - resizeOrigin.x;
                    int depY = p2.y - resizeOrigin.y;
                    repPanel.setPreferredSize(
                            new Dimension(
                            repPanel.getWidth() + depX,
                            repPanel.getHeight() + depY));
                    repPanel.getParent().validate();
                    repPanel.getParent().repaint();
                    resizeOrigin = p2;
                }
            }
        }

        public RepresentationPanel(LibraryModelElement element) {

            setLayout(new DragDropLayout());
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.green),
                    element.getName()));
            setPreferredSize(new Dimension(140, 240));

            MyMouseListener lst = new MyMouseListener(this);
            addMouseListener(lst);
            addMouseMotionListener(lst);

        }

        @Override
        public void paint(Graphics grphcs) {
            super.paint(grphcs);
            grphcs.setColor(Color.yellow);

            grphcs.drawLine(getHeight(), getWidth() - 10, getHeight() - 10, getWidth());
        }
    }
}
