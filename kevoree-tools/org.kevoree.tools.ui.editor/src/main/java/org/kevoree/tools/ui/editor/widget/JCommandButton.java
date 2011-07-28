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
/* $Id: JCommandButton.java 11976 2010-08-03 06:35:49Z dvojtise $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */

package org.kevoree.tools.ui.editor.widget;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

import com.explodingpixels.macwidgets.plaf.HudButtonUI;
import org.kevoree.tools.ui.editor.command.Command;

/**
 *
 * @author ffouquet
 */
public class JCommandButton extends JButton {

    private Command command ;

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public JCommandButton(final String title){
        this.setText(title);
        this.setOpaque(false);
        this.setUI(new HudButtonUI());
        this.addMouseListener(new MouseAdapter() {
        @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                command.execute(title);
            }
        });

    }

}
