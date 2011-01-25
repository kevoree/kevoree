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
package org.kevoree.library.fakedomo;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;


/**
 *
 * @author ffouquet
 */

@Requires({
    @RequiredPort(name = "on", type=PortType.MESSAGE),
    //@RequiredPort(name = "on2", type=PortType.MESSAGE),
    @RequiredPort(name = "off", type=PortType.MESSAGE)
})
@ComponentType
public class FakeSimpleSwitch extends AbstractFakeStuffComponent {

    private static final int SWITCH_WIDTH = 50;
    private static final int SWITCH_HEIGHT = 100;
    private MyFrame frame = null;

    @Override
    public void start() {
        frame = new MyFrame("on", "off");
        frame.setVisible(true);
    }

    @Override
    public void stop() {
        frame.dispose();
        frame = null;
    }

    private class MyFrame extends JFrame {

        private JButton on, off;
        private String onText;
        private String offText;

        public MyFrame(final String onText, final String offText) {

            this.onText = onText;
            this.offText = offText;
            setPreferredSize(new Dimension(SWITCH_WIDTH, SWITCH_HEIGHT));
            setLayout(new FlowLayout());
            on = new JButton(onText);
            on.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getPortByName("on", MessagePort.class).process(new HashMap<String, String>());
                }
            });

            off = new JButton(offText);
            off.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    getPortByName("off", MessagePort.class).process(new HashMap<String, String>());
                }
            });

            ButtonGroup bg = new ButtonGroup();
            bg.add(on);
            bg.add(off);

            setLayout(new FlowLayout());
            add(on);
            add(off);

            pack();
            setVisible(true);
        }

        @Override
        public void repaint() {
            on.setText(onText);
            off.setText(offText);
            super.repaint();
        }

        /**
         * @param onText the onText to set
         */
        public final void setOnText(String onText) {
            this.onText = onText;
        }

        /**
         * @param offText the offText to set
         */
        public final void setOffText(String offText) {
            this.offText = offText;
        }
    }
}
