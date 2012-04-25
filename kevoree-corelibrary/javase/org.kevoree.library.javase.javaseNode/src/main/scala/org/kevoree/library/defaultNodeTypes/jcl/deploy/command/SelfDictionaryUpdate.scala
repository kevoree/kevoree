package org.kevoree.library.defaultNodeTypes.jcl.deploy.command

import org.kevoree.api.PrimitiveCommand
import org.kevoree.Instance
import org.kevoree.framework.AbstractNodeType
import org.slf4j.LoggerFactory
import java.util.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 15:29
 */

case class SelfDictionaryUpdate(c: Instance, node: AbstractNodeType) extends PrimitiveCommand {
  var logger = LoggerFactory.getLogger(this.getClass)

  private var lastDictioanry: HashMap[String, AnyRef] = null

  def execute(): Boolean = {
    //BUILD MAP
    //SET DEFAULT VAL
    val dictionary: java.util.HashMap[String, AnyRef] = new java.util.HashMap[String, AnyRef]
    if (c.getTypeDefinition.getDictionaryType.isDefined) {
      if (c.getTypeDefinition.getDictionaryType.get.getDefaultValues != null) {
        c.getTypeDefinition.getDictionaryType.get.getDefaultValues.foreach {
          dv =>
            dictionary.put(dv.getAttribute.getName, dv.getValue)
        }
      }
    }
    //SET DIC VAL
    if (c.getDictionary.isDefined) {
      c.getDictionary.get.getValues.foreach {
        v =>
          dictionary.put(v.getAttribute.getName, v.getValue)
      }
    }

    //SAVE DICTIONARY
    lastDictioanry = node.getDictionary
    node.setDictionary(dictionary)
    node.updateNode() // TODO REFLEXIVE CALL

    true
  }

  def undo() {
    if (lastDictioanry != null) {
      node.setDictionary(lastDictioanry)
      node.updateNode() // TODO REFLEXIVE CALL
    }
  }

}
