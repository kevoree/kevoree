package org.kevoree.library.jmdnsrest

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
import java.util.HashMap
import javax.jmdns.{ServiceEvent, ServiceListener, ServiceInfo, JmDNS}
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.message.PlatformModelUpdate
import actors.Actor
import org.slf4j.LoggerFactory
import java.net.InetAddress

/**
 * User: ffouquet
 * Date: 13/09/11
 * Time: 17:42
 */

class  JmDnsComponent(nodeName: String, groupName : String, modelPort: Int, modelHandler : KevoreeModelHandlerService,groupTypeName :String) {

  val logger = LoggerFactory.getLogger(this.getClass)
  var servicelistener : ServiceListener = null


  final val REMOTE_TYPE: String = "_kevoree-remote._tcp.local."


  val values = new HashMap[String, String]
  // values.put("modelPort", modelPort)

  // TODO Listen interfaces ?
  val jmdns = JmDNS.create()

  logger.debug(" JmDNS listen on "+jmdns.getInterface());

  servicelistener = new ServiceListener() {

    def serviceAdded(p1: ServiceEvent) {

      // Required to force serviceResolved to be called again
      // (after the first search)

      jmdns.requestServiceInfo(p1.getType(), p1.getName(), 1);

      /* val infos = jmdns.list(REMOTE_TYPE)
      infos.foreach {
        info =>
          val msg = new PlatformModelUpdate(info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, info.getInet4Addresses()(0).getHostAddress, "LAN", 100)
          modelHandler.asInstanceOf[Actor] ! msg
          val msg2 = new PlatformModelUpdate(info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_MODEL_PORT, info.getPort.toString, "LAN", 100)
          modelHandler.asInstanceOf[Actor] ! msg2
      }
      */

      val typeNames = new String(p1.getInfo.getTextBytes, "UTF-8");
      val typeNamesArray = typeNames.split("/")

      val msg = new PlatformModelUpdate(p1.getInfo().getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, p1.getInfo().getInet4Addresses()(0).getHostAddress, "LAN", 100,typeNamesArray(0),typeNamesArray(1))
      modelHandler.asInstanceOf[Actor] ! msg

      val msg2 = new PlatformModelUpdate(p1.getInfo().getName.trim(), org.kevoree.framework.Constants.KEVOREE_MODEL_PORT, p1.getInfo().getPort.toString, "LAN", 100,typeNamesArray(0),typeNamesArray(1))
      modelHandler.asInstanceOf[Actor] ! msg2

    }

    def serviceResolved(p1: ServiceEvent)
    {
      logger.debug("Service resolved: "+ p1.getInfo().getQualifiedName()+ " port:" + p1.getInfo().getPort());


       val typeNames = new String(p1.getInfo.getTextBytes, "UTF-8");
      val typeNamesArray = typeNames.split("/")

      val msg = new PlatformModelUpdate(p1.getInfo().getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, p1.getInfo().getInet4Addresses()(0).getHostAddress, "LAN", 100,typeNamesArray(0),typeNamesArray(1))
      modelHandler.asInstanceOf[Actor] ! msg

      val msg2 = new PlatformModelUpdate(p1.getInfo().getName.trim(), org.kevoree.framework.Constants.KEVOREE_MODEL_PORT, p1.getInfo().getPort.toString, "LAN", 100,typeNamesArray(0),typeNamesArray(1))
      modelHandler.asInstanceOf[Actor] ! msg2

      /*
      val infos = jmdns.list(REMOTE_TYPE)

      infos.foreach
      {
        info =>
          val msg = new PlatformModelUpdate(info.getName.trim(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP, info.getInet4Addresses()(0).getHostAddress, "LAN", 100)
          modelHandler.asInstanceOf[Actor] ! msg


        //CREATE EMPTY NODE IF NOT FOUND
        //CREATE FRAGMENT DEP PROPERTY
        //ASSIGN PORT

        val msg2 = new PlatformModelUpdate(info.getPort.toString, org.kevoree.framework.Constants.KEVOREE_MODEL_PORT, info.getPort.toString, "LAN", 100)
        modelHandler.asInstanceOf[Actor] ! msg2
      } */

    }

    def serviceRemoved(p1: ServiceEvent)
    {
      logger.debug("Service removed "+p1.getName)
    }
  };

  jmdns.addServiceListener(REMOTE_TYPE,servicelistener)




  new Thread() {
    override def run() {
      val nodeType = modelHandler.getLastModel.getNodes.find(n => n.getName == nodeName).get.getTypeDefinition.getName


      val pairservice: ServiceInfo = ServiceInfo.create(REMOTE_TYPE, nodeName, groupName , modelPort,"")
      pairservice.setText((groupTypeName+"/"+nodeType).getBytes("UTF-8"))

      logger.debug("nodeName "+nodeName+" groupName "+groupName+" modelPort "+modelPort +" groupTypeName "+groupTypeName+" nodeType "+nodeType)

      jmdns.registerService(pairservice)
    }
  }.start()

  def close() {
    if(servicelistener != null){jmdns.removeServiceListener(REMOTE_TYPE,servicelistener)}
    jmdns.unregisterAllServices()
    jmdns.close()
  }


}