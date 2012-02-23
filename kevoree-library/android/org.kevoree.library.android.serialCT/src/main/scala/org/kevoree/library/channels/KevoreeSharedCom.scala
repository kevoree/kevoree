package org.kevoree.library.channels;

import actors.DaemonActor


object KevoreeSharedCom extends DaemonActor {

  def killAll() {
    port_cache.foreach {
      port =>
        port._2.killConnection()
    }
    port_cache.clear()
  }


  /* PORT COM OBSERVER */
  override def exit() = {
    killAll()
    super.exit()
  }

  override def exit(reason: AnyRef) = {
    killAll()
    super.exit(reason)
  }

  private val observers: scala.collection.mutable.HashMap[String, List[ContentListener]] = scala.collection.mutable.HashMap()

  def addObserver(portName: String, c: ContentListener) {
    observers.put(portName, observers.get(portName).getOrElse(List()) ++ List(c))
    getOrCreate(portName)
  }

  def removeObserver(portName: String, c: ContentListener) {
    observers.put(portName, observers.get(portName).getOrElse(List()).filterNot(p => p == c))
    //CLEANUP

    observers.foreach {
      obs =>
        if (obs._2.isEmpty) {
          port_cache.get(obs._1).map(p => p.killConnection())
        }
    }
  }

  def notifyObservers(portName: String, content: String) {
    observers.get(portName).getOrElse(List()).foreach {
      obs =>
        obs.recContent(content)
    }
  }

  case class ASYNC_SEND(portName: String, content: String)

  case class SYNC_SEND(portName: String, content: String, contentToWait: String, timeout: Long)

  def sendSynch(portName: String, content: String, contentToWait: String, timeout: Long): Boolean = {
    (this !? SYNC_SEND(portName: String, content: String, contentToWait: String, timeout: Long)).asInstanceOf[Boolean]
  }

  case class LOCK_PORT(portName: String)

  def lockPort(portName: String): Boolean = {
    (this !? LOCK_PORT(portName)).asInstanceOf[Boolean]
  }

  case class UNLOCK_PORT(portName: String)

  def unlockPort(portName: String): Boolean = {
    (this !? UNLOCK_PORT(portName)).asInstanceOf[Boolean]
  }

  case class ADD_LISTENER(portName: String, cl: ContentListener)

  case class REMOVE_LISTENER(portName: String, cl: ContentListener)


  val port_cache = scala.collection.mutable.HashMap[String, TwoWayActors]()
  start()

  def act() {
    loop {
      react {
        case ADD_LISTENER(pn, cl) => addObserver(pn, cl)
        case REMOVE_LISTENER(pn, cl) => removeObserver(pn, cl)
        case LOCK_PORT(pn) => {
          port_cache.get(pn).map(twa => {
            twa.killConnection(); port_cache.remove(pn)
          })
          reply(true)
          println("Port lock " + pn)
          react {
            case UNLOCK_PORT(pn) => {
              println("Port unlock " + pn)
              if (observers.contains(pn)) {
                getOrCreate(pn)
              }
              reply(true)
            }

          }
        }
        case ASYNC_SEND(pn, c) => {
          getOrCreate(pn).send(c)
        }
        case SYNC_SEND(pn, c, wc, timeout) => {
          reply(getOrCreate(pn).sendAndWait(c, wc, timeout))
        }
        case _ => println("warn unknow message !!!")
      }
    }
  }

  def getOrCreate(portName: String): TwoWayActors = {
    port_cache.get(portName).getOrElse {
      val newTWA = new TwoWayActors(portName)
      port_cache.put(portName, newTWA)
      newTWA
    }
  }

  def send(portName: String, payload: String) {
    this ! ASYNC_SEND(portName: String, payload: String)
  }

}