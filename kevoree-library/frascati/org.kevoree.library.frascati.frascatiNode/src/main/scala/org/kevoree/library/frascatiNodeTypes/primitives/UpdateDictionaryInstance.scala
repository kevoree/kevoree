package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.api.PrimitiveCommand
import org.ow2.frascati.FraSCAti
import org.objectweb.fractal.api.Component
import org.objectweb.fractal.api.control.{AttributeController, ContentController}
import org.kevoree.Instance

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 23:16
 */

case class UpdateDictionaryInstance(adapptationPrimitive: AdaptationPrimitive,frascati : FraSCAti) extends PrimitiveCommand {
  var oldDico: org.kevoree.Dictionary = _

  override def execute(): Boolean = {

    val c: Component = frascati.getCompositeManager.getComposite(adapptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName)
    val content: ContentController = c.getFcInterface("content-controller").asInstanceOf[ContentController];
    //content.getFcSubComponents().apply(0).getFcInterfaces().foreach(o => System.err.println(o))
    val attr = content.getFcSubComponents.apply(0).getFcInterfaces.filter(o => o.isInstanceOf[AttributeController]).apply(0)
    if (attr != null) {
      var att = attr.asInstanceOf[AttributeController]
      adapptationPrimitive.getRef.asInstanceOf[Instance].getDictionary.get
    }
    true
  }

  override def undo() {
    //TODO
  }

}
