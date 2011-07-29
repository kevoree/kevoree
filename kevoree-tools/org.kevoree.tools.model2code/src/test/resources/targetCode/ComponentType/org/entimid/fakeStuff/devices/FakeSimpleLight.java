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
package org.entimid.fakeStuff.devices;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.entimid.framework.service.OnOffService;
import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;

/**
 *
 * @author ffouquet
 */
@Provides({
    @ProvidedPort(name = "on", type = PortType.MESSAGE),
    @ProvidedPort(name = "off", type = PortType.MESSAGE),
    @ProvidedPort(name = "onoff", className = OnOffService.class)
})
@DictionaryType({
    @DictionaryAttribute(name = "param1"),
    @DictionaryAttribute(name = "param2", defaultValue = "defaultValue", optional = true)
})
@ComponentType
public class FakeSimpleLight extends AbstractFakeStuffComponent {

    public FakeSimpleLight() {
        frame = new MyFrame(Color.RED);
    }

    @Start
    public void start() {

        for (String s : getDictionary().keySet()) {
            System.out.println("key=" + s + "=" + getDictionary().get(s).toString());
        }

        logger = Logger.getLogger(this.getClass().getName());
        frame.setVisible(true);
        state = false;

        MessagePort log = getPortByName("log", MessagePort.class);
        if (log != null) {
            log.process("INIT");
        }
    }

    @Stop
    public void stop() {
        frame.dispose();
        frame = null;
    }

    @Update
    public void update() {
        //TODO check new values in dictionnary
    }

    @Ports({
        @Port(name = "on", method = "process"),
        @Port(name = "onoff", method = "on")
    })
    public void lightOn(Object o) {
        frame.setColor(Color.green);
        state = true;

        MessagePort log = getPortByName("log", MessagePort.class);
        if (log != null) {
            log.process("change color");
        }
    }

    @Ports({
        @Port(name = "off", method = "process"),
        @Port(name = "onoff", method = "off")
    })
    public void lightOff(Object o) {
        frame.setColor(Color.red);
        state = false;

        MessagePort log = getPortByName("log", MessagePort.class);
        if (log != null) {
            log.process("change color");
        }
    }
    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 300;
    private static Logger logger;
    private MyFrame frame;
    private Boolean state = false;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }


    private static class MyFrame extends JFrame {

        private Color c;

        public MyFrame(Color c) {
            super("Couleur " + c.toString());
            this.c = c;
            setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
            pack();
        }

        public void paint(Graphics g) {
            if (g instanceof Graphics2D) {
                Graphics2D g2d = Graphics2D.class.cast(g);
                g2d.setColor(c);
                g2d.fillOval(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
            } else {
                logger.warning("Graphics are not 2D. Instance of:" + g.getClass());
            }
        }

        public final void setColor(Color c) {
            this.c = c;
            repaint();
            Logger.getLogger(this.getName()).info("SetColor " + c.toString());
        }
    }
}
