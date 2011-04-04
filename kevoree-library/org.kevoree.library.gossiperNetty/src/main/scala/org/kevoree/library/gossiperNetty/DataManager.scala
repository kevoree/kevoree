/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.util.UUID
import org.kevoree.library.version.Version.VectorClock

trait DataManager[T] {
  def stop()
  def getData(uuid : UUID) : Tuple2[VectorClock,T]
  def setData(uuid : UUID, tuple : Tuple2[VectorClock,T])
  def removeData(uuid : UUID)
  def getUUIDVectorClock(uuid : UUID) : VectorClock
  def getUUIDVectorClocks() : java.util.Map[UUID, VectorClock]
  def mergeClock(uid: UUID,v : VectorClock):VectorClock
}
