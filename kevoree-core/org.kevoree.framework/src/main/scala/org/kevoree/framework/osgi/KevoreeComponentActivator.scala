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

package org.kevoree.framework.osgi

import java.util.Hashtable
import org.kevoree.framework.KevoreeComponent
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.Constants
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._


/* ABSTRACT COMPONENT */
abstract class KevoreeComponentActivator extends BundleActivator {

  def callFactory() : KevoreeComponent
  var nodeName : String = ""
  var componentName : String = ""
  var componentActor : KevoreeComponent = null
  var bundleContext : BundleContext = null

  def start(bc : BundleContext){
    bundleContext = bc
    /* SEARCH HEADERS VALUE */
    nodeName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_NODE_NAME_HEADER).get._2.toString
    componentName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_INSTANCE_NAME_HEADER).get._2.toString
    /* Create component actor */
    componentActor = callFactory()

    /* PUT INITIAL PROPERTIES */
    componentActor.getKevoreeComponentType.getDictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bc.getBundle)

    /* Start actor */
    componentActor.start
    /* Expose component in OSGI */
    var props = new Hashtable[String,String]()
    props.put(Constants.KEVOREE_NODE_NAME, nodeName)
    props.put(Constants.KEVOREE_INSTANCE_NAME, componentName)
    bc.registerService(classOf[KevoreeComponent].getName(), componentActor, props);

    /* Expose component provided port in OSGI */
    componentActor.getKevoreeComponentType.getHostedPorts.foreach{hpref=>
      var portProps = new Hashtable[String,String]()
      portProps.put(Constants.KEVOREE_NODE_NAME, nodeName)
      portProps.put(Constants.KEVOREE_INSTANCE_NAME, componentName)
      portProps.put(Constants.KEVOREE_PORT_NAME, hpref._1)
      bc.registerService(classOf[KevoreePort].getName(), hpref._2, portProps);
    }

    /* START NEEDPORT ACTOR */
    componentActor.getKevoreeComponentType.getNeededPorts.foreach{np=>np._2.asInstanceOf[KevoreePort].start}

    /* START HOSTED ACTOR */
    componentActor.getKevoreeComponentType.getHostedPorts.foreach{hp=>
      hp._2.asInstanceOf[KevoreePort].start
      //hp._2.asInstanceOf[KevoreePort].pause
    }


  }

  def stop(bc : BundleContext){
    /* STOP NEEDED PORT */
    componentActor.getKevoreeComponentType.getNeededPorts.foreach{np=>np._2.asInstanceOf[KevoreePort].stop}
    /* STOP NEEDED PORT */
    componentActor.getKevoreeComponentType.getHostedPorts.foreach{hp=>hp._2.asInstanceOf[KevoreePort].stop}

    componentActor.stop
    componentActor = null
  }

}
