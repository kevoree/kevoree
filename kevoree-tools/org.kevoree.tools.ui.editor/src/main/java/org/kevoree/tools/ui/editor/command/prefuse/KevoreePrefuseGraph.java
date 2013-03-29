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
package org.kevoree.tools.ui.editor.command.prefuse;

import org.kevoree.*;
import org.kevoree.container.KMFContainer;
import org.kevoree.tools.ui.editor.KevoreeUIKernel;
import org.kevoree.tools.ui.framework.elements.NodePanel;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.visual.VisualItem;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: dvojtise
 * Date: 20/11/12
 * Time: 11:20
 * To change this template use File | Settings | File Templates.
 */
public class KevoreePrefuseGraph {

    /** factor used to reduce the real screen to the default prefuse screen. This improve efficiency of repulsion force */
    public static final double DISPLAY_SCALE_FACTOR = 3;

    // ----------------------
    // prefuse data structure
    public static final String LABEL = "label" ;
    /** Node table schema used for kevoree Graphs */
    public static final Schema KEVOREE_SCHEMA = new Schema();
    static {
        KEVOREE_SCHEMA.addColumn(LABEL, String.class, "");
    }
    // ----------------------

    private HashMap<NamedElement, Node> model2GraphMap = new HashMap<NamedElement, Node>();
    private KevoreeUIKernel kernel;
    private Graph graph;


    public   KevoreePrefuseGraph(KevoreeUIKernel kernel){
          this.kernel = kernel;
    }

    public HashMap<NamedElement, Node> getModel2GraphMap(){
        return model2GraphMap;
    }

    public Graph getGraph(){
        return graph;
    }

    public Graph computePrefuseGraph(ContainerRoot model){
        graph  = new Graph();
        graph.getNodeTable().addColumns(KevoreePrefuseGraph.KEVOREE_SCHEMA);
        model2GraphMap.clear();



        // getReal graphical element position
        kernel.getUifactory().getMapping();

        //FIND TOP LEVEL NODE
        for (ContainerNode newnode : model.getNodes()) {
            Node n = graph.addNode();
            n.setString(KevoreePrefuseGraph.LABEL, newnode.getName());

            model2GraphMap.put(newnode, n);
        }


        //LOAD HUB
        for (Channel hub : kernel.getModelHandler().getActualModel().getHubs()) {
            Node n = graph.addNode();
            n.setString(KevoreePrefuseGraph.LABEL, hub.getName());

            model2GraphMap.put(hub, n);
        }

        //LOAD GROUP
        for (Group group : kernel.getModelHandler().getActualModel().getGroups()) {
            Node n = graph.addNode();
            n.setString(KevoreePrefuseGraph.LABEL, group.getName());
            model2GraphMap.put(group, n);

            //LOAD GROUP BINDINGS
            for (ContainerNode subNode : group.getSubNodes()) {
                graph.addEdge(n, model2GraphMap.get(subNode));
            }
        }
        //LOAD MBINDING
        for (MBinding binding : kernel.getModelHandler().getActualModel().getMBindings()) {

            graph.addEdge(model2GraphMap.get(findTopLevelNode(binding.getPort())), model2GraphMap.get(binding.getHub()));
        }

        return graph;
    }

    public NamedElement findTopLevelNode( Port port) {
        return findTopLevelNode(port.eContainer());
    }
    public NamedElement findTopLevelNode( KMFContainer cnode) {
        if( cnode.eContainer() instanceof  ContainerRoot) return (NamedElement) cnode;
        else return findTopLevelNode(cnode.eContainer());
    }

    public void initVisualItemWithCurrentLocation(PrefuseGraphView view){

        for(NamedElement ne : model2GraphMap.keySet()){
            JPanel panel = (JPanel)kernel.getUifactory().getMapping().get(ne);
            VisualItem vi = view.getVisualItem(model2GraphMap.get(ne));


            // TODO find which of these "set" effectively work
            vi.setStartX(panel.getLocation().getX()/DISPLAY_SCALE_FACTOR);
            vi.setX( panel.getLocation().getX()/DISPLAY_SCALE_FACTOR);
            vi.setEndX(panel.getLocation().getX()/DISPLAY_SCALE_FACTOR);
            vi.setStartY(panel.getLocation().getY()/DISPLAY_SCALE_FACTOR);
            vi.setY(panel.getLocation().getY()/DISPLAY_SCALE_FACTOR);
            vi.setEndY(panel.getLocation().getY()/DISPLAY_SCALE_FACTOR);

        }
    }

    public void updateCurrentLocationFromPrefuse(PrefuseGraphView view){
        for(NamedElement ne : model2GraphMap.keySet()){
            JPanel panel = (JPanel)kernel.getUifactory().getMapping().get(ne);
            VisualItem vi = view.getVisualItem(model2GraphMap.get(ne));

            // min/max to make sure it doesn't go too far away
            //panel.setLocation(Math.min(Math.max(0, ((Double) (vi.getX() * DISPLAY_SCALE_FACTOR)).intValue()), kernel.getModelPanel().getWidth()),
            //        Math.min(Math.max(0, ((Double) (vi.getY() * DISPLAY_SCALE_FACTOR)).intValue()), kernel.getModelPanel().getHeight()));
            panel.setLocation(((Double) (vi.getX() * DISPLAY_SCALE_FACTOR)).intValue(),
                              ((Double) (vi.getY() * DISPLAY_SCALE_FACTOR)).intValue());
        }
    }
}
