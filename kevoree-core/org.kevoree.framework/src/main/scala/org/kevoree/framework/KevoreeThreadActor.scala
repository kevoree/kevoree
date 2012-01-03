package org.kevoree.framework

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import scala.actors.Actor
import scala.actors.TIMEOUT

trait KevoreeThreadActor extends Actor {

  sealed case class ACTOR_ADMIN_MSG()

  sealed case class STOP_ACTOR(force: Boolean = false) extends ACTOR_ADMIN_MSG

  sealed case class PAUSE_ACTOR() extends ACTOR_ADMIN_MSG

  sealed case class RESUME_ACTOR() extends ACTOR_ADMIN_MSG

  def stop = this ! STOP_ACTOR()

  def forceStop = this ! STOP_ACTOR(true)

  def resume = this ! RESUME_ACTOR

  def pause = this ! PAUSE_ACTOR

  protected var pauseState = false

  def isInPause = pauseState

  protected def stopRequest(force: Boolean): Nothing = {
    if (force) {
      trapExitBoolean=false;
      exit() //Simply exit
    } else {
      emptyMailBox //Recusive call to the process fonction
    }
  }

  private def emptyMailBox: Nothing = reactWithin(0) {
    case adminMsg: ACTOR_ADMIN_MSG => println("Actor in stopping phase, ignore admin message")
    case TIMEOUT => trapExitBoolean=false;exit()
    case _@msg => internal_process(msg); emptyMailBox
  }

  def internal_process(msg: Any)

  var trapExitBoolean = true

  var deadMessageQueue : List[Any] = List()

  def act() {
    while (trapExitBoolean) {
      receive {

        case RESUME_ACTOR() if(!pauseState) => {
          pauseState = false;
          deadMessageQueue.foreach(msg => this ! msg )
          deadMessageQueue = List()
        }
        case PAUSE_ACTOR() if(pauseState) => { pauseState = true }
        case STOP_ACTOR(f) => {
          pauseState = false
          stopRequest(f)
        }
        case _@msg if(!pauseState) => internal_process(msg)
        case _ @ msg => {
          deadMessageQueue = deadMessageQueue ++ List(msg)
        }
      }
    }
  }


}
