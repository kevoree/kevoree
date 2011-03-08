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
package org.kevoree.tools.ui.framework.elements;


import javax.swing.*;
import java.awt.*;

public class GroupAnchorPanel extends JPanel {

    public GroupPanel getParentPanel() {
        return parentPanel;
    }

    private GroupPanel parentPanel = null;

    public GroupAnchorPanel(GroupPanel parent){
          parentPanel = parent;
    }

    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Color.ORANGE);

        GradientPaint grad = new GradientPaint(new Point(0, 0), Color.ORANGE, new Point(0, getHeight()), new Color(150, 150, 150, 220));
        g2.setPaint(grad);
        g2.fillOval(0, 0, 25, 25);

        g2.dispose();
    }

}
