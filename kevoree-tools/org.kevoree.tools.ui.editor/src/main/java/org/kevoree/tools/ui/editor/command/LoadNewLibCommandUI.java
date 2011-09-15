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
/* $Id: LoadNewLibCommand.java 13269 2010-11-02 14:24:49Z hrambelo $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.tools.ui.editor.KevoreeUIKernel;

import javax.swing.*;

/**
 * @author ffouquet
 */
public class LoadNewLibCommandUI implements Command {

    private JFileChooser filechooser = new JFileChooser();

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    private KevoreeUIKernel kernel;

    @Override
    public void execute(Object p) {
        int wayOut = filechooser.showOpenDialog(kernel.getModelPanel());

        if (wayOut == JFileChooser.APPROVE_OPTION && filechooser.getSelectedFile() != null) {

            LoadNewLibCommand loadLibCommand = new LoadNewLibCommand();
            loadLibCommand.setKernel(kernel);
            loadLibCommand.execute(filechooser.getSelectedFile().getAbsoluteFile());


        }
    }

}
