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
import org.kevoree.framework.KevoreeChannelFragment
import org.kevoree.framework.ChannelTypeFragment
import org.kevoree.framework.Constants
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._
import org.kevoree.framework.message._

abstract class KevoreeChannelFragmentActivator extends BundleActivator {

  def callFactory(): KevoreeChannelFragment

  var nodeName: String = ""
  var instanceName: String = ""
  var channelActor: KevoreeChannelFragment = null
  var bundleContext: BundleContext = null

  def start(bc: BundleContext) {
    bundleContext = bc
    /* SEARCH HEADERS VALUE */
    nodeName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_NODE_NAME_HEADER).get._2.toString
    instanceName = bc.getBundle.getHeaders.find(dic => dic._1 == Constants.KEVOREE_INSTANCE_NAME_HEADER).get._2.toString
    /* Create component actor */
    channelActor = callFactory()


    /* Start actor */
    channelActor.start
    /* Expose component in OSGI */
    var props = new Hashtable[String, String]()
    props.put(Constants.KEVOREE_NODE_NAME, nodeName)
    props.put(Constants.KEVOREE_INSTANCE_NAME, instanceName)
    bc.registerService(classOf[KevoreeChannelFragment].getName(), channelActor, props);

    /* PUT INITIAL PROPERTIES */
    channelActor.getDictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bc.getBundle)
    channelActor.asInstanceOf[ChannelTypeFragment].setName(instanceName)
    channelActor.asInstanceOf[ChannelTypeFragment].setNodeName(nodeName)

    //channelActor.startChannelFragment //DEPRECATED DONE BY DEPLOY
  }

  def stop(bc: BundleContext) {
    if(channelActor.asInstanceOf[ChannelTypeFragment].getIsStarted){
      channelActor ! StopMessage
      println("Stopping => " + instanceName)
    }



    channelActor.stop
    //channelActor.stopChannelFragment //DEPRECATED DONE BY DEPLOY
    channelActor = null
  }
}
