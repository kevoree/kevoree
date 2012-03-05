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

import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.jdesktop.swingx.graphics.ShadowRenderer;
import org.kevoree.tools.ui.framework.ErrorHighlightableElement;
import org.kevoree.tools.ui.framework.SelectElement;
import org.kevoree.tools.ui.framework.TitledElement;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author ffouquet
 */
public class ChannelPanel extends JPanel implements TitledElement, SelectElement, ErrorHighlightableElement {

    private String title = "";

    @Override
    public void setTitle(String _title) {
        if (_title != null) {
            title = _title;
            this.setToolTipText("Channel " + title);
            bufferGhost = null;
        }
    }

    public String getTitle() {
        return title;
    }

    private Color borderColor = Color.WHITE;
    private Color actualFillColor = new Color(255, 127, 36, 180);

    public ChannelPanel() {
        setOpaque(false);
        this.setLayout(null);
        this.setPreferredSize(new Dimension(100, 100));
        this.setMaximumSize(new Dimension(100, 100));
        this.setMinimumSize(new Dimension(100, 100));
        this.setSize(new Dimension(100, 100));
    }

    @Override
    public boolean contains(int x, int y) {

        int xx = (SHADOW_SIZE - 6);
        int yy = (SHADOW_SIZE - 6);
        int w = getWidth() - xx * 2;
        int h = getHeight() - yy * 2;
        int arc = 15;
        java.awt.geom.Ellipse2D.Double rec = new java.awt.geom.Ellipse2D.Double(xx, yy, w, h);
        //System.out.println("cont"+x+" "+y+" "+rec.contains(x, y));
        return rec.contains(x, y);
    }

    private BufferedImage bufferGhost;

    @Override
    protected void paintComponent(Graphics g) {

        if (bufferGhost == null) {
            bufferGhost = getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
            int x = (SHADOW_SIZE - 6);
            int y = (SHADOW_SIZE - 6);
            int w = getWidth() - x * 2;
            int h = getHeight() - y * 2;

            Graphics2D g2 = bufferGhost.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (shadow != null) {
                int xOffset = (shadow.getWidth() - w) / 2;
                int yOffset = (shadow.getHeight() - h) / 2;
                g2.drawImage(shadow, x - xOffset, y - yOffset, null);
            }

            GradientPaint grad = new GradientPaint(new Point(0, 0), actualFillColor, new Point(0, getHeight()), new Color(255, 127, 36, 220));
            g2.setPaint(grad);
            g2.fillOval(x, y, w, h);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(borderColor);
            if (selected) {
                g2.setColor(new Color(243, 238, 39, 150));
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.drawOval(x, y, w, h);//, arc, arc);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.BOLD, 9));
            //g2.drawChars(title.toCharArray(), 0, title.length(), 0, 5);
            String[] titles = title.split(":");
            for (int i = 0; i < titles.length; i++) {
                g2.drawString(titles[i].trim(), (int) ((getWidth() / 2) - (title.trim().length() / 2) * 2.6), 40 + i * 25);
                if (i != 0) {
                    g2.drawString(":", (int) ((getWidth() / 2) - 1), 30 + (i * 25));
                }
            }
            //g2.drawString(title, (int)(getWidth()/2-title.length()*3),(getHeight()/2)+5);
            g2.dispose();
        }
        g.drawImage(bufferGhost, 0, 0, this);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        int w = getWidth() - (SHADOW_SIZE - 2) * 2;
        int h = getHeight() - (SHADOW_SIZE - 2) * 2;
        int arc = 15;
        int shadowSize = SHADOW_SIZE;
        shadow =
                GraphicsUtilities.createCompatibleTranslucentImage(w, h);
        Graphics2D g2 = shadow.createGraphics();

        if (active) {
            g2.setColor(new Color(243, 238, 39, 150));
        } else {
            g2.setColor(Color.WHITE);
        }

        g2.setColor(Color.WHITE);
        g2.fillOval(0, 0, w, h);
        g2.dispose();
        ShadowRenderer renderer = new ShadowRenderer(shadowSize, 0.5f, Color.BLACK);
        shadow =
                renderer.createShadow(shadow);
        g2 =
                shadow.createGraphics();
        g2.setColor(Color.GRAY);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillOval(shadowSize, shadowSize, w, h);
        g2.dispose();
    }

    private static final int SHADOW_SIZE = 20;
    private BufferedImage shadow;
    protected boolean active = false;

    private Boolean selected = false;

    @Override
    public void setSelected(Boolean _selected) {
        selected = _selected;
        active = _selected;
    }

    private ErrorHighlightableElement.STATE _state = ErrorHighlightableElement.STATE.NO_ERROR;

    @Override
    public void setState(ErrorHighlightableElement.STATE state) {
        _state = state;
        if (_state.equals(ErrorHighlightableElement.STATE.IN_ERROR)) {
            actualFillColor = new Color(239, 50, 50, 150);
        } else {
            actualFillColor = new Color(255, 127, 36, 180);
        }
    }

    @Override
    public STATE getCurrentState() {
        return _state;
    }


    @Override
    public Boolean getSelected() {
        return selected;
    }
}
