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

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.AddMBindingCommand;
import org.kevoree.tools.ui.framework.elements.ChannelPanel;

/**
 * implementation of the target listener
 * @author francoisfouquet
 */
public class HubDragTargetListener extends DropTarget {

    KevoreeUIKernel kernel;
    ChannelPanel target;

    /**
     * constructor
     * @param _p the table view panel
     * @param _target the view of the component
     */
    public HubDragTargetListener(ChannelPanel _target, KevoreeUIKernel _kernel) {
        kernel = _kernel;
        target = _target;
    }

    /**
     * callback when DnD is finished
     * @param arg0
     */
    @Override
    public void drop(DropTargetDropEvent arg0) {
        AddMBindingCommand command = new AddMBindingCommand();
        command.setKernel(kernel);
        command.setTarget(target);
        try {
            command.execute(arg0.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType)));
        } catch (Exception ex) {
            Logger.getLogger(HubDragTargetListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        kernel.getModelPanel().repaint();
        kernel.getModelPanel().revalidate();
        arg0.dropComplete(true);
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
