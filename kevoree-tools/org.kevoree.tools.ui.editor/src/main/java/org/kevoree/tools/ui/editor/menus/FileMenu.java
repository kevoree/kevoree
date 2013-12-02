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

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.*;

import javax.swing.*;

public class FileMenu extends JMenu {

    private KevoreeUIKernel kernel;

    public FileMenu(KevoreeUIKernel kernel) {
        super("File");
        this.kernel = kernel;

        add(createOpenItem());
        add(createMergeItem());
        add(createOpenKevsItem());
        add(createOpenFromNode());
        add(createSaveItem());
        add(createSaveJSONItem());
        add(createSaveAsImageItem());
        add(createSaveAsSvgItem());
       add(createSaveAsKevsItem());
        add(createRefreshItem());
    }

    private JMenuItem createOpenItem() {
        JMenuItem fileOpen = new JMenuItem("Open");
        LoadModelCommandUI loadModelCommand = new LoadModelCommandUI();
        loadModelCommand.setKernel(kernel);
        fileOpen.addActionListener(new CommandActionListener(loadModelCommand));
        return fileOpen;
    }

    private JMenuItem createMergeItem() {
        JMenuItem fileMerge = new JMenuItem("Merge");
        MergeModelCommandUI cmdLMerge = new MergeModelCommandUI();
        cmdLMerge.setKernel(kernel);
        fileMerge.addActionListener(new CommandActionListener(cmdLMerge));
        return fileMerge;
    }

    private JMenuItem createOpenKevsItem() {
        /* Load command */
        JMenuItem kevsOpen = new JMenuItem("Open from KevScript");
        LoadKevScriptCommandUI cmdKevOpen = new LoadKevScriptCommandUI();
        cmdKevOpen.setKernel(kernel);
        kevsOpen.addActionListener(new CommandActionListener(cmdKevOpen));
        return kevsOpen;
    }

    private JMenuItem createOpenFromNode() {
        /* Load remote ui command */
        JMenuItem fileOpenRemote = new JMenuItem("Open from node");
        LoadRemoteModelUICommand cmdLMORemote2 = new LoadRemoteModelUICommand();
        cmdLMORemote2.setKernel(kernel);
        fileOpenRemote.addActionListener(new CommandActionListener(cmdLMORemote2));
        return fileOpenRemote;
    }

    private JMenuItem createSaveItem() {
        JMenuItem fileSave = new JMenuItem("Save");
        SaveActuelModelCommand cmdSM = new SaveActuelModelCommand();
        cmdSM.setKernel(kernel);
        fileSave.addActionListener(new CommandActionListener(cmdSM));
        return fileSave;
    }

    private JMenuItem createSaveJSONItem() {
        JMenuItem fileSave = new JMenuItem("SaveAsJSON");
        SaveActuelModelJSONCommand cmdSM = new SaveActuelModelJSONCommand();
        cmdSM.setKernel(kernel);
        fileSave.addActionListener(new CommandActionListener(cmdSM));
        return fileSave;
    }

    private JMenuItem createSaveAsKevsItem() {
        JMenuItem fileSave = new JMenuItem("SaveAsKevScript");
        SaveAsKevScript cmdSM = new SaveAsKevScript();
        cmdSM.setKernel(kernel);
        fileSave.addActionListener(new CommandActionListener(cmdSM));
        return fileSave;
    }

    private JMenuItem createSaveAsImageItem() {
        JMenuItem saveImage = new JMenuItem("SaveAsImage");
        ExportModelImage cmdImage = new ExportModelImage();
        cmdImage.setKernel(kernel);
        saveImage.addActionListener(new CommandActionListener(cmdImage));
        return saveImage;
    }

    private JMenuItem createSaveAsSvgItem() {
        JMenuItem saveSVG = new JMenuItem("SaveAsSVG");
        ExportModelSVGImage cmdImageSVG = new ExportModelSVGImage();
        cmdImageSVG.setKernel(kernel);
        saveSVG.addActionListener(new CommandActionListener(cmdImageSVG));
        return saveSVG;
    }

    private JMenuItem createRefreshItem() {
        JMenuItem refresh = new JMenuItem("Refresh");
        RefreshModelCommand cmdRM = new RefreshModelCommand();
        cmdRM.setKernel(kernel);
        refresh.addActionListener(new CommandActionListener(cmdRM));
        return refresh;
    }


}
