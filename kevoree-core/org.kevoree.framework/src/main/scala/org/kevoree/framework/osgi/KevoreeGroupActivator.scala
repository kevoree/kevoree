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
package org.kevoree.framework.osgi

import org.kevoree.framework.message._
import java.util.Hashtable
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.framework.{AbstractComponentType, KevoreeGroup, Constants}
import org.osgi.framework.{ServiceRegistration, BundleContext, BundleActivator}

abstract class KevoreeGroupActivator extends BundleActivator with KevoreeInstanceActivator {

  def callFactory(): KevoreeGroup

  var nodeName: String = ""
  var instanceName: String = ""
  var groupActor: KevoreeGroup = null
  var bundleContext: BundleContext = null

  var mainService : ServiceRegistration = null

  def setNodeName(n : String) {
    nodeName = n
  }
  def setInstanceName(in : String){
    instanceName = in
  }


  override def start(bc: BundleContext) {
    bundleContext = bc
    /* SEARCH HEADERS VALUE */
    import scala.collection.JavaConversions._
   // nodeName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_NODE_NAME_HEADER).get._2.toString
   // instanceName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_INSTANCE_NAME_HEADER).get._2.toString
    /* Create component actor */
    groupActor = callFactory()


    /* Start actor */
    groupActor.start
    /* Expose component in OSGI */
    val props = new Hashtable[String, String]()
    props.put(Constants.KEVOREE_NODE_NAME, nodeName)
    props.put(Constants.KEVOREE_INSTANCE_NAME, instanceName)
    mainService = bc.registerService(classOf[KevoreeGroup].getName, groupActor, props);


    
    /* PUT INITIAL PROPERTIES */
    groupActor.getDictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bc.getBundle)
    groupActor.asInstanceOf[KevoreeGroup].setName(instanceName)
    groupActor.asInstanceOf[KevoreeGroup].setNodeName(nodeName)

    val sr = bc.getServiceReference(classOf[KevoreeModelHandlerService].getName());
    val modelHandlerService : KevoreeModelHandlerService = bc.getService(sr).asInstanceOf[KevoreeModelHandlerService];
    groupActor.asInstanceOf[KevoreeGroup].setMhandler(modelHandlerService)


    val sr2 = bc.getServiceReference(classOf[KevScriptEngineFactory].getName());
    val kevSFHandlerService : KevScriptEngineFactory = bc.getService(sr2).asInstanceOf[KevScriptEngineFactory];
    groupActor.asInstanceOf[KevoreeGroup].setKevScriptEngineFactory(kevSFHandlerService)

    //channelActor.startChannelFragment //DEPRECATED DONE BY DEPLOY
  }

  override def stop(bc: BundleContext) {
    if (groupActor.asInstanceOf[KevoreeGroup].getIsStarted) {
      groupActor !? StopMessage
      println("Stopping => " + instanceName)
    }

    groupActor.stop
    groupActor = null

    mainService.unregister()

  }
}
