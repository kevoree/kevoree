package org.kevoree.library.javase.gossiperNetty

import java.util.UUID
import org.kevoree.library.gossiperNetty.protocol.version.Version.VectorClock

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 10:39
 */

trait DataManager {
  def stop ()

  def getData (uuid: UUID): (VectorClock, Any)

  def setData (uuid: UUID, tuple: (VectorClock, Any), source: String) : Boolean

//  def removeData (uuid: UUID, tuple: (VectorClock, Any))

  def getUUIDVectorClock (uuid: UUID): VectorClock

  def getUUIDVectorClocks(): java.util.Map[UUID, VectorClock]

//  def mergeClock (uid: UUID, v: VectorClock, source: String): VectorClock

  def checkForGarbage (uuids: List[UUID], source : String)
}