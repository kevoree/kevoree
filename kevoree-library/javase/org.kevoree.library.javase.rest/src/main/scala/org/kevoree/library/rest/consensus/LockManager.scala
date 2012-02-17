package org.kevoree.library.rest.consensus

import actors.DaemonActor
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.handler.{UUIDModel, ModelHandlerLockCallBack, KevoreeModelHandlerService}
import org.kevoree.library.rest.RestConsensusGroup
import org.slf4j.LoggerFactory
import java.util.{Random, UUID}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 18:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class LockManager (timeout: Long, consensusGroup: RestConsensusGroup, r : Random) extends DaemonActor with ModelHandlerLockCallBack {
  private val logger = LoggerFactory.getLogger(getClass)

  case class LOCK ()

  case class UNLOCK ()

  case class IS_LOCK ()

  case class LOCK_ACQUIRED (p1: UUID)

  case class LOCK_REJECTED ()

  case class LOCK_TIMEOUT ()

  case class STOP ()

  case class UPDATE (model: ContainerRoot)

  private var lockUUID: UUID = null
  private var updateDone = false

  def lock (): Boolean = {
    (this !? LOCK()).asInstanceOf[Boolean]
  }

  def unlock () : Boolean = {
    (this !? UNLOCK()).asInstanceOf[Boolean]
  }

  def isLock: Boolean = {
    (this !? IS_LOCK()).asInstanceOf[Boolean]
  }

  def update (model: ContainerRoot) {
    this ! UPDATE(model)
  }

  def lockTimeout () {
    (this ! LOCK_TIMEOUT())
  }

  def lockRejected () {
    (this ! LOCK_REJECTED())
  }

  def lockAcquired (uuid: UUID) {
    (this ! LOCK_ACQUIRED(uuid))
  }

  def stop () {
    this ! STOP()
  }

  def act () {
    loop {
      react {
        case LOCK() => {
          val previousSender = this.sender
          consensusGroup.getModelService.acquireLock(this, timeout)
          updateDone = false
          react {
            case LOCK_ACQUIRED(uuid) => lockUUID = uuid; previousSender ! true
            case LOCK_REJECTED() => previousSender ! false
            case LOCK_TIMEOUT() => previousSender ! false
            case STOP() => previousSender ! false; stopInternals()
          }
        }
        case IS_LOCK() => reply(lockUUID != null)
        case UNLOCK() => {
          logger.debug("Unlocking Kevoree core !")
          consensusGroup.getModelService.releaseLock(lockUUID)
          lockUUID = null
          reply(updateDone)
        }
        case LOCK_REJECTED() => lockUUID = null
        case LOCK_TIMEOUT() => lockUUID = null
        case STOP() => stopInternals()
        case UPDATE(model) => {
          val uuidModel = new UUIDModel() {
            def getUUID = lockUUID

            def getModel = null
          }
          try {
            consensusGroup.getModelService.unregisterModelListener(consensusGroup.getModelListener)
            logger.debug("Consensus is OK => an update must be done on the local node")
            consensusGroup.getModelService.atomicCompareAndSwapModel(uuidModel, model)
            consensusGroup.getModelService.registerModelListener(consensusGroup.getModelListener)
            updateDone = true
          } catch {
            case _@e =>
          }
        }
      }
    }
  }

  private def stopInternals () {
    if (lockUUID != null) {
      consensusGroup.getModelService.releaseLock(lockUUID)
    }
    this.exit()
  }
}
