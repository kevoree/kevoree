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

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.*;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.SpringForce;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import javax.swing.*;
import java.awt.*;

/**
 * Panel doing the link between the prefuse engine and kevoree graph
 * by default hidden
 */
public class PrefuseGraphView extends JPanel {



    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";

    public boolean mustDraw = false;
    public long stepTorun = 10;

    private Visualization m_vis;

    public PrefuseGraphView(KevoreePrefuseGraph kpg, int displayWidth, int displayHeight, boolean mustDraw, long stepTorun){
        this(kpg, displayWidth, displayHeight);
        this.mustDraw = mustDraw;
        this.stepTorun = stepTorun;
    }
    public PrefuseGraphView(KevoreePrefuseGraph kpg, int displayWidth, int displayHeight){
        super(new BorderLayout());

        // create a new, empty visualization for our data
        m_vis = new Visualization();

        // --------------------------------------------------------------------
        // set up the renderers
        LabelRenderer tr = new LabelRenderer();
        tr.setRoundedCorner(8, 8);
        m_vis.setRendererFactory(new DefaultRendererFactory(tr));


        // --------------------------------------------------------------------
        // register the data with a visualization

        // adds graph to visualization and sets renderer label field
        setGraph(kpg.getGraph());

        // --------------------------------------------------------------------
        // create actions to process the visual data

        //int hops = 30;
        //final GraphDistanceFilter filter = new GraphDistanceFilter(GRAPH, hops);

        ActionList animate = new ActionList(Activity.INFINITY);
        //ActionList animate = new ActionList(Activity.DEFAULT_STEP_TIME*stepTorun*100);
        animate.add(new KevoreeForceDirectedLayout(GRAPH));
        if(mustDraw){
            ColorAction fill = new ColorAction(NODES,
                    VisualItem.FILLCOLOR, ColorLib.rgb(200,200,255));
            fill.add(VisualItem.FIXED, ColorLib.rgb(255,100,100));
            fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));

            ActionList draw = new ActionList();
            //draw.add(filter);
            draw.add(fill);
            draw.add(new ColorAction(NODES, VisualItem.STROKECOLOR, 0));
            draw.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)));
            draw.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(200)));
            draw.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(200)));


            animate.add(fill);
            animate.add(new RepaintAction());
            // finally, we register our ActionList with the Visualization.
            // we can later execute our Actions by invoking a method on our
            // Visualization, using the name we've chosen below.
            m_vis.putAction("draw", draw);
            m_vis.putAction("layout", animate);

            m_vis.runAfter("draw", "layout");
        }
        else{
            // finally, we register our ActionList with the Visualization.
            // we can later execute our Actions by invoking a method on our
            // Visualization, using the name we've chosen below.
            m_vis.putAction("layout", animate);
        }


            // --------------------------------------------------------------------
            // set up a display to show the visualization

        Display display = new Display(m_vis);
        display.setSize(displayWidth,displayHeight);
        if(mustDraw){
            //display.pan(0, 0);
            //display.pan(displayWidth/2, displayHeight/2);
            display.setForeground(Color.GRAY);
            display.setBackground(Color.WHITE);


            // main display controls
            display.addControlListener(new FocusControl(1));
            display.addControlListener(new DragControl());
            display.addControlListener(new PanControl());
            display.addControlListener(new ZoomControl());
            display.addControlListener(new WheelZoomControl());
            display.addControlListener(new ZoomToFitControl());
            display.addControlListener(new NeighborHighlightControl());


            display.setForeground(Color.GRAY);
            display.setBackground(Color.WHITE);

            add(display);
        }
        // --------------------------------------------------------------------
        // launch the visualization

        // create a panel for editing force values
        ForceSimulator fsim = ((KevoreeForceDirectedLayout)animate.get(0)).getForceSimulator();
        //((KevoreeForceDirectedLayout)animate.get(0)).getSpringForce().setParameter(1, 250);  // change SpringForce/DefaultSpringLength
        //((KevoreeForceDirectedLayout)animate.get(0)).getSpringForce().setParameter(0, 0.001f);  // change SpringForce/springCoeff

        //fsim.setSpeedLimit(fsim.getSpeedLimit()*2);        // go faster ?

        // now we run our action list
        //m_vis.run("draw");
        kpg.initVisualItemWithCurrentLocation(this);





    }

    public Visualization getVisualization(){
        return m_vis;
    }
    public VisualItem getVisualItem(Tuple t){
        return m_vis.getVisualItem(GRAPH,t);
    }

    public void setGraph(Graph g) {
        // update labeling
        DefaultRendererFactory drf = (DefaultRendererFactory)
                                                m_vis.getRendererFactory();
        ((LabelRenderer)drf.getDefaultRenderer()).setTextField(KevoreePrefuseGraph.LABEL);

        // update graph
        m_vis.removeGroup(GRAPH);
        VisualGraph vg = m_vis.addGraph(GRAPH, g);
        m_vis.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.FALSE);
        VisualItem f = (VisualItem)vg.getNode(0);
        m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
        f.setFixed(false);
    }

    public void run() {
        if(mustDraw){
            m_vis.run("draw");
        }
        else{
            m_vis.run("layout");
        }
    }
    public void cancel() {
        if(mustDraw){
            m_vis.cancel("draw");
        }
        else{
            m_vis.cancel("layout");
        }
    }

}
