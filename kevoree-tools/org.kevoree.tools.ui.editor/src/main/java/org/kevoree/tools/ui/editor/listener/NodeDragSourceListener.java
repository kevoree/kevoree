/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.kevoree.ContainerNode;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.NodePanel;
import org.kevoree.tools.ui.framework.listener.InstanceDragSourceListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseListener;

/**
 * implementation of the drag source listener for the Dnd of a component
 *
 * @author francoisfouquet
 */
public class NodeDragSourceListener extends DragSourceAdapter implements DragSourceMotionListener, DragGestureListener {

    private Container sourceNode = null;
    private KevoreeUIKernel kernel;
    public NodePanel flightNode;
    private DragSource dragSource;
    private DragGestureEvent dragOriginEvent;
    private Transferable transferable;
    private Point origin2;
    private DropTarget flightNodeDropTarget = null;
    public DragGestureRecognizer dgr = null;


    public NodeDragSourceListener(NodePanel _ct, KevoreeUIKernel _kernel) {
        //this.panel = _panel;
        this.flightNode = _ct;
        this.kernel = _kernel;
        this.dragSource = DragSource.getDefaultDragSource();

        for (DragSourceMotionListener li : dragSource.getDragSourceMotionListeners()) {
            if (li instanceof InstanceDragSourceListener) {
                if (((InstanceDragSourceListener) li).flightComponent.equals(flightNode)) {
                    try {
                        dragSource.removeDragSourceListener((DragSourceListener) li);
                        dragSource.removeDragSourceMotionListener((DragSourceMotionListener) li);
                        ((InstanceDragSourceListener)li).dgr.resetRecognizer();
                        ((InstanceDragSourceListener)li).dgr.removeDragGestureListener((DragGestureListener) li);
                        flightNode.removeMouseListener((MouseListener)((InstanceDragSourceListener)li).dgr);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        for (DragSourceMotionListener li : dragSource.getDragSourceMotionListeners()) {
            if (li instanceof org.kevoree.tools.ui.editor.listener.NodeDragSourceListener) {
                if (((NodeDragSourceListener) li).flightNode.equals(flightNode)) {
                    try {
                        dragSource.removeDragSourceListener((DragSourceListener) li);
                        dragSource.removeDragSourceMotionListener((DragSourceMotionListener) li);
                        ((NodeDragSourceListener)li).dgr.resetRecognizer();
                        ((NodeDragSourceListener)li).dgr.removeDragGestureListener((DragGestureListener) li);
                        flightNode.removeMouseListener((MouseListener)((NodeDragSourceListener)li).dgr);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }




        dgr = this.dragSource.createDefaultDragGestureRecognizer((Component) this.flightNode, DnDConstants.ACTION_MOVE, this);
        dragSource.addDragSourceMotionListener(this);
        transferable = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[0];
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor arg0) {
                return true;
            }

            @Override
            public Object getTransferData(DataFlavor arg0) {
                return flightNode;
            }
        };

    }

    /**
     * callback when the mouse is moved
     *
     * @param dsde
     */
    @Override
    public void dragMouseMoved(DragSourceDragEvent dsde) {
        if (dsde.getDragSourceContext().getComponent().equals(this.flightNode)) {

            Point p = dsde.getLocation();
            Point p2 = (Point) p.clone();
            SwingUtilities.convertPointFromScreen(p2, kernel.getModelPanel());

            //tempPanel.setLocation(new Point(p2.x - origin2.x, p2.y - origin2.y));
            // kernel.getModelPanel().setFlightObject(flightComponent, new Point(p2.x - origin2.x, p2.y - origin2.y));

            flightNode.setBounds(p2.x - origin2.x, p2.y - origin2.y, flightNode.getWidth(), flightNode.getHeight());
            kernel.getModelPanel().repaint();
        }
    }

    /**
     * callback when the DnD is finished
     *
     * @param dsde
     */
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        if (!dsde.getDropSuccess()) {
            sourceNode.add(this.flightNode);
        }
        sourceNode.repaint();
        kernel.getModelPanel().unsetFlightObject(flightNode);
        ((Component) this.flightNode).setDropTarget(flightNodeDropTarget);
        kernel.getModelPanel().repaint();
        kernel.getModelPanel().revalidate();
    }

    /**
     * callback when a DnD begining is detected
     *
     * @param dge
     */
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        sourceNode = this.flightNode.getParent();
        dragOriginEvent = dge;
        Point origin = dragOriginEvent.getDragOrigin();
        origin2 = (Point) origin.clone();
        dragSource.startDrag(dragOriginEvent, DragSource.DefaultLinkDrop, transferable, this);
        sourceNode.remove(this.flightNode);
        kernel.getModelPanel().setFlightObject(this.flightNode);
        flightNodeDropTarget = ((Component) this.flightNode).getDropTarget();
        ((Component) this.flightNode).setDropTarget(null);


        kernel.getModelPanel().repaint();
        kernel.getModelPanel().revalidate();
    }
}
