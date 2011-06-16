package org.kevoree.core.basechecker.portchecker

import org.kevoree.ContainerRoot
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._
import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/06/11
 * Time: 09:23
 */

class PortChecker extends CheckerService {
  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: List[CheckerViolation] = List()
    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            component.getRequired.foreach {
              port =>
                if (!port.isBind) {
                  val concreteViolation: CheckerViolation = new CheckerViolation()
                  concreteViolation.setMessage("Required port (" + port.getName + ") is not bind")
                  violations = violations ++ List(concreteViolation)
                }
            }
        }
    }
    violations
  }
}