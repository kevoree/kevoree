/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.jdesktop.swingx.JXTitledSeparator;
import org.kevoree.tools.ui.framework.ErrorHighlightableElement;
import org.kevoree.tools.ui.framework.SelectElement;
import org.kevoree.tools.ui.framework.ThreePartRoundedPanel;
import org.kevoree.tools.ui.framework.TitledElement;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author ffouquet
 */
public class IconPanel extends JPanel implements SelectElement, TitledElement, ErrorHighlightableElement {

    JXTitledSeparator titlebar = new JXTitledSeparator();
    private BufferedImage image;

    public IconPanel(String imgPath) {
        setLayout(new BorderLayout());
        try {
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imgPath));
        } catch (IOException ex) {
            System.out.println(ex);
        }
        titlebar.setForeground(Color.WHITE);
        titlebar.setHorizontalAlignment(SwingConstants.CENTER);
        setSize(image.getWidth(),image.getHeight());
        setPreferredSize(getSize());
        add(titlebar,BorderLayout.CENTER);
    }

    @Override
    public void setTitle(String title) {
        titlebar.setTitle(title);
        this.setToolTipText("Component "+title);
    }

    private Boolean selected = false;
    private Boolean active = false;

    @Override
    public void setSelected(Boolean _selected) {
        selected = _selected;
        active = _selected;
    }

    @Override
    public Boolean getSelected() {
        return selected;
    }

    private STATE _state = STATE.NO_ERROR;

    @Override
    public void setState(STATE state) {
        _state = state;
        if (_state.equals(STATE.IN_ERROR)) {
            this.setBackground(new Color(239, 50, 50, 150));
        } else {
            this.setBackground(new Color(0, 0, 0, 200));
        }
    }

    @Override
    public STATE getCurrentState() {
        return _state;
    }


    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

}
