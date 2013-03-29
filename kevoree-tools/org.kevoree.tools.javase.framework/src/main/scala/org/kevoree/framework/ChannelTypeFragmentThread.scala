/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.framework

import internal.MethodAnnotationResolver
import message._
import org.slf4j.LoggerFactory
import java.util.HashMap
import org.kevoree.ContainerRoot
import port.PausablePortThreadPoolExecutor
import java.util.concurrent.Callable
import org.kevoree.annotation.{LocalBindingUpdated, RemoteBindingUpdated}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/08/12
 * Time: 10:24
 */

trait ChannelTypeFragmentThread extends KevoreeChannelFragment with KInstance with ChannelFragment {

  val kevoree_internal_logger = LoggerFactory.getLogger(this.getClass)

  var eventHandler: event.MonitorEventHandler = null
  var pool = PausablePortThreadPoolExecutor.newPausableThreadPool(1)
  val resolver : MethodAnnotationResolver = new MethodAnnotationResolver(this.getClass);

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

  def isStarted: Boolean = ct_started

  override def getBindedPorts(): java.util.List[KevoreePort] = {
    import scala.collection.JavaConversions._
    portsBinded.values.toList
  }

  //OVERRIDE BY FACTORY
  override def getOtherFragments(): java.util.List[KevoreeChannelFragment] = {
    import scala.collection.JavaConversions._
    fragementBinded.values.toList
  }

  override def forward(delegate: KevoreeChannelFragment, inmsg: Message): Object = {
    val msg = inmsg.clone
    msg.setDestChannelName(delegate.getName)
    msg.setDestNodeName(delegate.getNodeName)
    if (msg.inOut.booleanValue) {
      (delegate !? msg).asInstanceOf[Object]
    } else {
      (delegate ! msg)
      null
    }
  }


  override def forward(delegate: KevoreePort, inmsg: Message): Object = {
    val msg = inmsg.clone
    if (msg.inOut.booleanValue) {
      (delegate !? msg.getContent).asInstanceOf[Object]
    } else {
      (delegate ! msg.getContent)
      null
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

  def kInstanceStart(tmodel: ContainerRoot): Boolean = {
    if (!ct_started) {
      try {
        getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(tmodel)
        startChannelFragment
        getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
        ct_started = true
        true
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Channel Instance Start Error !", e)
          false
        }
      }
    } else {
      false
    }
  }

  def kInstanceStop(tmodel: ContainerRoot): Boolean = {
    if (ct_started) {
      try {
        //TODO CHECK QUEUE SIZE AND SAVE STATE
        getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(tmodel)
        stopChannelFragment
        getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
        ct_started = false
        true
      } catch {
        case _@e => {
          kevoree_internal_logger.error("Kevoree Channel Instance Stop Error !", e)
          false
        }
      }
    } else {
      false
    }
  }

  def getDictionary: java.util.HashMap[String, Object]

  def getName: String

  def kUpdateDictionary(d: java.util.HashMap[String, AnyRef], cmodel: ContainerRoot): java.util.HashMap[String, AnyRef] = {
    try {
      import scala.collection.JavaConversions._
      val previousDictionary = getDictionary.clone()
      d.keySet.foreach {
        v =>
          getDictionary.put(v, d.get(v))
      }
      if (ct_started) {
        getModelService.asInstanceOf[ModelHandlerServiceProxy].setTempModel(cmodel)
        updateChannelFragment
        getModelService.asInstanceOf[ModelHandlerServiceProxy].unsetTempModel()
      }
      previousDictionary.asInstanceOf[java.util.HashMap[String, AnyRef]]
    } catch {
      case _@e => {
        kevoree_internal_logger.error("Kevoree Channel Instance Update Error !", e)
        null
      }
    }
  }


  def startC {
    pool = PausablePortThreadPoolExecutor.newPausableThreadPool(1)
    //start()
  }

  def stopC {
    if (pool != null) {
      pool.shutdownNow()
      pool = null
    }
  }

  case class AsyncCall(o: Any) extends Runnable {
    def run() {
      if (eventHandler != null) {
        eventHandler.triggerEvent(event.MonitorEvent(classOf[ChannelFragment], getName))
      }
      o match {
        case msg: Message => {
          dispatch(msg)
        }
        case msg: Object => {
          val msg2 = new Message
          msg2.setInOut(false)
          msg2.setContent(msg)
          dispatch(msg2)
        }
      }
    }
  }

  case class SyncCall(o: Any) extends Callable[Any] {
    def call(): Any = {
      if (eventHandler != null) {
        eventHandler.triggerEvent(event.MonitorEvent(classOf[ChannelFragment], getName))
      }
      o match {
        case msg: Message => {
          if (msg.inOut.booleanValue) {
            dispatch(msg)
          } else {
            dispatch(msg)
          }
        }
        case msg: MethodCallMessage => {
          val msg2 = new Message
          msg2.setInOut(true)
          msg2.setContent(msg)
          dispatch(msg2)
        }
      }
    }
  }

  /* LifeCycle Method */
  def startChannelFragment(): Unit = {}

  def stopChannelFragment(): Unit = {}

  def updateChannelFragment: Unit = {}

  def processAdminMsg(o: Any): Boolean = {
    pool.pause()
    val res = o match {
      case msg: FragmentBindMessage => {
        kevoree_internal_logger.debug("FragmentBindMessage=>" + createPortKey(msg))
        val sender = this.createSender(msg.getFragmentNodeName, msg.getChannelName)
        val proxy = new KevoreeChannelFragmentThreadProxy(msg.getFragmentNodeName, msg.getChannelName)
        proxy.setChannelSender(sender)
        fragementBinded += ((createPortKey(msg), proxy))
        proxy.startC
        val met = resolver.resolve(classOf[RemoteBindingUpdated])
        if (met != null) {
          met.invoke(this)
        }
        true
      }
      case msg: FragmentUnbindMessage => {
        kevoree_internal_logger.debug("Try to unbind channel " + getName)
        val actorPort: Option[KevoreeChannelFragment] = fragementBinded.get(createPortKey(msg))
        if (actorPort.isDefined) {
          actorPort.get.stopC
          fragementBinded = fragementBinded.filter(p => p._1 != (createPortKey(msg)))
          val met = resolver.resolve(classOf[RemoteBindingUpdated])
          if (met != null) {
            met.invoke(this)
          }
          true
        } else {
          kevoree_internal_logger.debug("Can't unbind Fragment " + createPortKey(msg))
          false
        }
      }
      case msg: PortBindMessage => {
        portsBinded.put(createPortKey(msg), msg.getProxy)
        val met = resolver.resolve(classOf[LocalBindingUpdated])
        if (met != null) {
          met.invoke(this)
        }
        true
      }
      case msg: PortUnbindMessage => {
        portsBinded.remove(createPortKey(msg))
        val met = resolver.resolve(classOf[LocalBindingUpdated])
        if (met != null) {
          met.invoke(this)
        }
        true
      }
    }
    pool.resume()
    res
  }

  def !(o: Any) {
    pool.submit(AsyncCall(o))
  }

  def !?(o: Any): Any = {
    pool.submit(SyncCall(o)).get()
  }

}
