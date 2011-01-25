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
/* $Id: AddChannelCommand.java 12827 2010-10-07 09:28:51Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import java.awt.Point;
import org.kevoree.Channel;
import org.kevoree.ChannelType;
import org.kevoree.KevoreeFactory;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.ChannelPanel;
import scala.util.Random;

/**
 *
 * @author ffouquet
 */
public class AddChannelCommand implements Command {

    private KevoreeUIKernel kernel;
    private Random random = new Random();

    private Point point = null;

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    public void setPoint(Point p) {
        this.point = p;
    }

    @Override
    public void execute(Object p) {
        Channel newhub = KevoreeFactory.eINSTANCE.createChannel();
        ChannelType type = (ChannelType) kernel.getUifactory().getMapping().get(p);
        newhub.setTypeDefinition(type);

        //CREATE NEW NAME
        newhub.setName("hub-" + random.nextInt());
        ChannelPanel newhubpanel = kernel.getUifactory().createHub(newhub);
        kernel.getModelHandler().getActualModel().getHubs().add(newhub);
        kernel.getModelPanel().addHub(newhubpanel);

        if ((point.x - newhubpanel.getPreferredSize().getHeight()/2 > 0 ) && (point.y - newhubpanel.getPreferredSize().getHeight()/2 > 0 )  ){
            newhubpanel.setLocation( (int)(point.x - newhubpanel.getPreferredSize().getHeight()/2), (int)(point.y - newhubpanel.getPreferredSize().getWidth()/2));
        } else {
            newhubpanel.setLocation(point.x, point.y);
        }

    }
}
