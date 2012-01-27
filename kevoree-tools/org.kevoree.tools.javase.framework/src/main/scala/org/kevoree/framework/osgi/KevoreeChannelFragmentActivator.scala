package org.kevoree.framework.osgi

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

import java.util.Hashtable
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.framework._
import message.StopMessage
import org.osgi.framework.{ServiceRegistration, BundleActivator, BundleContext}

abstract class KevoreeChannelFragmentActivator extends BundleActivator with KevoreeInstanceActivator {

  def callFactory(): KevoreeChannelFragment

  var nodeName: String = ""
  var instanceName: String = ""
  var channelActor: KevoreeChannelFragment = null
  var bundleContext: BundleContext = null
  var mainService: ServiceRegistration = null


  def setNodeName(n: String) {
    nodeName = n
  }

  def setInstanceName(in: String) {
    instanceName = in
  }

  def start(bc: BundleContext) {
    bundleContext = bc
    channelActor = callFactory()
    channelActor.start()
    /* Expose component in OSGI */
    val props = new Hashtable[String, String]()
    props.put(Constants.KEVOREE_NODE_NAME, nodeName)
    props.put(Constants.KEVOREE_INSTANCE_NAME, instanceName)
    mainService = bc.registerService(classOf[KevoreeChannelFragment].getName, channelActor, props);

    /* PUT INITIAL PROPERTIES */
    if(bc != null){
      channelActor.getDictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bc.getBundle)
    }


    channelActor.asInstanceOf[ChannelTypeFragment].setName(instanceName)
    channelActor.asInstanceOf[ChannelTypeFragment].setNodeName(nodeName)
    val sr = bc.getServiceReference(classOf[KevoreeModelHandlerService].getName());
    val modelHandlerService: KevoreeModelHandlerService = bc.getService(sr).asInstanceOf[KevoreeModelHandlerService];
    channelActor.asInstanceOf[AbstractChannelFragment].setModelService(modelHandlerService)


    val sr2 = bc.getServiceReference(classOf[KevScriptEngineFactory].getName());
    val kevSFHandlerService: KevScriptEngineFactory = bc.getService(sr2).asInstanceOf[KevScriptEngineFactory];
    channelActor.asInstanceOf[AbstractChannelFragment].setKevScriptEngineFactory(kevSFHandlerService)


    //channelActor.startChannelFragment //DEPRECATED DONE BY DEPLOY
  }

  def stop(bc: BundleContext) {
    if (channelActor.asInstanceOf[ChannelTypeFragment].isStarted) {
      channelActor !? StopMessage
      println("Stopping => " + instanceName)
    }

    if(channelActor.asInstanceOf[AbstractChannelFragment].isInstanceOf[ModelHandlerServiceProxy]){
      channelActor.asInstanceOf[AbstractChannelFragment].asInstanceOf[ModelHandlerServiceProxy].stopProxy()
    }

    channelActor.stop
    //channelActor.stopChannelFragment //DEPRECATED DONE BY DEPLOY
    channelActor = null

    mainService.unregister()
  }
}
