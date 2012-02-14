package org.kevoree.library.rest.consensus

import actors.DaemonActor
import java.util.UUID
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.handler.{UUIDModel, ModelHandlerLockCallBack, KevoreeModelHandlerService}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 18:38
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class LockManager (timeout: Long, modelService: KevoreeModelHandlerService) extends DaemonActor with ModelHandlerLockCallBack {

  case class LOCK ()

  case class UNLOCK ()

  case class IS_LOCK ()

  case class LOCK_ACQUIRED (p1: UUID)

  case class LOCK_REJECTED ()

  case class LOCK_TIMEOUT ()

  case class STOP ()

  case class UPDATE (model: ContainerRoot)

  private var lockUUID: UUID = null

  def lock (): Boolean = {
    (this !? LOCK()).asInstanceOf[Boolean]
  }

  def unlock () {
      this ! LOCK()
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
          lockUUID = null
          modelService.acquireLock(this, timeout)
          react {
            case LOCK_ACQUIRED(uuid) => lockUUID = uuid; previousSender ! true
            case LOCK_REJECTED() => previousSender ! false
            case LOCK_TIMEOUT() => previousSender ! false
            case STOP() => previousSender ! false; stopInternals()
          }
        }
        case IS_LOCK() => reply(lockUUID != null)
        case UNLOCK() => this.modelService.releaseLock(lockUUID);lockUUID = null
        case LOCK_REJECTED() => lockUUID = null
        case LOCK_TIMEOUT() => lockUUID = null
        case STOP() => stopInternals()
        case UPDATE(model) => {
          val uuidModel = new UUIDModel() {
            def getUUID = lockUUID
            def getModel = null
          }
          try {
            this.modelService.atomicCompareAndSwapModel(uuidModel, model)
          } catch {
            case _@e =>
          }
        }
      }
    }
  }

  private def stopInternals () {
    if (lockUUID != null) {
      modelService.releaseLock(lockUUID)
    }
    this.exit()
  }
}
