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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.editor.component.creator;

import javax.swing.JFrame;
import org.kevoree.editor.component.creator.handlers.ModelHandler;
import org.kevoree.editor.component.creator.handlers.ModelMapper;
import org.kevoree.editor.component.creator.panels.BasicCommandsPanel;
import org.kevoree.editor.component.creator.panels.ModelPanel;
import org.kevoree.editor.component.creator.panels.PalettePanel;
import org.kevoree.editor.component.creator.panels.RootPanel;

/**
 *
 * @author gnain
 */
public class StandaloneLauncher {

    public static void main(String[] args) {

        Kernel k = new Kernel();
        k.setModelHandler(new ModelHandler(k));
        k.setModelMapper(new ModelMapper());
        k.setModelPanel(new ModelPanel(k));
        k.setPalette(new PalettePanel());
        k.setBasicCommandsPanel(new BasicCommandsPanel(k));

        JFrame frame = new JFrame("Component Creator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new RootPanel(k));

        frame.pack();
        frame.setVisible(true);
    }

}
