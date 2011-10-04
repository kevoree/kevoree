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
 import org.slf4j.LoggerFactory

trait ChannelTypeFragment extends KevoreeChannelFragment with ChannelFragment {

  val kevoree_internal_logger = LoggerFactory.getLogger(this.getClass)

  override def remoteDispatch(msg: Message): Object = {
    if (msg.inOut.booleanValue) {
      (this !? msg).asInstanceOf[Object]
    } else {
      this ! msg
      null
    }
  }

  private val portsBinded: HashMap[String, KevoreePort] = new HashMap[String, KevoreePort]()
  private var fragementBinded: scala.collection.immutable.HashMap[String, KevoreeChannelFragment] = scala.collection.immutable.HashMap[String, KevoreeChannelFragment]()

  //@BeanProperty
  var nodeName: String = ""

  // @BeanProperty
  def isStarted: Boolean = ct_started

  override def getNodeName = nodeName

  def setNodeName(n: String) {
    nodeName = n
  }

  //@BeanProperty
  var name: String = ""

  override def getName = name

  def setName(n: String) {
    name = n
  }

  //@BeanProperty
  var dictionary: HashMap[String, Object] = new HashMap[String, Object]()

  def setDictionary(d: HashMap[String, Object]) {
    dictionary = d
  }

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
          (delegate !? msg.getContent).asInstanceOf[Object]
        } else {
          (delegate ! msg.getContent);
          null
        }
      }
      case f: KevoreeChannelFragment =>
        msg.setDestChannelName(f.getName)
        msg.setDestNodeName(f.getNodeName)

        if (msg.inOut.booleanValue) {
          (delegate !? msg).asInstanceOf[Object]
        } else {
          (delegate ! msg);
          null
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
      try {
        d.keySet.foreach {
          v =>
            dictionary.put(v, d.get(v))
        }
        if (ct_started) {
          updateChannelFragment
        }
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Channel Instance Update Error !", e)
          reply(false)
        }
      }
    }
    case StartMessage if (!ct_started) => {
      try {
        startChannelFragment
        local_queue.start()
        ct_started = true
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Channel Instance Start Error !", e)
          reply(false)
        }
      }
    }
    case StopMessage if (ct_started) => {
      try {
        //TODO CHECK QUEUE SIZE AND SAVE STATE
        local_queue.forceStop
        stopChannelFragment
        ct_started = false
        reply(true)
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Channel Instance Stop Error !", e)
          reply(false)
        }
      }
    }
    case StopMessage if (!ct_started) => //IGNORE
    case StartMessage if (ct_started) => //IGNORE

    case msg: FragmentBindMessage => {

      kevoree_internal_logger.debug("FragmentBindMessage=>"+createPortKey(msg))
      val sender = this.createSender(msg.getFragmentNodeName, msg.getChannelName)
      val proxy = new KevoreeChannelFragmentProxy(msg.getFragmentNodeName, msg.getChannelName)
      proxy.setChannelSender(sender)
      fragementBinded += ((createPortKey(msg),proxy))
      proxy.start;
      reply(true)
    }
    case msg: FragmentUnbindMessage => {
      kevoree_internal_logger.debug("Try to unbind channel " + name)
      val actorPort : Option[KevoreeChannelFragment] = fragementBinded.get(createPortKey(msg))
      if (actorPort.isDefined) {
        actorPort.get.stop
        fragementBinded = fragementBinded.filter(p => p._1 != (createPortKey(msg)))
        reply(true)
      } else {
        kevoree_internal_logger.debug("Can't unbind Fragment " + createPortKey(msg))
        reply(false)
      }
    }
    case msg: PortBindMessage => {
      portsBinded.put(createPortKey(msg), msg.getProxy);
      reply(true)
    }
    case msg: PortUnbindMessage => {
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
        val msg2 = new Message
        msg2.setInOut(false)
        msg2.setContent(msg)
        dispatch(msg2)
      }

    }
  }


  /* LifeCycle Method */
  def startChannelFragment() : Unit = {}

  def stopChannelFragment() : Unit = {}

  def updateChannelFragment: Unit = {}


}
