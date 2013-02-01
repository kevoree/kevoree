package org.kevoree.library.sky.api.checker

import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.ContainerRoot
import java.util
import org.kevoree.library.sky.api.helper.KloudModelHelper
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/06/12
 * Time: 13:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class NodeNameKloudChecker extends KloudCheckerService {

  def check (model: ContainerRoot): util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new util.ArrayList[CheckerViolation]()
    (model.getGroups.filter(g => g.getName == KloudModelHelper.getPaaSKloudGroup(model)) ++ model.getNodes.filter(n => KloudModelHelper.isPaaSNode(model, n))).foreach {
      // check only groups and nodes that will be host on Kloud => inherits of PJavaSENode
      instance =>
        if (!instance.getName.startsWith(getId)) {
          val concreteViolation: CheckerViolation = new CheckerViolation()
          concreteViolation.setMessage(instance.getName + " is not a valid name. It must start with your login.")
          val targets = new util.ArrayList[AnyRef]()
          targets.add(instance.asInstanceOf[AnyRef])
          concreteViolation.setTargetObjects(targets)
          violations.add(concreteViolation)
        }
    }
    violations
  }
}
