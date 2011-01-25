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
/* $Id: AddNodeCommand.java 11975 2010-08-02 16:02:55Z dvojtise $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor.command;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.ModelPanel;
import org.kevoree.tools.ui.framework.elements.NodePanel;

/**
 *
 * @author ffouquet
 */
public class ContextualMenuCommand implements Command {

    private KevoreeUIKernel kernel;
    private JPopupMenu current;
    private JPopupMenu modelPanelMenu, nodePanelMenu;
    private MouseListener menuCloser;

    public ContextualMenuCommand() {

        menuCloser = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (current.isVisible()) {
                    kernel.getModelPanel().removeMouseListener(menuCloser);
                    current.setVisible(false);
                }
            }
        };

        createModelPanelContextualMenu();
        createNodePanelContextualMenu();
    }

    public void setKernel(KevoreeUIKernel kernel) {
        this.kernel = kernel;
    }

    private void createModelPanelContextualMenu() {

        modelPanelMenu = new JPopupMenu();

        JMenuItem addNode = new JMenuItem("Add Node");
        addNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kernel.getModelPanel().removeMouseListener(menuCloser);
                modelPanelMenu.setVisible(false);
                AddNodeCommand addNodeCommand = new AddNodeCommand();
                addNodeCommand.setKernel(kernel);
                addNodeCommand.execute(e);
            }
        });
        modelPanelMenu.add(addNode);

    }


    private void createNodePanelContextualMenu() {

        nodePanelMenu = new JPopupMenu();

        JMenuItem deploy = new JMenuItem("Deploy");
        deploy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kernel.getModelPanel().removeMouseListener(menuCloser);
                nodePanelMenu.setVisible(false);
            }
        });
        nodePanelMenu.add(deploy);

        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kernel.getModelPanel().removeMouseListener(menuCloser);
                nodePanelMenu.setVisible(false);
            }
        });
        nodePanelMenu.add(delete);
    }


    @Override
    public void execute(final Object p) {

        System.out.println("ContextualMenuCommand:: called from " + p.getClass().getSimpleName());
        
        if (p instanceof ModelPanel) {
            System.out.println("RightClick on editor panel");
            kernel.getModelPanel().addMouseListener(menuCloser);
            current = modelPanelMenu;
            Point pointer = MouseInfo.getPointerInfo().getLocation();
            current.setLocation(pointer.x, pointer.y);
            current.setVisible(true);

        } else if (p instanceof NodePanel) {
            System.out.println("RichtClick on node");
            kernel.getModelPanel().addMouseListener(menuCloser);
            current = nodePanelMenu;
            Point pointer = MouseInfo.getPointerInfo().getLocation();
            current.setLocation(pointer.x, pointer.y);
            current.setVisible(true);
        }





        /*
         *
         * *************    TO BE REMOVED *****************
         *
        ContainerNode newnode = org.kevoree.Art2Factory.eINSTANCE.createContainerNode();
        //CREATE NEW NAME
        newnode.setName("node-"+kernel.getModelHandler().getActualModel().getNodes().size());
        NodePanel newnodepanel = kernel.getUifactory().createComponentNode(newnode);
        kernel.getModelHandler().getActualModel().getNodes().add(newnode);
        kernel.getModelPanel().addNode(newnodepanel);
         *
         */
    }
}
