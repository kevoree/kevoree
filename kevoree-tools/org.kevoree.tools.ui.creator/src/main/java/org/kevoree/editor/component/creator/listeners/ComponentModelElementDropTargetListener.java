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
package org.kevoree.editor.component.creator.listeners;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.kevoree.editor.component.creator.Kernel;
import org.kevoree.editor.component.creator.commands.CreateLibraryCommand;
import org.kevoree.editor.component.creator.model.ComponentModelElement;
import org.kevoree.editor.component.creator.model.LibraryModelElement;
import org.kevoree.editor.component.creator.palettes.Component;
import org.kevoree.editor.component.creator.palettes.MessageInputPort;
import org.kevoree.editor.component.creator.panels.ModelPanel;

/**
 *
 * @author gnain
 */
public class ComponentModelElementDropTargetListener extends DropTargetAdapter {

    private DropTarget dropTarget;
    private JPanel panel;
    private JPanel flyingComponent;
    private Kernel kernel;

    public ComponentModelElementDropTargetListener(Kernel k, ComponentModelElement element) {
        this.panel = element.getGraphicalRepresentation();
        this.kernel = k;
        dropTarget = new DropTarget(panel, DnDConstants.ACTION_COPY, this, true, null);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        try {
            super.dragOver(dtde);
            // System.out.println("DragOver");
            Transferable trans = dtde.getTransferable();
            if (trans.isDataFlavorSupported(TransferableArt2Element.art2ElementFlavor)) {
                if (dtde.getTransferable().getTransferData(TransferableArt2Element.art2ElementFlavor) instanceof MessageInputPort) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            } else if (trans.isDataFlavorSupported(TransferableArt2ModelElement.graphicalElementFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            } else {
                System.out.println("TransferableObject unknown::" + trans.getClass());
                dtde.rejectDrag();
            }
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(ComponentModelElementDropTargetListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ComponentModelElementDropTargetListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void drop(DropTargetDropEvent event) {
        try {
            System.out.println("Droped");
            if (event.isLocalTransfer()) {
                System.out.println("LocalTransfer");

                Transferable trans = event.getTransferable();
                if (trans.isDataFlavorSupported(TransferableArt2Element.art2ElementFlavor)) {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    /*
                    CreateLibraryCommand command = new CreateLibraryCommand();
                    command.setKernel(kernel);
                    command.setPoint(event.getLocation());
                    command.execute(null);
                    */
                    event.dropComplete(true);
                    return;

                } else if (trans.isDataFlavorSupported(TransferableArt2ModelElement.graphicalElementFlavor)) {
                    event.acceptDrop(DnDConstants.ACTION_MOVE);
                    event.dropComplete(true);
                    kernel.getModelPanel().repaint();
                    return;
                } else {
                    System.out.println("TransferableObject unknown::" + trans.getClass());
                    event.rejectDrop();
                }
            }
            event.rejectDrop();
        } catch (Exception e) {
            e.printStackTrace();
            event.rejectDrop();
        }
    }
}
