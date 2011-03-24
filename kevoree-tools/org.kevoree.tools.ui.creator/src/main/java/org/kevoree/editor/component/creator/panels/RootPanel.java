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
package org.kevoree.editor.component.creator.panels;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.kevoree.editor.component.creator.Kernel;

/**
 *
 * @author gnain
 */
public class RootPanel extends JPanel {

    private Kernel kernel;
    private JSplitPane splitPane;
    private JPanel left, right;

    public RootPanel(Kernel k) {
        kernel = k;
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        left = new JPanel();
        
        right = new JPanel();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
    }

    private void layoutComponents() {

        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.add(kernel.getBasicCommandsPanel());
        left.add(kernel.getPalette());

        right.setLayout(new BorderLayout());
        right.add(kernel.getModelPanel(), BorderLayout.CENTER);

        splitPane.setLeftComponent(left);
        
        splitPane.setRightComponent(right);

        //splitPane.setDividerLocation(left.getPreferredSize().getWidth());


        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

    }
}
