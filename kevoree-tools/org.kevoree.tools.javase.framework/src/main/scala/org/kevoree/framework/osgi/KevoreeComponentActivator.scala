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

import org.kevoree.framework._


/* ABSTRACT COMPONENT */
abstract class KevoreeComponentActivator extends KevoreeInstanceActivator {

  def callFactory(): KevoreeComponent
  var nodeName: String = ""
  var componentName: String = ""
  def setNodeName(n: String) {
    nodeName = n
  }
  def setInstanceName(in: String) {
    componentName = in
  }
  var componentActor: KevoreeComponent = null

  override def start() {
    /* SEARCH HEADERS VALUE */
    //nodeName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_NODE_NAME_HEADER).get._2.toString
    //componentName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_INSTANCE_NAME_HEADER).get._2.toString
    /* Create component actor */
    componentActor = callFactory()


    /* PUT INITIAL PROPERTIES */
    /*
    if (bundleContext != null) {
      componentActor.getKevoreeComponentType.getDictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bundleContext.getBundle)
    }*/


    componentActor.getKevoreeComponentType.asInstanceOf[AbstractComponentType].setName(componentName)
    componentActor.getKevoreeComponentType.asInstanceOf[AbstractComponentType].setNodeName(nodeName)
    componentActor.getKevoreeComponentType.asInstanceOf[AbstractComponentType].setModelService(modelHandlerService)
    componentActor.getKevoreeComponentType.asInstanceOf[AbstractComponentType].setKevScriptEngineFactory(kevScriptEngine)


    import scala.collection.JavaConversions._
    /* START NEEDPORT ACTOR */
    componentActor.getKevoreeComponentType.getNeededPorts.foreach {
      np => np._2.asInstanceOf[Port].startPort()
    }

    /* START HOSTED ACTOR */
    componentActor.getKevoreeComponentType.getHostedPorts.foreach {
      hp =>
        hp._2.asInstanceOf[Port].startPort()
      //hp._2.asInstanceOf[KevoreePort].pause
    }


  }

  override def stop() {
    
    if(componentActor == null){
      return
    }

    if (componentActor.isStarted) {
      componentActor.kInstanceStop(null)// !? StopMessage(null)
      println("Stopping => " + componentName)
    }

    //STOP PROXY MODEL
    if (componentActor.getKevoreeComponentType.getModelService.isInstanceOf[ModelHandlerServiceProxy]) {
      componentActor.getKevoreeComponentType.getModelService.asInstanceOf[ModelHandlerServiceProxy].stopProxy()
    }



    /* STOP NEEDED PORT */
    import scala.collection.JavaConversions._
    componentActor.getKevoreeComponentType.getNeededPorts.foreach {
      np => np._2.asInstanceOf[Port].stop()
    }
    /* STOP NEEDED PORT */
    componentActor.getKevoreeComponentType.getHostedPorts.foreach {
      hp => hp._2.asInstanceOf[Port].stop()
    }
    //componentActor.stop
    componentActor = null
/*
    services.foreach {
      service =>
        service.unregister()
    }*/

  }

  def getKInstance : KInstance = componentActor


}
