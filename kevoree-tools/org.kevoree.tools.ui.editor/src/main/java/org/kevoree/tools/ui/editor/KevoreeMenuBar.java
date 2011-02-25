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
package org.kevoree.tools.ui.editor;


import org.kevoree.tools.ui.editor.command.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KevoreeMenuBar extends JMenuBar {

    public KevoreeMenuBar(KevoreeUIKernel kernel) {

        JMenu file, model = null;

        file = new JMenu("File");
        JMenuItem fileOpen = new JMenuItem("Open");

        LoadModelCommandUI cmdLM = new LoadModelCommandUI();
        cmdLM.setKernel(kernel);
        fileOpen.addActionListener(new CommandActionListener(cmdLM));
        file.add(fileOpen);
        JMenuItem fileSave = new JMenuItem("Save");
        SaveActuelModelCommand cmdSM = new SaveActuelModelCommand();
        cmdSM.setKernel(kernel);
        fileSave.addActionListener(new CommandActionListener(cmdSM));
        file.add(fileSave);
        JMenuItem refresh = new JMenuItem("Refresh");
        RefreshModelCommand cmdRM = new RefreshModelCommand();
        cmdRM.setKernel(kernel);
        refresh.addActionListener(new CommandActionListener(cmdRM));
        file.add(refresh);

        model = new JMenu("Model");
        JMenuItem addNode = new JMenuItem("Add node");
        AddNodeCommand cmdAN = new AddNodeCommand();
        cmdAN.setKernel(kernel);
        addNode.addActionListener(new CommandActionListener(cmdAN));
        model.add(addNode);
        JMenuItem clearModel = new JMenuItem("Clear");
        ClearModelCommand cmdCM = new ClearModelCommand();
        cmdCM.setKernel(kernel);
        clearModel.addActionListener(new CommandActionListener(cmdCM));
        model.add(clearModel);
        JMenuItem mergeLib = new JMenuItem("Merge Lib");
        LoadNewLibCommand cmdLL = new LoadNewLibCommand();
        cmdLL.setKernel(kernel);
        mergeLib.addActionListener(new CommandActionListener(cmdLL));
        model.add(mergeLib);

        JMenuItem checkModel = new JMenuItem("Check");
        CheckCurrentModel cmdCheck = new CheckCurrentModel();
        cmdCheck.setKernel(kernel);
        checkModel.addActionListener(new CommandActionListener(cmdCheck));
        model.add(checkModel);


        this.add(file);
        this.add(model);


    }

    class CommandActionListener implements ActionListener {

        private Command _command = null;

        public CommandActionListener(Command command) {
            _command = command;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            _command.execute("");
        }
    }


}
