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
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoree.library.frascatiNodeTypes.primitives.AdaptatationPrimitiveFactory;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.ow2.frascati.FraSCAti;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @author obarais 
 */ 
@Library(name = "Frascati")
@NodeType
public class FrascatiNode extends JavaSENode {

    private AdaptatationPrimitiveFactory cmdFactory = null;

    private FrascatiRuntime f_runtime = null;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    @Override
    public void startNode() {
        long beforeStart = System.currentTimeMillis();
        final FrascatiNode selfPointer = this;
        super.startNode();
        f_runtime = new FrascatiRuntime();
        f_runtime.start();
        FraSCAti f_sub = f_runtime.startRuntime();
        cmdFactory = new AdaptatationPrimitiveFactory(f_sub, selfPointer, (org.kevoree.kcl.KevoreeJarClassLoader) FrascatiNode.class.getClassLoader(),f_runtime);
        logger.info("FrascatiNode started in {} ms",(System.currentTimeMillis()-beforeStart));
    }


    @Stop
    @Override
    public void stopNode() {
        f_runtime.stopRuntime();
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
