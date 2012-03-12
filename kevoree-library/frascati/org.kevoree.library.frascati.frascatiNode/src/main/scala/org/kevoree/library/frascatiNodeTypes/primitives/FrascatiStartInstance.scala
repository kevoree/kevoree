package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.api.PrimitiveCommand
import org.objectweb.fractal.api.control.LifeCycleController
import org.ow2.frascati.FraSCAti
import org.objectweb.fractal.api.Component
import org.slf4j.LoggerFactory
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:27
 */

case class FrascatiStartInstance(adapptationPrimitive: AdaptationPrimitive, frascati: FraSCAti) extends PrimitiveCommand {
  var lf: LifeCycleController = null
  val logger = LoggerFactory.getLogger(this.getClass)

  override def execute(): Boolean = {

    val c = adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance]
    KevoreeDeployManager.bundleMapping.find(map => map.objClassName == c.getClass.getName && map.name == c.getName) match {
      case None => false
      case Some(mapfound) => {
        val c: Component = mapfound.ref.asInstanceOf[Component]
        lf = c.getFcInterface("lifecycle-controller").asInstanceOf[LifeCycleController]
        lf.startFc();
        true
      }
    }
  }

  override def undo() {
    if (lf != null) {
      lf.stopFc()
    }
  }


}