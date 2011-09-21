package org.kevoree.tools.annotation.generator

import org.kevoree.NodeType

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/09/11
 * Time: 16:11
 */

object AdaptationPrimitiveMapping {

  var mappings = Map[NodeType, Map[String, String]]()

  def getMappings(nodeType : NodeType) : Map[String, String] = {mappings(nodeType)}

  def addMapping(nodeType : NodeType, name : String, className : String) {
    var nodeMapping = mappings(nodeType)
    nodeMapping = nodeMapping ++ Map(name -> className)
    mappings = mappings ++ Map(nodeType -> nodeMapping)
  }

  def clear() {
    mappings = Map[NodeType, Map[String, String]]()
  }

}