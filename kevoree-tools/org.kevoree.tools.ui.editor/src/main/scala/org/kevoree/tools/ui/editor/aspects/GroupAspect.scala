package org.kevoree.tools.ui.editor.aspects

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.{ContainerRoot, Group}
import org.kevoree.tools.ui.framework.elements.{Binding, ModelPanel, GroupPanel}
import scala.collection.JavaConversions._

case class GroupAspect(self: Group) {

  def removeModelAndUI(kernel: KevoreeUIKernel) = {
    val root: ContainerRoot = self.eContainer.asInstanceOf[ContainerRoot]
    val panel = kernel.getUifactory().getMapping().get(self).asInstanceOf[GroupPanel]
    val modelPanel = kernel.getUifactory().getMapping().get(root).asInstanceOf[ModelPanel]

    //REMOVE UI BINDING
    val bindings = kernel.getModelPanel.getBindings().toList  ++ List()
    bindings.foreach{b=>
      if(b.getFrom.equals(panel.getAnchor) || b.getTo.equals(panel.getAnchor)){
          kernel.getModelPanel.removeBinding(b)
      }
    }

    modelPanel.removeInstance(panel)
    root.getGroups.remove(self)
    kernel.getUifactory().getMapping().unbind(panel, self);
  }

}