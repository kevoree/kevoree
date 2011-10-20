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
package org.kevoree.tools.ui.editor.property;

import com.explodingpixels.macwidgets.plaf.HudLabelUI;
import com.explodingpixels.macwidgets.plaf.HudTextFieldUI;
import org.kevoree.MBinding;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.RemoveBindingCommand;
import org.kevoree.tools.ui.editor.command.RemoveGroupBindingCommand;
import org.kevoree.tools.ui.editor.widget.JCommandButton;
import org.kevoree.tools.ui.editor.widget.TempGroupBinding;
import org.kevoree.tools.ui.framework.elements.Binding;

import javax.swing.*;

/**
 * User: ffouquet
 * Date: 02/07/11
 * Time: 14:31
 */
public class GroupBindingPropertyEditor extends JPanel {

    private Binding gui = null;
    protected KevoreeUIKernel kernel;
    private TempGroupBinding mbinding;
    public void addCenter(JComponent p) {
        this.add(p);
    }

    public GroupBindingPropertyEditor(TempGroupBinding mb, KevoreeUIKernel _kernel) {
        this.setOpaque(false);
        this.setBorder(null);
        mbinding = mb;
        kernel = _kernel;
        gui = (Binding) kernel.getUifactory().getMapping().get(mbinding);
        JPanel p = new JPanel(new SpringLayout());
        p.setBorder(null);
        p.setOpaque(false);
        JLabel l = new JLabel("Name", JLabel.TRAILING);
        l.setUI(new HudLabelUI());
        p.add(l);
        l.setText(mbinding.getOriginGroup().getName() + " => " + mbinding.getTargetNode().getName());

        this.addCenter(p);
        SpringUtilities.makeCompactGrid(p,
                1, 1, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad



        JCommandButton btDelete = new JCommandButton("Delete");
        RemoveGroupBindingCommand removecmd = new RemoveGroupBindingCommand(mbinding);
        removecmd.setKernel(kernel);
        btDelete.setCommand(removecmd);
        this.addCenter(btDelete);




    }
}
