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
package org.kevoree.editor.component.creator.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.Kernel;
import org.kevoree.editor.component.creator.commands.NewModelCommand;
import org.kevoree.editor.component.creator.commands.SaveModelCommand;

/**
 *
 * @author gnain
 */
public class BasicCommandsPanel extends JPanel {

    private Kernel kernel;

    public BasicCommandsPanel(Kernel k) {
        kernel = k;

        add(createNewModelElement());
        add(createSaveModelElement());
    }

    private JButton createNewModelElement() {
        JButton bt = new JButton(new ImageIcon(getClass().getResource("/icons/new_model.png")));
        bt.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                NewModelCommand command = new NewModelCommand();
                command.setKernel(kernel);
                command.execute(null);
            }
        });
        return bt;
    }


    private JButton createSaveModelElement() {
        JButton bt = new JButton(new ImageIcon(getClass().getResource("/icons/save_model.png")));
        bt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                SaveModelCommand command = new SaveModelCommand();
                command.setKernel(kernel);
                command.execute(null);
            }
        });
        return bt;
    }
}
