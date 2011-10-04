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
package org.kevoree.tools.ui.editor.property;

import com.explodingpixels.macwidgets.plaf.HudButtonUI;
import com.explodingpixels.macwidgets.plaf.HudComboBoxUI;
import com.explodingpixels.macwidgets.plaf.HudTextFieldUI;
import org.kevoree.*;
import org.kevoree.framework.KevoreePlatformHelper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author ffouquet
 */
public class NetworkPropertyEditor extends JPanel {

    private JPanel add_form_panel = null;
    private ContainerNode _node = null;
    private JPanel listPanel = new JPanel();

    public NetworkPropertyEditor(ContainerNode node) {
        _node = node;
        add_form_panel = new NetworkAddFormEditor(_node, this);
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setBorder(new LineBorder(Color.WHITE));
        add(add_form_panel);
        add_form_panel.setBorder(null);
        this.setOpaque(false);
        listPanel.setOpaque(false);
        listPanel.setBorder(null);
        add(listPanel);
        this.setBorder(null);
        this.refresh();
    }

    public void refresh() {
        DefaultListModel listModel = new DefaultListModel();
        ContainerRoot root = (ContainerRoot) _node.eContainer();
        for (NodeNetwork nn : root.getNodeNetworksForJ()) {
            if (nn.getTarget().equals(_node)) {
                for (NodeLink nl : nn.getLinkForJ()) {
                    for(NetworkProperty np : nl.getNetworkPropertiesForJ()){
                        listModel.addElement(np.getName()+"="+np.getValue());
                    }
                }
            }
        }
        JList list = new JList(listModel);
        JScrollPane pane = new JScrollPane(list);
        pane.setBorder(null);
        pane.setPreferredSize(new Dimension(270, 60));
        listPanel.removeAll();
        listPanel.add(pane);
        listPanel.repaint();
        listPanel.revalidate();
    }

    private class NetworkAddFormEditor extends JPanel {

        private ContainerNode _node = null;
        String[] attlistString = {
            org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()//,
           // org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT(),
           // org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_DISPATCHER_PORT()
        };
        JComboBox attlist = new JComboBox(attlistString);
        JTextField value = new JTextField();
        JButton bt_add = new JButton("Add");

        public NetworkAddFormEditor(ContainerNode node, final NetworkPropertyEditor parent) {
            _node = node;
            attlist.setUI(new HudComboBoxUI());
            value.setUI(new HudTextFieldUI());
            bt_add.setUI(new HudButtonUI());
            this.setBorder(null);

            this.setOpaque(false);
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.add(attlist);
            value.setColumns(10);

            JPanel lineValueAdd = new JPanel();
            lineValueAdd.setOpaque(false);
            lineValueAdd.setLayout(new BoxLayout(lineValueAdd, BoxLayout.LINE_AXIS));
            lineValueAdd.add(value);
            lineValueAdd.add(bt_add);
            this.add(lineValueAdd);

            bt_add.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent me) {
                    KevoreePlatformHelper.updateNodeLinkProp((ContainerRoot) _node.eContainer(), _node.getName(), _node.getName(), attlist.getSelectedItem().toString(), value.getText(), "", 100);
                    parent.refresh();
                }
            });
        }
    }
}
