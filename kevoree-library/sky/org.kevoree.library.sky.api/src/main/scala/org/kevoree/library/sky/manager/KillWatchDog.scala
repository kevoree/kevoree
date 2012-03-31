package org.kevoree.library.sky.manager

import actors.{TIMEOUT, DaemonActor}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/09/11
 * Time: 10:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KillWatchDog(process : Process, timout : Int)  extends DaemonActor {
  case class STOP()

  def stop() {
    this ! STOP()
  }

  def act () {
    reactWithin(timout) {
      case TIMEOUT => process.destroy()
      case STOP() => this.exit()
    }
  }
}