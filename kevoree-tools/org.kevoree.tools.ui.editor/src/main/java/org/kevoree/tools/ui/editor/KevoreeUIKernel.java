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
/* $Id: KevoreeUIKernel.java 13469 2010-11-14 16:09:27Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */

package org.kevoree.tools.ui.editor;

import org.kevoree.tools.ui.editor.command.CheckCurrentModel;
import org.kevoree.tools.ui.editor.panel.KevoreeEditorPanel;
import org.kevoree.tools.ui.framework.elements.ModelPanel;


/**
 *
 * @author ffouquet
 */
public class KevoreeUIKernel {

    public KevoreeUIKernel(){
        uifactory = new KevoreeUIFactory(this);
        modelHandler = new Art2Handler(this);
        modelPanel = uifactory.createModelPanelUI(modelHandler.getActualModel());
        //Art2Cluster.start();

        //INIT PERIODIQUE COMMAND

        CheckCurrentModel checker = new CheckCurrentModel();
        checker.setKernel(this);
        modelHandler.addListenerCommand(checker);

    }

    private KevoreeUIFactory uifactory;
    private ModelPanel modelPanel;

    private Art2Handler modelHandler;
    private KevoreeEditorPanel editorPanel;

    public void setEditorPanel(KevoreeEditorPanel editorPanel) {
        this.editorPanel = editorPanel;
    }

    public KevoreeEditorPanel getEditorPanel() {
        return editorPanel;
    }

    public Art2Handler getModelHandler() {
        return modelHandler;
    }

    public ModelPanel getModelPanel() {
        return modelPanel;
    }

    public KevoreeUIFactory getUifactory() {
        return uifactory;
    }

}
