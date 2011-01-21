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

package org.kevoree.framework.port

import org.kevoree.framework.KevoreeActor
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.message.FragmentUnbindMessage
import org.slf4j.LoggerFactory

trait Art2RequiredPort extends KevoreePort {

  def getName : String
  def getInOut : Boolean
  var delegate : Option[KevoreeActor] = None
  var logger = LoggerFactory.getLogger(this.getClass);

  private var isBind : Boolean = false
  def getIsBind = isBind

  private def bind(bindmsg : FragmentBindMessage) ={
    delegate = Some(bindmsg.getProxy)
    isBind = true
  }

  private def unbind(unbindmsg: FragmentUnbindMessage)= {
    delegate = None
    isBind = false
  }

  override def internal_process(msg : Any) = msg match {
    case bindmsg : FragmentBindMessage => { bind(bindmsg);reply(true) }
    case unbindmsg : FragmentUnbindMessage => { unbind(unbindmsg);reply(true) }
      /* other kind of message send */
    case _ @ msg => {
        delegate match {
          case None => react {
              case bindmsg : FragmentBindMessage => bind(bindmsg)
              case STOP_ACTOR(f) => pauseState = false ; stopRequest(f)
            }
          case Some(d) => {
              if(getInOut){
                try { reply(d !? msg) } catch { case _ @ e=> logger.error("error sending message  ",e) }
              } else {
                try { d ! msg } catch { case _ @ e=> logger.error("error sending message  ",e) }
              }
            }
        }
      }

  }
}
