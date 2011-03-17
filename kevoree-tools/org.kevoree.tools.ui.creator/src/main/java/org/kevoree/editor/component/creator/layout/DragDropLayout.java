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
package org.kevoree.editor.component.creator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.io.Serializable;

/**
 *
 * @author ffouquet
 */
public class DragDropLayout implements LayoutManager, Serializable {

    //private List<Component> l = new ArrayList();
    @Override
    public void addLayoutComponent(String name, Component comp) {
        //l.add(comp);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        //l.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension();
    }

    @Override
    public void layoutContainer(Container p) {

        int n = p.getComponents().length;
        for (int i = 0; i < n; i++) {
            Component c = p.getComponent(i);
            if(c.isVisible()){
                Dimension d = c.getPreferredSize();
                c.setSize(d.width, d.height);
                c.setBounds(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            }
        }
        
    }
}
