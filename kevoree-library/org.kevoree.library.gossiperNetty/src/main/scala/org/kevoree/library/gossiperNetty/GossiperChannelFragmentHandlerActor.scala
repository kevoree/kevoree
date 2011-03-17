/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.gossiperNetty

import java.util.HashMap
import scala.actors.DaemonActor

object GossiperChannelFragmentHandlerActor extends DaemonActor {

  start
  
  private var channels = new HashMap[String, GossiperChannel]()
  case class SET(channelName : String,fragment :GossiperChannel)
  case class GET(channelName : String)
  case class REMOVE(channelName : String)
  
  def getFragment(channelName : String) : GossiperChannel = {(this !? GET(channelName)).asInstanceOf[GossiperChannel]}
  def setFragment(channelName : String,fragment :GossiperChannel) = { this ! SET(channelName,fragment) }
  def removeFragement(channelName : String) = { this ! REMOVE(channelName) }
  
  def act() = {
    loop {
      react {
        case SET(channelName,fragment) => channels.put(channelName, fragment)
        case GET(channelName)=> reply(channels.get(channelName))
        case REMOVE(channelName)=> channels.remove(channelName)
      }
    }
  }
  
  
  
  
}
