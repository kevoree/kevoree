package org.kevoree.library.javase.conflictSolver

import org.kevoree.ContainerRoot
import org.kevoree.library.basicGossiper.protocol.version.Version.VectorClock

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/11/12
 * Time: 12:50
 */
trait ConflictSolver {

  def resolve(current : Tuple2[VectorClock,ContainerRoot],proposed : Tuple2[VectorClock,ContainerRoot], sourceNodeName : String, currentNodeName : String) : ContainerRoot

}
