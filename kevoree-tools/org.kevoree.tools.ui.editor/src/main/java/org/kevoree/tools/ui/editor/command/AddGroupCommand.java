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
package org.kevoree.tools.ui.editor.command;

import org.kevoree.*;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.ModelHelper;
import org.kevoree.tools.ui.framework.elements.GroupPanel;
import scala.util.Random;

import java.awt.*;

public class AddGroupCommand implements Command {

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
        Group newgroup = KevoreeFactory.createGroup();
        GroupType type = (GroupType) kernel.getUifactory().getMapping().get(p);
        newgroup.setTypeDefinition(type);

        //CREATE NEW NAME
        newgroup.setName("group" + Math.abs(random.nextInt()));
        GroupPanel newgrouppanel = kernel.getUifactory().createGroup(newgroup);
        kernel.getModelHandler().getActualModel().addGroups(newgroup);
        kernel.getModelPanel().addGroup(newgrouppanel);

        if ((point.x - newgrouppanel.getPreferredSize().getHeight() / 2 > 0) && (point.y - newgrouppanel.getPreferredSize().getHeight() / 2 > 0)) {
            newgrouppanel.setLocation((int) (point.x - newgrouppanel.getPreferredSize().getHeight() / 2), (int) (point.y - newgrouppanel.getPreferredSize().getWidth() / 2));
        } else {
            newgrouppanel.setLocation(point.x, point.y);
        }


        kernel.getEditorPanel().getPalette().updateTypeValue(ModelHelper.getTypeNbInstance(kernel.getModelHandler().getActualModel(), type), type.getName());
        kernel.getModelHandler().notifyChanged();
    }
}