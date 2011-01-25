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
package org.kevoree.tools.ui.editor.listener;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.PortPanel;

/**
 * implementation of the target listener
 * @author francoisfouquet
 */
public class PortDragTargetListener extends DropTarget {

    KevoreeUIKernel kernel;
    PortPanel target;

    /**
     * constructor
     * @param _p the table view panel
     * @param _target the view of the component
     */
    public PortDragTargetListener(PortPanel _target, KevoreeUIKernel _kernel) {
        kernel = _kernel;
        target = _target;
    }

    /**
     * callback when DnD is finished
     * @param arg0
     */
    @Override
    public void drop(DropTargetDropEvent arg0) {

        //DEPRECATED BEFORE MBINDING

        /*
        try {
            Object draggedPanel = arg0.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));

            if (target.getNature().equals(PortPanel.PortNature.SERVICE)) {
                AddBindingCommand command = new AddBindingCommand();
                command.setKernel(kernel);
                command.setTarget(target);
                command.execute(draggedPanel);
                kernel.getModelPanel().repaint();
                kernel.getModelPanel().revalidate();
                arg0.dropComplete(true);
            } else {
                arg0.rejectDrop();
            }



        } catch (Exception ex) {
            Logger.getLogger(PortDragTargetListener.class.getName()).log(Level.SEVERE, null, ex);
            arg0.rejectDrop();
        }*/
    }

    /**
     * not implemented
     * @param dtde
     */
    //@Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    /**
     * not implemented
     * @param arg0
     */
    //@Override
    public void dragExit(DropTargetEvent arg0) {
    }

    /**
     * not implemented
     * @param arg0
     */
    //@Override
    public void dragOver(DropTargetDragEvent arg0) {
    }

    /**
     * not implemented
     * @param arg0
     */
    //@Override
    public void dropActionChanged(DropTargetDragEvent arg0) {
    }
}
