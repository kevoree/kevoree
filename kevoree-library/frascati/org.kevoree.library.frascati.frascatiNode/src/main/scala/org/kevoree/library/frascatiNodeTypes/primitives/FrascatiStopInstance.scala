package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.api.PrimitiveCommand
import org.objectweb.fractal.api.control.LifeCycleController
import org.ow2.frascati.FraSCAti
import org.objectweb.fractal.api.Component
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:27
 */

case class FrascatiStopInstance(adapptationPrimitive: AdaptationPrimitive, frascati: FraSCAti) extends PrimitiveCommand {
  var lf: LifeCycleController = null
  val logger = LoggerFactory.getLogger(this.getClass)

  override def execute(): Boolean = {
    val c: Component = frascati.getCompositeManager.getComposite(adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
    lf = c.getFcInterface("lifecycle-controller").asInstanceOf[LifeCycleController];
    try {
      lf.stopFc()
      true
    } catch {
      case _@e => {
        logger.debug("Error stopping Instance ",e)
        false
      }
    }
  }

  override def undo() {
    if (lf != null) {
      lf.startFc()
    }
  }


}