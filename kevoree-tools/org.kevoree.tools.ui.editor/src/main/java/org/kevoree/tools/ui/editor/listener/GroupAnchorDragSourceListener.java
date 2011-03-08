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
import org.kevoree.tools.ui.editor.command.CleanRequirePortBinding;
import org.kevoree.tools.ui.editor.widget.FlightPortPanel;
import org.kevoree.tools.ui.framework.elements.Binding;
import org.kevoree.tools.ui.framework.elements.GroupAnchorPanel;
import org.kevoree.tools.ui.framework.elements.PortPanel;
import org.kevoree.tools.ui.framework.elements.PortPanel.PortType;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;

/**
 * implementation of the drag source listener for the Dnd of a component
 *
 * @author francoisfouquet
 */
public class GroupAnchorDragSourceListener extends DragSourceAdapter implements DragSourceMotionListener, DragGestureListener {

    private KevoreeUIKernel kernel;
    private GroupAnchorPanel eventSourcePanel;
    private DragSource dragSource;
    private DragGestureEvent dragOriginEvent;
    private Transferable transferable;
    private Point origin2;
    private FlightPortPanel tempPanel = new FlightPortPanel();

    private Binding tempBinding = null; //new Binding();

    /**
     * constructor
     *
     * @param _p
     * @param _panel
     */
    public GroupAnchorDragSourceListener(GroupAnchorPanel _ct, KevoreeUIKernel _kernel) {
        this.eventSourcePanel = _ct;
        this.kernel = _kernel;
        tempBinding = new Binding(Binding.Type.groupLink);

        this.dragSource = DragSource.getDefaultDragSource();
        this.dragSource.createDefaultDragGestureRecognizer((Component) this.eventSourcePanel, DnDConstants.ACTION_MOVE, this);
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
                return eventSourcePanel;
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
        if (dsde.getDragSourceContext().getComponent().equals(this.eventSourcePanel)) {

            Point p = dsde.getLocation();
            Point p2 = (Point) p.clone();
            SwingUtilities.convertPointFromScreen(p2, kernel.getModelPanel());

            //tempPanel.setLocation(new Point(p2.x - origin2.x, p2.y - origin2.y));
            // kernel.getModelPanel().setFlightObject(flightComponent, new Point(p2.x - origin2.x, p2.y - origin2.y));

            tempPanel.setBounds(p2.x - origin2.x, p2.y - origin2.y, eventSourcePanel.getWidth(), eventSourcePanel.getHeight());

            kernel.getModelPanel().repaint();
            // kernel.getModelPanel().revalidate();
        }
    }

    /**
     * callback when the DnD is finished
     *
     * @param dsde
     */
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        //tv.showTrashZone(false);

        //App.view.desktop.remove(flightComponent);
        //tv.add(flightComponent);

        //flightComponent.setActive(false);
        kernel.getModelPanel().unsetFlightObject(tempPanel);
        kernel.getModelPanel().removeBinding(tempBinding);

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

        //STEP 0 CLEAN SELECTED PORT
        /*
        CleanRequirePortBinding commandclean = new CleanRequirePortBinding();
        commandclean.setKernel(kernel);
        commandclean.setPortpanel(eventSourcePanel);
        commandclean.execute(null);
             */
        //STEP 1 CREATE FLIGHT BINDING
        //if()


        //tempPanel = new ComponentPanel();
        //tempPanel.setPreferredSize(new Dimension(200,200));

        dragOriginEvent = dge;
        Point origin = dragOriginEvent.getDragOrigin();
        origin2 = (Point) origin.clone();
        //SwingUtilities.convertPointToScreen(origin2, p);
        //SwingUtilities.convertPointFromScreen(origin2,(Component) p);
        dragSource.startDrag(dragOriginEvent, DragSource.DefaultLinkDrop, transferable, this);

        //tv.showTrashZone(true);

        // flightComponent.setActive(true);

        tempBinding.setFrom(eventSourcePanel);


        tempBinding.setTo(tempPanel);
        kernel.getModelPanel().addBinding(tempBinding);
        kernel.getModelPanel().setFlightObject(tempPanel);

        // tv.remove(flightComponent);
        // App.view.desktop.add(flightComponent);

    }
}
