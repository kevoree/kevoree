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

import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.editor.command.AddChannelCommand;
import org.kevoree.tools.ui.editor.command.AddGroupCommand;
import org.kevoree.tools.ui.editor.command.AddNodeCommand;
import org.kevoree.tools.ui.editor.command.LoadNewLibCommand;
import org.kevoree.tools.ui.framework.elements.ChannelTypePanel;
import org.kevoree.tools.ui.framework.elements.GroupTypePanel;
import org.kevoree.tools.ui.framework.elements.ModelPanel;
import org.kevoree.tools.ui.framework.elements.NodeTypePanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * implementation of the target listener
 *
 * @author francoisfouquet
 */
public class ModelDragTargetListener extends DropTarget {

    KevoreeUIKernel kernel;
    ModelPanel target;

    DataFlavor mimeDataFlavour;
    DataFlavor urlDataFlavour;

    /**
     * constructor
     *
     * @param _kernel the table view panel
     * @param _target the view of the component
     */
    public ModelDragTargetListener(ModelPanel _target, KevoreeUIKernel _kernel) {
        kernel = _kernel;
        target = _target;
        try {
            mimeDataFlavour = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
            urlDataFlavour = new DataFlavor("application/x-java-url;class=java.net.URL");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Boolean isDropAccept(Object o) {
        if (o instanceof ChannelTypePanel) {
            return true;
        }
        if (o instanceof GroupTypePanel) {
            return true;
        }
        if (o instanceof NodeTypePanel) {
            return true;
        }

        //otherwise return false / no other type accepted
        return false;
    }

    /**
     * callback when DnD is finished
     *
     * @param arg0
     */
    @Override
    public void drop(DropTargetDropEvent arg0) {

        try {
            
            if (arg0.getTransferable().isDataFlavorSupported(mimeDataFlavour)) {
                Object o = arg0.getTransferable().getTransferData(mimeDataFlavour);
                if (isDropAccept(o)) {
                    if (o instanceof ChannelTypePanel) {
                        AddChannelCommand command = new AddChannelCommand();
                        command.setPoint(arg0.getLocation());
                        command.setKernel(kernel);
                        command.execute(o);
                    }
                    if (o instanceof NodeTypePanel) {
                        AddNodeCommand command = new AddNodeCommand();
                        command.setPoint(arg0.getLocation());
                        command.setKernel(kernel);
                        command.execute(o);
                    }
                    if (o instanceof GroupTypePanel) {
                        AddGroupCommand command = new AddGroupCommand();
                        command.setPoint(arg0.getLocation());
                        command.setKernel(kernel);
                        command.execute(o);
                    }

                    kernel.getModelPanel().repaint();
                    kernel.getModelPanel().revalidate();
                    arg0.dropComplete(true);
                } else {
                    arg0.rejectDrop();
                }
            } else if(arg0.getTransferable().isDataFlavorSupported(urlDataFlavour)){
                arg0.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);//Necessary because of drop from an external source
                Object url = arg0.getTransferable().getTransferData(urlDataFlavour);
                LoadNewLibCommand loadLib = new LoadNewLibCommand();
                loadLib.setKernel(kernel);
                loadLib.execute(url);
                arg0.dropComplete(true);
            } else {
                arg0.rejectDrop();
            }
        } catch (Exception ex) {
            Logger.getLogger(ModelDragTargetListener.class.getName()).log(Level.SEVERE, null, ex);
            arg0.rejectDrop();
        }

    }

    /**
     * not implemented
     *
     * @param dtde
     */
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    /**
     * not implemented
     *
     * @param arg0
     */
    @Override
    public void dragExit(DropTargetEvent arg0) {
    }

    /**
     * not implemented
     *
     * @param arg0
     */
    @Override
    public void dragOver(DropTargetDragEvent arg0) {
        if (kernel.getModelPanel().getFlightObject() != null) {
            kernel.getModelPanel().getFlightObject().setBounds(
                    (int) arg0.getLocation().getX() - (kernel.getModelPanel().getFlightObject().getWidth() / 2),
                    (int) arg0.getLocation().getY() - (kernel.getModelPanel().getFlightObject().getHeight() / 2),
                    kernel.getModelPanel().getFlightObject().getWidth(),
                    kernel.getModelPanel().getFlightObject().getHeight());
            kernel.getModelPanel().repaint();
            //  kernel.getModelPanel().revalidate();
        }
    }

    /**
     * not implemented
     *
     * @param arg0
     */
    @Override
    public void dropActionChanged(DropTargetDragEvent arg0) {
    }
}
