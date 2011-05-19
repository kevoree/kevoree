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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by IntelliJ IDEA.
 * User: ffouquet
 * Date: 18/05/11
 * Time: 23:28
 * To change this template use File | Settings | File Templates.
 */
public class ZoomPanel extends JPanel {


    /*Variables*/
    private double zoomFactor = 2.0;   //<-- Change zoom factor to see effect
    private AffineTransform scaleXform, inverseXform;

    //Override super.paint()
    
    @Override
    public void paint(Graphics g) {
        super.paintComponent(g); // clears background
        Graphics2D g2 = (Graphics2D) g;

        /*1) Backup current transform*/
        AffineTransform backup = g2.getTransform();
        scaleXform = new AffineTransform(this.zoomFactor,
                0.0, 0.0,
                this.zoomFactor,
                0.0, 0.0);

        /*3) Create the inverse of scale (used on mouse evt points)*/
        try {
            inverseXform = new AffineTransform();
            inverseXform = scaleXform.createInverse();
        } catch (Exception ex) {
        }

        /*4) Apply transformation*/
        g2.transform(scaleXform);

        super.paint(g);

        /*After drawing do*/
        g2.setTransform(backup);
    }//end paint()


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
        Dimension unzoomed
                = getLayout().preferredLayoutSize(this);
        Dimension zoomed
                = new Dimension((int) (unzoomed.width * zoomFactor),
                (int) (unzoomed.height * zoomFactor));

        return zoomed;
    }//end method




}
