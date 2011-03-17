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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.kevoree.editor.component.creator.model.Art2ModelElement;

/**
 *
 * @author gnain
 */
public class ModelElementDragSourceListener extends DragSourceAdapter implements DragSourceMotionListener, DragGestureListener {

    private JPanel flyingComponent;
    private DragSource dragSource;
    private DragGestureEvent dragOriginEvent;
    private Transferable transferable;
    private Point originPointClone;
    private int mode;
    private static final int DRAG_MODE = 0;
    private static final int RESIZE_MODE = 1;

    public ModelElementDragSourceListener(Art2ModelElement art2Element) {
        flyingComponent = art2Element.getGraphicalRepresentation();
        this.dragSource = DragSource.getDefaultDragSource();
        this.dragSource.createDefaultDragGestureRecognizer((Component) this.flyingComponent, DnDConstants.ACTION_MOVE, this);
        dragSource.addDragSourceMotionListener(this);
        transferable = new TransferableArt2ModelElement(art2Element);

    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        System.out.println("Art2ElementDragSourceListener");
        dragOriginEvent = dge;
        Point origin = dragOriginEvent.getDragOrigin();
        originPointClone = (Point) origin.clone();

        Point bottomRight = new Point();
        bottomRight.setLocation(
                flyingComponent.getWidth(),
                flyingComponent.getHeight());
        System.out.println("BR:" + bottomRight.x + "," + bottomRight.y);
        System.out.println("Distance:" + originPointClone.distance(bottomRight));
        if (originPointClone.distance(bottomRight) < 7) {
            System.out.println("ResizeModeOn");
            mode = RESIZE_MODE;
        } else {
            mode = DRAG_MODE;
            //SwingUtilities.convertPointToScreen(origin2, p);
            //SwingUtilities.convertPointFromScreen(origin2,(Component) p);
            dragSource.startDrag(dragOriginEvent, DragSource.DefaultLinkDrop, transferable, this);
        }

    }

    public void dragMouseMoved(DragSourceDragEvent dsde) {
        super.dragMouseMoved(dsde);
        Point p = dsde.getLocation();
        Point p2 = (Point) p.clone();
        System.out.println("DRAG_MouseMoved");
        switch (mode) {
            case DRAG_MODE: {
                if (dsde.getDragSourceContext().getComponent().equals(this.flyingComponent)) {
                    
                    SwingUtilities.convertPointFromScreen(p2, flyingComponent.getParent());

                    //tempPanel.setLocation(new Point(p2.x - origin2.x, p2.y - origin2.y));
                    // kernel.getModelPanel().setFlightObject(flightComponent, new Point(p2.x - origin2.x, p2.y - origin2.y));

                    flyingComponent.setBounds(
                            p2.x - originPointClone.x,
                            p2.y - originPointClone.y,
                            flyingComponent.getWidth(),
                            flyingComponent.getHeight());


                    flyingComponent.getParent().repaint();
                }
            }
            break;
            case RESIZE_MODE: {
                System.out.println("DRAG_MouseMoved");
            }
        }


    }
}
