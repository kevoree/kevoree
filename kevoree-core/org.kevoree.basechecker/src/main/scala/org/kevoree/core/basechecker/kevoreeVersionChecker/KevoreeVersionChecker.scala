package org.kevoree.core.basechecker.kevoreeVersionChecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{ContainerNode, DeployUnit, ContainerRoot}
import java.util.ArrayList
import collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/04/12
 * Time: 15:21
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeVersionChecker extends CheckerService {
  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    var violations: java.util.List[CheckerViolation] = new ArrayList[CheckerViolation]()
    model.getDeployUnits.filter(du => du.getGroupName == "org.kevoree"
      && (du.getUnitName == "org.kevoree.api"
      || du.getUnitName == "org.kevoree.core"
      || du.getUnitName == "org.kevoree.framework"
      || du.getUnitName == "org.kevoree.kcl")).forall {
      du => true
    }

    model.getNodes.foreach {
      node =>
        node.getComponents.foreach {
          component =>
            val du = component.getTypeDefinition.foundRelevantDeployUnit(node)
            violations.addAll(check(component.getName, du, node))
        }
        node.getGroups.foreach {
          group =>
            val du = group.getTypeDefinition.foundRelevantDeployUnit(node)
            violations.addAll(check(group.getName, du, node))
        }
        node.getChannelFragment.foreach {
          channel =>
            val du = channel.getTypeDefinition.foundRelevantDeployUnit(node)
            violations.addAll(check(channel.getName, du, node))
        }
    }
    violations
  }

  private def check (instanceName: String, deployUnit: DeployUnit, node: ContainerNode): java.util.List[CheckerViolation] = {
    var violations: java.util.List[CheckerViolation] = new ArrayList[CheckerViolation]()
    if (((deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.api")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.core")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.framework")
      || (deployUnit.getGroupName == "org.kevoree" && deployUnit.getUnitName == "org.kevoree.kcl"))
      && deployUnit.getVersion != node.getKevoreeVersion) {
      val concreteViolation: CheckerViolation = new CheckerViolation()
      concreteViolation
        .setMessage("Component "+ instanceName + " has a required deployUnit \"" + deployUnit.getGroupName + ":" + deployUnit.getUnitName + "\" which needs different version of Kevoree that the one provided (requiredVersion=" + deployUnit.getVersion +
        ",providedVersion=" + node.getKevoreeVersion)
      concreteViolation.setTargetObjects(List(node.asInstanceOf[AnyRef]))
      violations.add(concreteViolation)
      violations
    } else {
      deployUnit.getRequiredLibs.foreach {
        requiredDU =>
          violations.addAll(check(instanceName, requiredDU, node))
      }
      violations
    }
  }
}