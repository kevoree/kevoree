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

package org.kevoree.framework

import java.util.HashMap
import org.kevoree.framework.message._
import scala.actors.Actor
import scala.collection.JavaConversions._
trait ChannelTypeFragment extends KevoreeChannelFragment with ChannelFragment {

  override def remoteDispatch(msg: Message): Object = {
    if (msg.inOut.booleanValue) {
      (this !? msg).asInstanceOf[Object]
    } else {
      this ! msg
      null
    }
  }

  private var portsBinded: HashMap[String, KevoreePort] = new HashMap[String, KevoreePort]()
  private var fragementBinded: HashMap[String, KevoreeChannelFragment] = new HashMap[String, KevoreeChannelFragment]()

  //@BeanProperty
  var nodeName: String = ""

  // @BeanProperty
  def isStarted: Boolean = ct_started

  override def getNodeName = nodeName

  def setNodeName(n: String) = {
    nodeName = n
  }

  //@BeanProperty
  var name: String = ""

  override def getName = name

  def setName(n: String) = {
    name = n
  }

  //@BeanProperty
  var dictionary: HashMap[String, Object] = new HashMap[String, Object]()

  def setDictionary(d: HashMap[String, Object]) = dictionary = d

  override def getDictionary(): HashMap[String, Object] = dictionary

  override def getBindedPorts(): java.util.List[KevoreePort] = {
    portsBinded.values.toList
  }

  //OVERRIDE BY FACTORY
  override def getOtherFragments(): java.util.List[KevoreeChannelFragment] = {
    fragementBinded.values.toList
  }

  override def forward(delegate: KevoreeActor, msg: Message): Object = {

    delegate match {
      case p: KevoreePort => {
        if (msg.inOut.booleanValue) {
          return (delegate !? msg.getContent).asInstanceOf[Object]
        } else {
          (delegate ! msg.getContent);
          return null
        }
      }
      case f: KevoreeChannelFragment =>
        msg.setDestChannelName(f.getName)
        msg.setDestNodeName(f.getNodeName)

        if (msg.inOut.booleanValue) {
          return (delegate !? msg).asInstanceOf[Object]
        } else {
          (delegate ! msg);
          return null
        }
      case _ => println("Call on forward on bad object type ! => Only Port or Channel accepted"); return null
    }
  }

  private def createPortKey(a: Any): String = {
    a match {
      case msg: PortBindMessage => msg.getNodeName + "-" + msg.getComponentName + "-" + msg.getPortName
      case msg: PortUnbindMessage => msg.getNodeName + "-" + msg.getComponentName + "-" + msg.getPortName
      case msg: FragmentBindMessage => msg.getChannelName + "-" + msg.getFragmentNodeName
      case msg: FragmentUnbindMessage => msg.getChannelName + "-" + msg.getFragmentNodeName
      case _ => ""
    }
  }


  private var ct_started: Boolean = false

  override def internal_process(msgg: Any) = msgg match {

    case UpdateDictionaryMessage(d) => {
      d.keySet.foreach {
        v =>
          dictionary.put(v, d.get(v))
      }
      //updateChannelFragment
      if (ct_started) {
        new Actor {
          def act = updateChannelFragment
        }.start()
      }

      reply(true)
    }

    case StartMessage if (!ct_started) => {
      // new Actor {
      //  def act = {
      startChannelFragment;
      println("Starting Channel Internal queue");
      local_queue.start; //isStarted=true
      //}
      // }.start
      ct_started = true
      reply(true)
    }
    case StopMessage if (ct_started) => {
      // new Actor {
      //   def act = {
      //TODO CHECK QUEUE SIZE AND SAVE STATE
      local_queue.forceStop
      stopChannelFragment
      //isStarted=false
      //   }

      // }.start

      ct_started = false
      reply(true)
    }
    case StopMessage if (!ct_started) =>
    case StartMessage if (ct_started) =>

    case msg: FragmentBindMessage => {
      var sender = this.createSender(msg.getFragmentNodeName, msg.getChannelName)
      var proxy = new KevoreeChannelFragmentProxy(msg.getFragmentNodeName, msg.getChannelName)
      proxy.setChannelSender(sender)
      fragementBinded.put(createPortKey(msg), proxy);
      proxy.start;
      reply(true)
    }
    case msg: FragmentUnbindMessage => {
      println("Try to unbind channel " + name)
      var actorPort = fragementBinded.get(createPortKey(msg))
      if (actorPort != null) {
        actorPort.stop
        fragementBinded.remove(createPortKey(msg))
      } else {
        println("Can't unbind Fragment " + createPortKey(msg))
      }
      reply(true)
    }
    case msg: PortBindMessage => {
      println("Addkey=" + createPortKey(msg));
      portsBinded.put(createPortKey(msg), msg.getProxy);
      reply(true)
    }
    case msg: PortUnbindMessage => {
      println("Removekey=" + createPortKey(msg));
      portsBinded.remove(createPortKey(msg));
      reply(true)
    }
    //USE CASE A MSG REC BY OTHER FRAGMENT
    case msg: Message => local_queue forward msg
    case msg: MethodCallMessage => local_queue forward msg
    case msg: Object => local_queue forward msg
    case _@msg => local_queue forward msg
  }

  val local_queue = new KevoreeActor {
    override def internal_process(msgg: Any) = msgg match {
      case msg: Message => {
        if (msg.inOut.booleanValue) {
          reply(dispatch(msg))
        } else {
          dispatch(msg)
        }
      }
      case msg: MethodCallMessage => {
        var msg2 = new Message
        msg2.setInOut(true)
        msg2.setContent(msg)
        reply(dispatch(msg2))
      }
      case msg: Object => {
        var msg2 = new Message
        msg2.setInOut(false)
        msg2.setContent(msg)
        dispatch(msg2)
      }
      case _@msg => {
        println("Msg does not seem to be an object =>" + msg)
        var msg2 = new Message
        msg2.setInOut(false)
        msg2.setContent(msg)
        dispatch(msg2)
      }

    }
  }


  /* LifeCycle Method */
  def startChannelFragment: Unit = {}

  def stopChannelFragment: Unit = {}

  def updateChannelFragment: Unit = {}


}
