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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 *
 * @author ffouquet
 */
public class PortPanel extends JPanel implements ErrorHighlightableElement {

	 private STATE currentState = STATE.NO_ERROR;

    @Override
    public void setState(STATE state) {
        currentState = state;
    }

    @Override
    public STATE getCurrentState() {
        return currentState;
    }

	public enum PortType {
        REQUIRED, PROVIDED
    }
    public enum PortNature {
        MESSAGE, SERVICE
    }

    private PortNature nature = null;

    public PortNature getNature() {
        return nature;
    }

    public void setNature(PortNature pt) {
        nature = pt;
        if (pt.equals(PortNature.MESSAGE)) {
            actualFillColor = new Color(255, 127, 36, 240);
        }
        if (pt.equals(PortNature.SERVICE)) {
            actualFillColor = new Color(0, 0, 0, 150);
        }
    }



    private Color borderColor = Color.WHITE;
    private String title = "";

    public void setTitle(String _title) {
        if (_title != null) {
            title = _title;
            this.setToolTipText("Port "+title);
        }
    }

    private PortType portType = null;

    public PortType getType(){
        return portType;
    }

    public void setType(PortType pt) {
        if (pt.equals(PortType.REQUIRED)) {
            borderColor = new Color(255, 0, 0, 150);
        }
        if (pt.equals(PortType.PROVIDED)) {
            borderColor = new Color(68, 68, 68, 150);
        }
        portType =pt;
    }

    /**
     * contructor
     */
    public PortPanel() {

        setOpaque(false);
        this.setLayout(null);
        this.setPreferredSize(new Dimension(30, 30));
        this.setMaximumSize(new Dimension(30, 30));
        this.setMinimumSize(new Dimension(30, 30));
        this.setSize(new Dimension(30, 30));
    }
    private Color actualFillColor = new Color(0, 0, 0, 150);
	private Color errorFillColor = new Color(150, 150, 0, 150);

    protected void paintComponent(Graphics g) {


        int x = 5;
        int y = 5;
        int w = getWidth() - 10;
        int h = getHeight() - 10;
        int arc = 30;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        //g2.setColor(new Color(0, 0, 0, 150));

        //g2.setColor(borderColor);


        /*
        if (shadow != null) {
        int xOffset = (shadow.getWidth() - w) / 2;
        int yOffset = (shadow.getHeight() - h) / 2;
        g2.drawImage(shadow, x - xOffset, y - yOffset, null);
        }
         */

        //g2.setColor(Color.WHITE);

        GradientPaint grad;
		if (getCurrentState().equals(ErrorHighlightableElement.STATE.IN_ERROR)) {
			grad = new GradientPaint(new Point(0, 0), errorFillColor, new Point(0, getHeight()), new Color(150, 150, 150, 180));
		} else {
			grad = new GradientPaint(new Point(0, 0), actualFillColor, new Point(0, getHeight()), new Color(150, 150, 150, 180));
		}
        g2.setPaint(grad);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(borderColor);
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.setColor(Color.ORANGE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        //g2.drawChars(title.toCharArray(), 0, title.length(), 0, 5);
        g2.drawString(title, 0, 9);
        g2.dispose();
    }

    public void highlightDisable() {
        actualFillColor = new Color(255, 0, 0, 150);
        repaint();
        revalidate();
    }

    /**
     * semantic returns : enable the highlights
     */
    //@Override
    public void highlightEnable() {
        actualFillColor = new Color(0, 255, 0, 150);
        repaint();
        revalidate();
    }

    /**
     * semantic returns : stop the highlights
     */
    //@Override
    public void highlightStop() {
        actualFillColor = new Color(0, 0, 0, 150);
        repaint();
        revalidate();
    }
}
