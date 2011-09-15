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
package org.kevoree.tools.ui.editor.panel;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.*;
import org.kevoree.tools.ui.editor.command.LoadNewLibCommandUI;
import org.kevoree.tools.ui.editor.widget.JCommandButton;

/**
 *
 * @author ffouquet
 */
public class CommandPanel extends JPanel {

    public CommandPanel(KevoreeUIKernel kernel) {
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        /*
        JCommandButton btAddHub = new JCommandButton("Add hub");
        AddHubCommand btAddHubCommand = new AddHubCommand();
        btAddHubCommand.setKernel(kernel);
        btAddHub.setCommand(btAddHubCommand);
         */
        JCommandButton btAddNode = new JCommandButton("Add node");
        AddNodeCommand btAddNodeCommand = new AddNodeCommand();
        btAddNodeCommand.setKernel(kernel);
        btAddNode.setCommand(btAddNodeCommand);

        JCommandButton btSave = new JCommandButton("Save");
        SaveActuelModelCommand btSaveCommand = new SaveActuelModelCommand();
        btSaveCommand.setKernel(kernel);
        btSave.setCommand(btSaveCommand);

        JCommandButton btLoadModel = new JCommandButton("LoadModel");
        LoadModelCommandUI btLoadLibCommandUI = new LoadModelCommandUI();
        btLoadLibCommandUI.setKernel(kernel);
        btLoadModel.setCommand(btLoadLibCommandUI);

        
        JCommandButton btClearModel = new JCommandButton("ClearModel");
        ClearModelCommand btClearModelCommand = new ClearModelCommand();
        btClearModelCommand.setKernel(kernel);
        btClearModel.setCommand(btClearModelCommand);
         
        
        JCommandButton btLoadLib = new JCommandButton("LoadLib");
        LoadNewLibCommandUI btLoadLibCommand = new LoadNewLibCommandUI();
        btLoadLibCommand.setKernel(kernel);
        btLoadLib.setCommand(btLoadLibCommand);
        
        
        JCommandButton btRefresh = new JCommandButton("Refresh Model");
        RefreshModelCommand loadModel = new RefreshModelCommand();
        loadModel.setKernel(kernel);
        btRefresh.setCommand(loadModel);


        // add(btAddHub);
        add(btAddNode);
        add(btSave);
        add(btLoadModel);
        add(btLoadLib);
        add(btClearModel);
        add(btRefresh);


    }
}
