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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.NetworkProperty;
import org.kevoree.NodeLink;
import org.kevoree.NodeNetwork;
import org.kevoree.framework.KevoreePlatformHelper;

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
        this.setOpaque(false);
        listPanel.setOpaque(false);
        add(listPanel);
        this.refresh();
    }

    public void refresh() {
        DefaultListModel listModel = new DefaultListModel();
        ContainerRoot root = (ContainerRoot) _node.eContainer();
        for (NodeNetwork nn : root.getNodeNetworks()) {
            if (nn.getTarget().equals(_node)) {
                for (NodeLink nl : nn.getLink()) {
                    for(NetworkProperty np : nl.getNetworkProperties()){
                        listModel.addElement(np.getName()+"="+np.getValue());
                    }
                }
            }
        }
        JList list = new JList(listModel);
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(300, 70));
        listPanel.removeAll();
        listPanel.add(pane);
        listPanel.repaint();
        listPanel.revalidate();
    }

    private class NetworkAddFormEditor extends JPanel {

        private ContainerNode _node = null;
        String[] attlistString = {
            org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(),
            org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT(),
            org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_DISPATCHER_PORT()
        };
        JComboBox attlist = new JComboBox(attlistString);
        JTextField value = new JTextField();
        JButton bt_add = new JButton("ADD");

        public NetworkAddFormEditor(ContainerNode node, final NetworkPropertyEditor parent) {
            _node = node;
            this.setOpaque(false);
            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            this.add(attlist);
            value.setColumns(10);
            this.add(value);
            this.add(bt_add);

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
