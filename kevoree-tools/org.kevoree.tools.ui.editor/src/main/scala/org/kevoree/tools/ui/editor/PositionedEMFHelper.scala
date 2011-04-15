package org.kevoree.tools.ui.editor

import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.framework.elements.NodePanel


object PositionedEMFHelper {

  def updateModelUIMetaData(model : ContainerRoot) {
                 //PREPROCESS UI POSITION

    model.getNodes.foreach({node =>
         var nodePanel = NodePanelkernel.getUifactory().getMapping().get(node).asInstanceOf[NodePanel];
    })

        for(ContainerNode node : kernel.getModelHandler().getActualModel().getNodes()){
            NodePanel nodePanel = (NodePanel) kernel.getUifactory().getMapping().get(node);
            String metadata = "x="+nodePanel.getX()+","+"y="+nodePanel.getY();
            node.setMetaData(metadata);
        }
  }

}