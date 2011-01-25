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
package org.kevoree.tools.ui.editor.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.kevoree.tools.ui.editor.command.Command;

/**
 *
 * @author ffouquet
 */
public class CommandMouseListener extends MouseAdapter {

    private Command leftClickCommand, rightClickCommand;

    public void setLeftClickCommand(Command command) {
        this.leftClickCommand = command;
    }

    public void setRightClickCommand(Command command) {
        this.rightClickCommand = command;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("MouseListener::MouseClicked::"+e.getButton());
        switch(e.getButton()) {
            case MouseEvent.BUTTON1 : {
                if(leftClickCommand != null) {
                    System.out.println("MouseListener::MouseClicked::executeLeftClickCommand");
                    leftClickCommand.execute(e.getComponent());
                }
            }break;
            case MouseEvent.BUTTON3 : {
                if(rightClickCommand != null) {
                    System.out.println("MouseListener::MouseClicked::executeRightClickCommand");
                    rightClickCommand.execute(e.getComponent());
                }
            }break;
        }
        
    }
}
