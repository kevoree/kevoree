package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.api.PrimitiveCommand
import org.kevoreeAdaptation.AdaptationPrimitive
import org.ow2.frascati.FraSCAti

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:35
 */

case class FrascatiRemoveInstance(adapptationPrimitive: AdaptationPrimitive, frascati: FraSCAti) extends PrimitiveCommand {

  override def execute(): Boolean = {
    val c = frascati.getCompositeManager.getComposite(adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
    frascati.close(c)
    true
  }

  override def undo() {
   // var v = File.createTempFile(adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName, "composite")
  }
}
