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
package org.kevoree.tools.ui.editor.menus;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 08/11/12
* (c) 2012 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.OpenKevsShell;

import javax.swing.*;

public class KevsMenu extends JMenu {

    private KevoreeUIKernel kernel;

    public KevsMenu(KevoreeUIKernel kernel) {
        super("KevScript");
        this.kernel = kernel;

        add(createKevsOpenEditorItem());
    }

    private JMenuItem createKevsOpenEditorItem() {
        JMenuItem openEditor = new JMenuItem("Open editor");
        OpenKevsShell cmdOpenKevsGUI = new OpenKevsShell();
        cmdOpenKevsGUI.setKernel(kernel);
        openEditor.addActionListener(new CommandActionListener(cmdOpenKevsGUI));
        return openEditor;
    }
}
