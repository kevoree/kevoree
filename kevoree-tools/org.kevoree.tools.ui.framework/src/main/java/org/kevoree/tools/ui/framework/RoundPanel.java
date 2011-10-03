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
package org.kevoree.tools.ui.framework;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import javax.swing.JPanel;

import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.jdesktop.swingx.graphics.ShadowRenderer;

/**
 * @author ffouquet
 */
public class RoundPanel extends JPanel implements ChangeAwareComponent {

    protected JPanel content = new JPanel();
    private JPanel left = new JPanel();
    private JPanel right = new JPanel();
    protected JPanel up = new JPanel();
    private JPanel down = new JPanel();
    private static final int SHADOW_SIZE = 12;
    private BufferedImage shadow;
    protected boolean active = false;
    private Color backgroundColor = new Color(0, 0, 0, 200);

    @Override
    public void setBackground(Color newc) {
        backgroundColor = newc;
        repaint();
        this.notifyUIChanged();
        revalidate();
    }

    public RoundPanel() {
        this.setOpaque(false);
        setDoubleBuffered(true);
        this.setLayout(new BorderLayout());
        left.setPreferredSize(new Dimension(SHADOW_SIZE, SHADOW_SIZE));
        right.setPreferredSize(new Dimension(SHADOW_SIZE, SHADOW_SIZE));
        up.setPreferredSize(new Dimension(SHADOW_SIZE, SHADOW_SIZE));
        down.setPreferredSize(new Dimension(SHADOW_SIZE, SHADOW_SIZE));
        left.setOpaque(false);
        right.setOpaque(false);
        up.setOpaque(false);
        down.setOpaque(false);
        content.setOpaque(false);
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
        add(up, BorderLayout.NORTH);
        add(down, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        content.setLayout(new FlowLayout());

    }

    @Override
    public boolean contains(int x, int y) {

        int xx = (SHADOW_SIZE - 6);
        int yy = (SHADOW_SIZE - 6);
        int w = getWidth() - xx * 2;
        int h = getHeight() - yy * 2;
        int arc = 15;
        RoundRectangle2D.Double rec = new RoundRectangle2D.Double(xx, yy, w, h, arc, arc);
        //System.out.println("cont"+x+" "+y+" "+rec.contains(x, y));
        return rec.contains(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
       // super.paintComponent(g);

        if (bufferGhost == null) {
            //bufferGhost = createVolatileImage(getWidth(), getHeight(),Transparency.TRANSLUCENT);
           // bufferGhost = getGraphicsConfiguration().createCompatibleVolatileImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
            bufferGhost = getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);

            int x = (SHADOW_SIZE - 6);
            int y = (SHADOW_SIZE - 6);
            int w = getWidth() - x * 2;
            int h = getHeight() - y * 2;
            int arc = 15;

            Graphics2D g2 =bufferGhost.createGraphics();
            g2.setComposite(AlphaComposite.Src);
           // g2.setComposite(AlphaComposite.getInstance(
           //             AlphaComposite.SRC_IN, 50));

            //Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            if (shadow != null) {
                int xOffset = (shadow.getWidth() - w) / 2;
                int yOffset = (shadow.getHeight() - h) / 2;
                g2.drawImage(shadow, x - xOffset, y - yOffset, null);
            }
            g2.setColor(backgroundColor);
            //GradientPaint grad = new GradientPaint(new Point(0, 0), new Color(0, 0, 0, 180), new Point(0, getHeight()), new Color(51, 51, 51, 180));
            //g2.setPaint(grad);
            g2.fillRoundRect(x, y, w, h, arc, arc);
            g2.setStroke(new BasicStroke(2f));
            if (active) {
                g2.setColor(new Color(243, 238, 39, 150));
            } else {
                g2.setColor(Color.WHITE);
            }

            //g2.setComposite(AlphaComposite.Clear);
            g2.drawRoundRect(x, y, w, h, arc, arc);
            g2.dispose();
        }
        g.drawImage(bufferGhost, 0, 0, this);

    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        int w = getWidth() - (SHADOW_SIZE - 2) * 2;
        int h = getHeight() - (SHADOW_SIZE - 2) * 2;
        if (h < 0 || w < 0) return;

        int arc = 15;
        int shadowSize = SHADOW_SIZE;
        shadow =
                GraphicsUtilities.createCompatibleTranslucentImage(w, h);
        Graphics2D g2 = shadow.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        g2.dispose();
        ShadowRenderer renderer = new ShadowRenderer(shadowSize, 0.5f, Color.BLACK);
        shadow =
                renderer.createShadow(shadow);
        g2 =
                shadow.createGraphics();
        g2.setColor(Color.GRAY);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRoundRect(shadowSize, shadowSize, w, h, arc, arc);
        g2.dispose();
        this.notifyUIChanged();
    }

    @Override
    public Component add(Component comp) {
        return content.add(comp);
    }

    private BufferedImage bufferGhost;

    @Override
    public void notifyUIChanged() {
        bufferGhost = null;
    }
}
