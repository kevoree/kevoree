package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.api.PrimitiveCommand
import org.ow2.frascati.FraSCAti
import org.objectweb.fractal.api.Component
import org.objectweb.fractal.api.control.{AttributeController, ContentController}
import org.kevoree.Instance
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager
import org.ow2.frascati.tinfi.api.control.SCAPropertyController

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:16
 */

case class FrascatiUpdateDictionaryInstance(adapptationPrimitive: AdaptationPrimitive, frascati: FraSCAti) extends PrimitiveCommand {
  var oldDico: org.kevoree.Dictionary = _

  override def execute(): Boolean = {

    val c = adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance]

    KevoreeDeployManager.bundleMapping.find(map => map.objClassName == c.getClass.getName && map.name == c.getName) match {
      case None => false
      case Some(mapfound) => {
        val c: Component = mapfound.ref.asInstanceOf[Component]
        val content: ContentController = c.getFcInterface("content-controller").asInstanceOf[ContentController];
        val attr = content.getFcSubComponents.apply(0).getFcInterfaces.filter(o => o.isInstanceOf[SCAPropertyController]).apply(0)
        if (attr != null) {
          val att = attr.asInstanceOf[SCAPropertyController]
          adapptationPrimitive.getRef.asInstanceOf[Instance].getDictionary.map(dic => {
            dic.getValues.foreach{ v =>
              att.setValue(v.getAttribute.getName,v.getValue)
            }
          })
        }

      }
      true
    }
  }

  override def undo() {
    //TODO
  }

}
