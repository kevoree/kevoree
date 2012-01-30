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

import java.util.Hashtable
import org.kevoree.framework.message.StopMessage
import org.kevoree.framework.{ModelHandlerServiceProxy, KevoreeGroup, Constants}

abstract class KevoreeGroupActivator extends KevoreeInstanceActivator {

  def callFactory(): KevoreeGroup

  var nodeName: String = ""
  var instanceName: String = ""
  var groupActor: KevoreeGroup = null

  def setNodeName(n : String) {
    nodeName = n
  }
  def setInstanceName(in : String){
    instanceName = in
  }


  override def start() {
    /* SEARCH HEADERS VALUE */
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
    /*
    if(bundleContext != null){
      mainService = bundleContext.registerService(classOf[KevoreeGroup].getName, groupActor, props);
    }*/

    /* PUT INITIAL PROPERTIES */
    /*
    if(bundleContext != null){
      groupActor.getDictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bundleContext.getBundle)
    }*/

    groupActor.asInstanceOf[KevoreeGroup].setName(instanceName)
    groupActor.asInstanceOf[KevoreeGroup].setNodeName(nodeName)
    groupActor.asInstanceOf[KevoreeGroup].setModelService(modelHandlerService)
    groupActor.asInstanceOf[KevoreeGroup].setKevScriptEngineFactory(kevScriptEngine)
    //channelActor.startChannelFragment //DEPRECATED DONE BY DEPLOY
  }

  override def stop() {
    if (groupActor.asInstanceOf[KevoreeGroup].getIsStarted) {
      groupActor !? StopMessage
      println("Stopping => " + instanceName)
    }

    //STOP PROXY MODEL
    if(groupActor.getModelService().isInstanceOf[ModelHandlerServiceProxy]){
      groupActor.getModelService().asInstanceOf[ModelHandlerServiceProxy].stopProxy()
    }


    groupActor.stop
    groupActor = null
/*
    if(mainService != null){
      mainService.unregister()
    }*/


  }
}
