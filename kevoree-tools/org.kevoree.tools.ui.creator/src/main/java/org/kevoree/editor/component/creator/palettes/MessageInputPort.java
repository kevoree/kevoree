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
package org.kevoree.editor.component.creator.palettes;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author gnain
 */
public class MessageInputPort extends Art2Element {

    private JPanel graphicalElement;

    public MessageInputPort() {
        graphicalElement = new Round();
    }


    @Override
    public JPanel getGraphicalElement() {
        return graphicalElement;
    }

    @Override
    public String getElementName() {
        return "Message Input Port";
    }

    private class Round extends JPanel {

        public Round() {
            setOpaque(false);
            setPreferredSize(new Dimension(17,17));
        }

        @Override
        public void paint(Graphics grphcs) {
            super.paint(grphcs);
            if(grphcs instanceof Graphics2D) {
                Graphics2D g = (Graphics2D)grphcs;
                g.fillOval(0, 0, 15, 15);
            }
        }
    }
}
