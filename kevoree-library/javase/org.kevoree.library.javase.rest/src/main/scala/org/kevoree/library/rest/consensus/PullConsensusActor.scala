package org.kevoree.library.rest.consensus

import actors.{TIMEOUT, DaemonActor}
import org.kevoree.library.rest.RestConsensusGroup


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/02/12
 * Time: 15:31
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class PullConsensusActor (interval: Long, group : RestConsensusGroup) extends DaemonActor {
  
  case class STOP()
  
  start()

  def stop() {
    this ! STOP()
  }
  
  def act () {
    loop {
      reactWithin(interval) {
        case STOP() => this.exit()
        case TIMEOUT => {
          val modelOption = ConsensusClient.pull(group.getModelElement, group.getNodeName, group.getModelService.getLastModel, HashManager.getHashedModel(group.getModelService.getLastModel))
          if (modelOption.isDefined) {
            group.getModelService.atomicCompareAndSwapModel(group.getModelService.getLastUUIDModel, modelOption.get)
          }
        }
      }
    }
  }
}
