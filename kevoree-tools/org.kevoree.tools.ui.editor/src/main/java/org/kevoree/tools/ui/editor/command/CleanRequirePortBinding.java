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
/* $Id: CleanRequirePortBinding.java 12431 2010-09-15 14:52:41Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.ComponentInstance;
import org.kevoree.Port;
import java.util.ArrayList;
import java.util.List;
import org.kevoree.MBinding;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.Binding;
import org.kevoree.tools.ui.framework.elements.PortPanel;

/**
 *
 * @author ffouquet
 */
public class CleanRequirePortBinding implements Command {

    private KevoreeUIKernel kernel;
    private PortPanel portpanel;

    public void setPortpanel(PortPanel portpanel) {
        this.portpanel = portpanel;
    }


    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void execute(Object p) {
        Port port = (Port) kernel.getUifactory().getMapping().get(portpanel);
        ComponentInstance component = (ComponentInstance) port.eContainer();
        if(component.getRequired().contains(port)){
            List<MBinding> portBindings = new ArrayList();
            for(MBinding b : kernel.getModelHandler().getActualModel().getMBindings()){
                if(b.getPort().equals(port)){
                    portBindings.add(b);
                }
            }
            for(MBinding b : portBindings){
                Binding bp = (Binding) kernel.getUifactory().getMapping().get(b);
                kernel.getModelPanel().removeBinding(bp);
                kernel.getModelHandler().getActualModel().getMBindings().remove(b);
            }
        }
    }

}
