package org.kevoree.tools.ui.editor

import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.framework.elements.{GroupPanel, ChannelPanel, NodePanel}

object PositionedEMFHelper {

  def updateModelUIMetaData(kernel: KevoreeUIKernel) {
    //PREPROCESS UI POSITION
    val model = kernel.getModelHandler.getActualModel

    model.getNodes.foreach(node => {
      val nodePanel = kernel.getUifactory.getMapping.get(node).asInstanceOf[NodePanel];
      val metadata = "x=" + nodePanel.getX + "," + "y=" + nodePanel.getY
      node.setMetaData(metadata)
    })
    model.getHubs.foreach(hub => {
      val hubPanel = kernel.getUifactory.getMapping.get(hub).asInstanceOf[ChannelPanel];
      val metadata = "x=" + hubPanel.getX + "," + "y=" + hubPanel.getY
      hub.setMetaData(metadata)
    })
    model.getGroups.foreach(group => {
      val groupPanel = kernel.getUifactory.getMapping.get(group).asInstanceOf[GroupPanel];
      val metadata = "x=" + groupPanel.getX + "," + "y=" + groupPanel.getY
      group.setMetaData(metadata)
    })
  }

}