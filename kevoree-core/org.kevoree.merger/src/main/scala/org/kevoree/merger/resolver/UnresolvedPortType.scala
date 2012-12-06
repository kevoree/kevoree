package org.kevoree.merger.resolver

import org.kevoree.PortType

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 06/12/12
 * Time: 23:38
 */
case class UnresolvedPortType(name : String) extends PortType {

  override def getName = name

}
