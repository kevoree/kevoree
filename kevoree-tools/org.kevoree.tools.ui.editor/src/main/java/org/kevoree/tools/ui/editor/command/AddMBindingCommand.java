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
/* $Id: AddMBindingCommand.java 12431 2010-09-15 14:52:41Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import org.kevoree.Channel;
import org.kevoree.KevoreeFactory;
import org.kevoree.MBinding;
import org.kevoree.Port;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.ChannelPanel;
import org.kevoree.tools.ui.framework.elements.PortPanel;

/**
 *
 * @author ffouquet
 */
public class AddMBindingCommand implements Command {

    private KevoreeUIKernel kernel;
    private ChannelPanel target;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    public void setTarget(ChannelPanel target) {
        this.target = target;
    }

    @Override
    public void execute(Object p) {
        if (p instanceof PortPanel) {
            PortPanel fromPanel = (PortPanel) p;
           // if (fromPanel.getNature().equals(PortNature.MESSAGE)) {
                Port fromPort = (Port) kernel.getUifactory().getMapping().get(fromPanel);
                Channel targetHub = (Channel) kernel.getUifactory().getMapping().get(target);

                //TODO CHECK CONSISTENCY
                MBinding newb = KevoreeFactory.createMBinding();
                newb.setPort(fromPort);
                newb.setHub(targetHub);
                org.kevoree.tools.ui.framework.elements.Binding uib = kernel.getUifactory().createMBinding(newb);

                kernel.getModelHandler().getActualModel().addMBindings(newb);
                kernel.getModelPanel().addBinding(uib);

            kernel.getModelHandler().notifyChanged();


          //  }

        }




    }
}
