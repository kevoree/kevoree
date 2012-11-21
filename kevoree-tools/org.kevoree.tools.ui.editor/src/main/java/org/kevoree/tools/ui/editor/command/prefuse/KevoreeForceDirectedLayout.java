package org.kevoree.tools.ui.editor.command.prefuse;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.Force;
import prefuse.util.force.SpringForce;
import prefuse.util.force.WallForce;

/**
 * Created with IntelliJ IDEA.
 * User: dvojtise
 * Date: 20/11/12
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeForceDirectedLayout extends ForceDirectedLayout {


    public KevoreeForceDirectedLayout(String graph){
       super(graph, true);
       // add top and left repelling walls
       //this.getForceSimulator().addForce(new WallForce(-0.001f, 0,0, 0,20000));
       //this.getForceSimulator().addForce(new WallForce(-0.001f, 0,0, 20000,0));
    }


    public SpringForce getSpringForce(){
        for(Force f : getForceSimulator().getForces()){
            if(f instanceof SpringForce) return (SpringForce) f;
        }
        return null;
    }
}
