package org.kevoree.framework.deploy

import org.kevoreeAdaptation.{ParallelStep, AdaptationModel}
import org.kevoree.framework.{PrimitiveCommand, AbstractNodeType}
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/09/11
 * Time: 20:19
 */

trait PrimitiveCommandExecutionHelper {

  var logger = LoggerFactory.getLogger(this.getClass)

  def execute(adaptionModel: AdaptationModel, nodeInstance: AbstractNodeType): Boolean = {
    executeStep(adaptionModel.getOrderedPrimitiveSet, nodeInstance)
  }

  private def executeStep(step: ParallelStep, nodeInstance: AbstractNodeType): Boolean = {
    val phase = new KevoreeParDeployPhase
    val populateResult = step.getAdaptations.forall {
      adapt =>
        val primitive = nodeInstance.getPrimitive(adapt)
        if (primitive != null) {
          logger.debug("Populate primitive => "+primitive)
          phase.populate(primitive)
          true
        } else {
          logger.debug("Error while searching primitive => "+adapt)
          false
        }
    }
    if (populateResult) {
      phase.runPhase()
      val subResult = executeStep(step.getNextStep, nodeInstance)
      if(!subResult){
        phase.rollBack()
        false
      } else {true}
    } else {
      logger.debug("Primitive mapping error")
      false
    }
  }

  class KevoreeParDeployPhase {
    var primitives: List[PrimitiveCommand] = List()

    def populate(cmd: PrimitiveCommand) {
      primitives = primitives ++ List(cmd)
    }

    def runPhase(): Boolean = {
      true
    }

    def rollBack(){

    }

  }


}