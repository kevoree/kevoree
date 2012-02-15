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
package org.kevoree.core.impl

import actors.{Actor, DaemonActor}
import org.kevoree.api.service.core.handler.ModelListener
import org.kevoree.ContainerRoot

class KevoreeListeners extends DaemonActor {

  private val registeredListeners = new scala.collection.mutable.HashMap[ModelListener, Actor]()

  def addListener(l: ModelListener) = this ! AddListener(l)

  case class AddListener(l: ModelListener)

  def removeListener(l: ModelListener) = this ! RemoveListener(l)

  case class RemoveListener(l: ModelListener)

  def notifyAllListener() = this ! NotifyAll()

  case class NotifyAll()

  def stop() = this ! STOP_ACTOR()

  case class STOP_ACTOR()

  case class PREUPDATE(currentModel : ContainerRoot, proposedModel : ContainerRoot)

  def preUpdate(currentModel : ContainerRoot,pmodel : ContainerRoot ) : Boolean = {
    (this !? PREUPDATE(currentModel, pmodel)).asInstanceOf[Boolean]
  }
  
  def act() {
    loop {
      react {
        case PREUPDATE(currentModel, pmodel) => {
          reply(registeredListeners.forall(l=> l._1.preUpdate(currentModel, pmodel)))
        }
        case AddListener(l) => {
          val myActor = new ListenerActor(l)
          myActor.start()
          registeredListeners.put(l, myActor)
          Unit
        }
        case RemoveListener(l) => {
          registeredListeners.get(l) match {
            case Some(previousActor) => {
              previousActor ! STOP_LISTENER()
              registeredListeners.remove(l)
              Unit
            }
            case None => //NOOP
          }
        }
        case NotifyAll() => {
          registeredListeners.values.foreach {
            value =>
              value ! Notify()
          }
        }
        case STOP_ACTOR() => {
          registeredListeners.values.foreach {
            value =>
              value ! STOP_LISTENER()
          }
          exit()
        }
      }
    }
  }


  case class Notify()

  case class STOP_LISTENER()

  class ListenerActor(listener: ModelListener) extends DaemonActor {
    def act() {
      loop {
        react {
          case Notify() => listener.modelUpdated()
          case STOP_LISTENER() => exit()
        }
      }
    }
  }

}