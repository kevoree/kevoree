/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.frascatiNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.frascatiNodeTypes.primitives.AdaptatationPrimitiveFactory;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.ow2.frascati.FraSCAti;
import org.ow2.frascati.util.FrascatiException;

/** 
 * @author obarais 
 */ 
@Library(name = "Frascati")
@NodeType
public class FrascatiNode extends JavaSENode {

    private AdaptatationPrimitiveFactory cmdFactory = null;

    //private FrascatiRuntime f_runtime = null;

    private Thread t = null;
    
    @Start
    @Override
    public void startNode() {
        final FrascatiNode selfPointer = this;
        super.startNode();
       // f_runtime = new FrascatiRuntime();
        //f_runtime.start();
       // FraSCAti f_sub = f_runtime.startRuntime();
        
        t = new Thread(){
            @Override
            public void run() {
                FrascatiClassLoaderWrapper fcl = new FrascatiClassLoaderWrapper((KevoreeJarClassLoader)FrascatiNode.class.getClassLoader());
                 Thread.currentThread().setContextClassLoader(fcl);
                try {
                    FraSCAti internal_frascati = FraSCAti.newFraSCAti(fcl);
                    cmdFactory = new AdaptatationPrimitiveFactory(internal_frascati, selfPointer, (org.kevoree.kcl.KevoreeJarClassLoader) FrascatiNode.class.getClassLoader(),null);
                } catch (FrascatiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }


    @Stop
    @Override
    public void stopNode() {
        //f_runtime.stopRuntime();
        super.stopNode();
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return super.kompare(current, target);
    }

    public org.kevoree.api.PrimitiveCommand getSuperPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return super.getPrimitive(adaptationPrimitive);
    }

    @Override
    public org.kevoree.api.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return cmdFactory.getPrimitive(adaptationPrimitive);
    }

}
