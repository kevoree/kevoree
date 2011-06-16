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
package org.kevoree.tools.ui.framework;

import sun.tools.tree.ThisExpression;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 18/05/11
 * Time: 23:28
 * To change this template use File | Settings | File Templates.
 */
public class ZoomPanel extends JPanel {

    public ZoomPanel() {

        //this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK |
                     AWTEvent.MOUSE_MOTION_EVENT_MASK);

    }


    public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			AffineTransform oldTransform = g2.getTransform();
			AffineTransform transform = AffineTransform.getScaleInstance(zoomFactor, zoomFactor);
			g2.setTransform(transform);
			super.paint(g);
			g2.setTransform(oldTransform);
		}


    /*Variables*/
    private double zoomFactor = 1.0;   //<-- Change zoom factor to see effect
       /*

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clears background
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform tx2 = new AffineTransform();
        tx2.scale(zoomFactor, zoomFactor);
        g2.transform(tx2);
        //  System.out.println("Scale");
    }//end paint()
     */

    /**
     * Change zoom factor
     *
     * @param newZoomFactor
     */
    public void changeZoom(double newZoomFactor) {

        this.zoomFactor = newZoomFactor;

        revalidate();
        repaint();
    }//end method


    /**
     * Return current zoomFacor
     *
     * @return
     */
    public double getZoomFactor() {
        return this.zoomFactor;
    }


    //Override getPreferredSize()


    public Dimension getPreferredSize() {

        // System.out.println("GetDIm");

        Dimension unzoomed
                = getLayout().preferredLayoutSize(this);
        Dimension zoomed
                = new Dimension((int) (unzoomed.width * zoomFactor),
                (int) (unzoomed.height * zoomFactor));

        return zoomed;
    }//end method


    @Override
    protected void processMouseEvent(MouseEvent e) {
        translateMouseLocation(e);
        super.processMouseEvent(e);
    }

    @Override
    public void processMouseMotionEvent(MouseEvent e) {
        translateMouseLocation(e);
        super.processMouseEvent(e);
    }

    private void translateMouseLocation(final MouseEvent e) {
        System.out.println("Translate " + e.getLocationOnScreen());
        final Point p = e.getPoint();
        p.setLocation((int) (p.x / this.zoomFactor),
                (int) (p.y / this.zoomFactor));
    }


}
