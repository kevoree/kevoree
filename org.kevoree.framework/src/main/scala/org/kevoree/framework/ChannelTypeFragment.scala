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
import scala.actors.Actor
import scala.collection.JavaConversions._
import scala.reflect.BeanProperty

trait ChannelTypeFragment extends KevoreeChannelFragment with ChannelFragment {

  private var portsBinded : HashMap[String,KevoreePort] = new HashMap[String, KevoreePort]()
  private var fragementBinded : HashMap[String,KevoreeChannelFragment] = new HashMap[String, KevoreeChannelFragment]()

  var internal_logger = LoggerFactory.getLogger(this.getClass);

  @BeanProperty
  var nodeName : String = ""
  @BeanProperty
  var name : String = ""
  //@BeanProperty
  var dictionary : HashMap[String, Object] = new HashMap[String, Object]()
  def setDictionary(d : HashMap[String, Object]) = dictionary = d
  override def getDictionary() : HashMap[String, Object] = dictionary

  override def getBindedPorts():java.util.List[Port] = { portsBinded.values.toList } //OVERRIDE BY FACTORY
  override def getOtherFragments():java.util.List[KevoreeChannelFragment] = { fragementBinded.values.toList }
  override def forward(delegate : KevoreeActor,msg : Message) : Object = {

    delegate match {
      case p: KevoreePort => {
          if(msg.inOut.booleanValue){
            return (delegate !? msg.getContent).asInstanceOf[Object]
          } else {
            (delegate ! msg.getContent);return null
          }
        }
      case f : KevoreeChannelFragment =>
        msg.setDestChannelName(f.getName)
        msg.setDestNodeName(f.getNodeName)

        if(msg.inOut.booleanValue){
          return (delegate !? msg).asInstanceOf[Object]
        } else {
          (delegate ! msg);return null
        }
      case _ => println("WTF !!!");return null
    }
  }

  private def createPortKey(a : Any) : String = {
    a match {
      case msg : PortBindMessage => msg.getNodeName+"-"+msg.getComponentName+"-"+msg.getPortName
      case msg : PortUnbindMessage => msg.getNodeName+"-"+msg.getComponentName+"-"+msg.getPortName
      case msg : FragmentBindMessage => msg.getChannelName+"-"+msg.getFragmentNodeName
      case msg : FragmentUnbindMessage => msg.getChannelName+"-"+msg.getFragmentNodeName
      case _=>""
    }
  }

  override def internal_process(msgg : Any)= msgg match {
    
    case UpdateDictionaryMessage(d) => {
        d.keySet.foreach{v=>
          dictionary.put(v, d.get(v))
        }
        reply(true)
      }

    case StartMessage => {
        new Actor{ def act = startChannelFragment }.start
        reply(true)
      }
    case StopMessage => {
        new Actor{ def act = stopChannelFragment }.start
        reply(true)
      }

    case msg : FragmentBindMessage=> {
        fragementBinded.put(createPortKey(msg), msg.getProxy);
        msg.getProxy.start;
        reply(true)
      }
    case msg : FragmentUnbindMessage=> {
        internal_logger.info("Try to unbind channel "+name)
        var actorPort = fragementBinded.get(createPortKey(msg))
        if(actorPort!=null){
          actorPort.stop
          fragementBinded.remove(createPortKey(msg))
        } else {
          println("Can't unbind Fragment "+createPortKey(msg))
        }
        reply(true)
      }
    case msg : PortBindMessage => {
        internal_logger.info("Addkey="+createPortKey(msg));
        portsBinded.put(createPortKey(msg), msg.getProxy);
        reply(true)
      }
    case msg : PortUnbindMessage => {
        internal_logger.info("Removekey="+createPortKey(msg));
        portsBinded.remove(createPortKey(msg));
        reply(true)
      }
      //USE CASE A MSG REC BY OTHER FRAGMENT
    case msg : Message => {
        if(msg.inOut.booleanValue){
          reply(dispatch(msg))
        } else {
          dispatch(msg)
        }
      }
    case msg : MethodCallMessage =>{
        var msg2 = new Message
        msg2.setInOut(true)
        msg2.setContent(msg)
        reply(dispatch(msg2))
      }
    case msg : Object => {
        var msg2 = new Message
        msg2.setInOut(false)
        msg2.setContent(msg)
        dispatch(msg2)
      }
    case _ @ msg => {
        internal_logger.warn("Msg does not seem to be an object =>"+msg)
        var msg2 = new Message
        msg2.setInOut(false)
        msg2.setContent(msg)
        dispatch(msg2)
      }
  }


  def startChannelFragment : Unit = {}
  def stopChannelFragment : Unit = {}
  

}
