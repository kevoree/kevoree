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

import org.jdesktop.swingx.JXTaskPane;
import org.kevoree.tools.ui.framework.UITools;
import org.kevoree.tools.ui.framework.elements.ComponentTypePanel;

import javax.swing.*;

/**
 *
 * @author ffouquet
 */
public class ComponentTypeLibraryPalette extends JXTaskPane {

    //private JPanel content = new JPanel();

    private String libName = "";

    public String getLibName(){
        return libName;
    }

    public ComponentTypeLibraryPalette(String title){

        libName = title;

        String name = UITools.formatTitle(title,25);

        this.setTitle(name);
        this.setName(name);
        //this.setLayout(new BorderLayout());
        //add(content);
    }

    public void addComponentTypePanel(JPanel ctp){
        add(ctp);
        repaint();
        revalidate();
    }

 

}
