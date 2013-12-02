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
package org.kevoree.tools.ui.editor.menus;

import org.kevoree.tools.ui.editor.KevoreeStore;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.*;

import javax.swing.*;

public class ModelMenu extends JMenu {

    private KevoreeUIKernel kernel;
    KevoreeStore store = new KevoreeStore();
    private JMenu subLibraryMenu = null;


    public ModelMenu(KevoreeUIKernel kernel) {
        super("Model");
        this.kernel = kernel;

        add(createClearItem());
        add(createLoadLibraryItem());
        add(createCheckModelItem());
        add(clearCoreLibraryCache());

        subLibraryMenu = store.buildModelMenu(kernel);
        add(subLibraryMenu);
    }

    private JMenuItem createClearItem() {
        JMenuItem clearModel = new JMenuItem("Clear");
        ClearModelCommand cmdCM = new ClearModelCommand();
        cmdCM.setKernel(kernel);
        clearModel.addActionListener(new CommandActionListener(cmdCM));
        return clearModel;
    }

    private JMenuItem createLoadLibraryItem() {
        JMenuItem mergeLib = new JMenuItem("Load Library");
        LoadNewLibCommandUI cmdLL = new LoadNewLibCommandUI();
        cmdLL.setKernel(kernel);
        mergeLib.addActionListener(new CommandActionListener(cmdLL));
        return mergeLib;
    }

    private JMenuItem clearCoreLibraryCache() {
        JMenuItem mergeLib = new JMenuItem("Reload Library Cache");
        Command cmdLL = new Command() {

            @Override
            public void execute(Object p) {
                remove(subLibraryMenu);
                subLibraryMenu = store.buildModelMenu(kernel);
                add(subLibraryMenu);
            }
        };
        mergeLib.addActionListener(new CommandActionListener(cmdLL));
        return mergeLib;
    }


    private JMenuItem createCheckModelItem() {
        JMenuItem checkModel = new JMenuItem("Check");
        CheckCurrentModel cmdCheck = new CheckCurrentModel();
        cmdCheck.setKernel(kernel);
        checkModel.addActionListener(new CommandActionListener(cmdCheck));
        return checkModel;
    }


}
