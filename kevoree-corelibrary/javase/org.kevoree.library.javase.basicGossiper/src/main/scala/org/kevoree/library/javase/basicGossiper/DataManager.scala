package org.kevoree.library.javase.basicGossiper

import java.util.UUID
import org.kevoree.library.basicGossiper.protocol.version.Version.VectorClock

trait DataManager {

  def getData (uuid: UUID): (VectorClock, Any)

  def setData (uuid: UUID, tuple: (VectorClock, Any), source: String) : Boolean

  def getUUIDVectorClock (uuid: UUID): VectorClock

  def getUUIDVectorClocks: java.util.Map[UUID, VectorClock]

  def checkForGarbage (uuids: List[UUID], source : String)
}