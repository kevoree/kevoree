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

import org.kevoree.tools.ui.framework.ErrorHighlightableElement;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author ffouquet
 */
public class BindingPanel extends JPanel {

    private Collection<Binding> bindings = new ArrayList();

    private JPanel mySelf;


    @Override
    public boolean contains(int x, int y) {
        for (Binding b : bindings) {
            Point p1 = null;
            Point ptemp = new Point();
            ptemp = b.getFrom().getLocationOnScreen();
            ptemp.setLocation(ptemp.getX() + (b.getFrom().getWidth() / 2), ptemp.getY() + (b.getFrom().getHeight() / 2));
            SwingUtilities.convertPointFromScreen(ptemp, mySelf);
            p1 = ptemp;
            Point p2 = null;
            ptemp = b.getTo().getLocationOnScreen();
            ptemp.setLocation(ptemp.getX() + (b.getTo().getWidth() / 2), ptemp.getY() + (b.getTo().getHeight() / 2));
            SwingUtilities.convertPointFromScreen(ptemp, mySelf);
            p2 = ptemp;
            if (p1 != null && p2 != null) {
                Point intermed = new Point();
                intermed.y = Math.max(p2.y, p1.y) + 15;
                intermed.x = (p2.x + p1.x) / 2;
                QuadCurve2D curve = new QuadCurve2D.Float(p1.x, p1.y, intermed.x, intermed.y, p2.x, p2.y);
                if (curve.contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public BindingPanel() {
        this.setOpaque(false);
        mySelf = this;
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {

               // System.out.println("Hi" + mouseEvent.getPoint().getX() + "-" + mouseEvent.getPoint().getY());
                for (Binding b : bindings) {
                    Point p1 = null;
                    Point ptemp = new Point();
                    ptemp = b.getFrom().getLocationOnScreen();
                    ptemp.setLocation(ptemp.getX() + (b.getFrom().getWidth() / 2), ptemp.getY() + (b.getFrom().getHeight() / 2));
                    SwingUtilities.convertPointFromScreen(ptemp, mySelf);
                    p1 = ptemp;
                    Point p2 = null;
                    ptemp = b.getTo().getLocationOnScreen();
                    ptemp.setLocation(ptemp.getX() + (b.getTo().getWidth() / 2), ptemp.getY() + (b.getTo().getHeight() / 2));
                    SwingUtilities.convertPointFromScreen(ptemp, mySelf);
                    p2 = ptemp;
                    if (p1 != null && p2 != null) {
                        Point intermed = new Point();
                        intermed.y = Math.max(p2.y, p1.y) + 15;
                        intermed.x = (p2.x + p1.x) / 2;
                        QuadCurve2D curve = new QuadCurve2D.Float(p1.x, p1.y, intermed.x, intermed.y, p2.x, p2.y);
                        if (curve.contains(mouseEvent.getX(), mouseEvent.getY())) {
                            b.triggerListeners();
                        }
                    }
                }
            }
        });
    }

    public void addBinding(Binding b) {
        bindings.add(b);
    }

    public void removeBinding(Binding b) {
        bindings.remove(b);
    }

    public void clear() {
        Collection<Binding> rbindings = new ArrayList();
        rbindings.addAll(bindings);
        for (Binding b : rbindings) {
            removeBinding(b);
        }
    }

    public Collection<Binding> getBindings() {
        return bindings;
    }

    public void drawCable(Graphics2D g2D, Point start, Point end) {
        Point intermed = new Point();
        intermed.y = Math.max(end.y, start.y) + 15;
        intermed.x = (end.x + start.x) / 2;
        g2D.draw(new QuadCurve2D.Float(start.x, start.y, intermed.x, intermed.y, end.x, end.y));
    }


    @Override
    public void paint(Graphics g) {

        super.paint(g);
        g.setPaintMode();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
        g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));


        for (Binding b : bindings) {
            //g2d.setColor(new Color(243, 238, 39, 150));
            Point p1 = null;
            Point ptemp = new Point();
            ptemp = b.getFrom().getLocationOnScreen();
            ptemp.setLocation(ptemp.getX() + (b.getFrom().getWidth() / 2), ptemp.getY() + (b.getFrom().getHeight() / 2));
            SwingUtilities.convertPointFromScreen(ptemp, this);
            p1 = ptemp;
            Point p2 = null;
            ptemp = b.getTo().getLocationOnScreen();
            ptemp.setLocation(ptemp.getX() + (b.getTo().getWidth() / 2), ptemp.getY() + (b.getTo().getHeight() / 2));
            SwingUtilities.convertPointFromScreen(ptemp, this);
            p2 = ptemp;
            if (p1 != null && p2 != null) {
                /*
                if (b.getSelected()) {
                    g2d.setColor(new Color(243, 0, 0, 150));
                } else {
                    g2d.setColor(new Color(243, 238, 39, 150));
                }*/
                if (b.getCurrentState().equals(ErrorHighlightableElement.STATE.IN_ERROR)) {
                    float dash1[] = {10.0f};
                    BasicStroke dashed = new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
                    g2d.setStroke(dashed);
                } else {
                    //g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

                    g2d.setStroke(b.getStroke());
                }

                g2d.setColor(b.getActualColor());

                drawCable(g2d, p1, p2);
            }
        }

        g.setColor(new Color(255, 255, 255, 150));
    }
}
