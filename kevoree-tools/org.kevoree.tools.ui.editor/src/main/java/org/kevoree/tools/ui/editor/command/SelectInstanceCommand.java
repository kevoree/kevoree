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
/* $Id: SelectInstanceCommand.java 12827 2010-10-07 09:28:51Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import javax.swing.JPanel;
import org.kevoree.Instance;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.SelectElement;

/**
 *
 * @author ffouquet
 */
public class SelectInstanceCommand implements Command {

    private KevoreeUIKernel kernel;
    private static SelectElement alreadySelected = null;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void execute(Object p) {

        Object bObject = kernel.getUifactory().getMapping().get(p);

        if (bObject instanceof org.kevoree.Instance) {

            org.kevoree.Instance instance = (Instance) bObject;
            
            SelectElement component = (SelectElement) p;
            if (alreadySelected != null && alreadySelected != component ) {
                alreadySelected.setSelected(false);
            }
            alreadySelected = component;


            component.setSelected(!component.getSelected());

            if (component.getSelected()) {
                kernel.getEditorPanel().showPropertyFor((JPanel) component);
            } else {
                kernel.getEditorPanel().unshowPropertyEditor();
            }

            /* 
            List<org.kevoree.MBinding> relatedbinding = Art2Utility.getRelatedBinding(instance, kernel.getModelHandler().getActualModel());
            for (org.kevoree.MBinding b : relatedbinding) {
            Binding belem = (Binding) kernel.getUifactory().getMapping().get(b);
            belem.setSelected(!belem.getSelected());
            }
             */
            kernel.getModelPanel().repaint();
            kernel.getModelPanel().revalidate();
        }
    }
}
