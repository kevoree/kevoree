package org.kevoree.library.javase.conflictSolver

import org.kevoree.library.basicGossiper.protocol.version.Version.VectorClock
import org.kevoree.ContainerRoot
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/11/12
 * Time: 13:36
 */
class DatePrioritySolver extends ConflictSolver {
  def resolve(current: (VectorClock, ContainerRoot), proposed: (VectorClock, ContainerRoot), sourceNodeName: String,currentNodeName : String) : ContainerRoot = {
    val localDate = new Date(current._1.getTimestamp)
    val remoteDate = new Date(proposed._1.getTimestamp)
    if (localDate.before(remoteDate)) {
      current._2
    } else {
      current._2
    }
  }
}
