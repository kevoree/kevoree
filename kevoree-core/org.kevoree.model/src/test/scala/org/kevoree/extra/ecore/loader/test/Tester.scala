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
package org.kevoree

import cloner.ModelCloner
import java.io.{ByteArrayOutputStream, File}
import org.kevoree.loader.{XMIModelLoader, ModelLoader}
import org.kevoree.serializer.{XMIModelSerializer, ModelSerializer}
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/10/11
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  val current = System.currentTimeMillis()
  /*
for(i <- 0 until 30){
  ContainerRootLoader.loadModel(new File(("/Users/duke/Documents/dev/dukeboard/kevoree-experiment/org.kevoree.experiment.smartForest/duke.irisa.fr-duke.irisa.fr-generated/kevoreeIndividualModel.kev")));
}*/

  val loader = new XMIModelLoader()
  val m = loader.loadModelFromPath(new File(getClass.getResource("/unomas.kev").toURI())).get(0).asInstanceOf[ContainerRoot]



  /*
val node = m.getNodes.find(n => n.getName == "node0").get
val console = node.getComponents.find(c => c.getName == "FakeConso75").get
val port = console.getProvided.find(p=> p.getPortTypeRef.getName == "showText").get
val channel = m.getHubs.find(h => h.getName == "defMSG122").get

val b = KevoreeFactory.createMBinding
b.setHub(channel)
b.setPort(port)
m.addMBindings(b)
  */

  m.getMBindings.foreach { mb =>
    println("---------->")
    val p = mb.getPort
    println(mb.getPort+"-"+mb.getPort.getBindings.size+"-"+mb.getPort.getBindings.contains(mb))
    mb.setPort(null)
    println(mb.getPort+"-"+p.getBindings.size+"-"+p.getBindings.contains(mb))
    mb.setPort(p)
    println(mb.getPort+"-"+mb.getPort.getBindings.size+"-"+mb.getPort.getBindings.contains(mb))

    p.removeBindings(mb)
    println(mb.getPort+"-"+p.getBindings.size+"-"+p.getBindings.contains(mb))
    p.addBindings(mb)
    println(mb.getPort+"-"+p.getBindings.size+"-"+p.getBindings.contains(mb))


  }


  val cloner = new ModelCloner
  cloner.clone(m)


  val serializer = new XMIModelSerializer
  val oo = new ByteArrayOutputStream
  serializer.serialize(m,oo)
  println(System.currentTimeMillis() - current)
  println(new String(oo.toByteArray))

}