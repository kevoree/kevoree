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
package org.kevoree.tools.ui.editor.panel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;

/**
 *
 * @author ffouquet
 */
public class TypeDefinitionPalette extends JScrollPane {

    private JPanel subpanels = new JPanel();
    private List<ComponentTypeLibraryPalette> libPalettes = new ArrayList<ComponentTypeLibraryPalette>();

    public void addTypeDefinitionPanel(JPanel ctp, String libName) {
        ComponentTypeLibraryPalette foundP = null;
        for (ComponentTypeLibraryPalette p : libPalettes) {
            if (p.getName().equals(libName)) {
                foundP = p;
            }
        }
        if (foundP == null) {
            foundP = new ComponentTypeLibraryPalette(libName);
            libPalettes.add(foundP);
            subpanels.add(foundP);
            repaint();
            revalidate();
        }
        foundP.add(ctp);
    }

    public void clear(){
        libPalettes.clear();
        subpanels.removeAll();
    }

    public TypeDefinitionPalette() {
        subpanels.setOpaque(false);
        subpanels.setLayout(new BoxLayout(subpanels, BoxLayout.PAGE_AXIS));
        setOpaque(false);
        //setPreferredSize(new Dimension(200, 400));
        setLayout(new ScrollPaneLayout());
        setViewportView(subpanels);
        setAutoscrolls(true);
    }
}
