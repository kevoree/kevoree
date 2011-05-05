package org.kevoree.platform.agent

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
import org.kevoree.ContainerRoot
import scala.collection.JavaConversions._
import java.net.NetworkInterface
import actors.DaemonActor
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePlatformHelper}
import java.io.File

class KevoreeRuntimeAgent extends DaemonActor {

  start()

  case class NEW_MODEL(model : ContainerRoot)

  def act() {
    loop {
      react {
        case NEW_MODEL(model) => {
          val file = new File("bootStrap.kev")
          if(file.exists){
            file.delete
          }

          KevoreeXmiHelper.save("bootStrap.kev",model)

          KevoreeNodeRunnerHandler.closeAllRunners()
          detectLocalNodeFromRuntime(model).foreach {
            t =>
              KevoreeNodeRunnerHandler.addRunner(t._1, t._2,"bootStrap.kev")
          }
        }
      }
    }
  }


  def processModel(model: ContainerRoot) {
       this ! NEW_MODEL(model)
  }


  private def detectLocalNodeFromRuntime(model: ContainerRoot): List[(String, Int)] = {

    var localIPS: List[String] = List()
    NetworkInterface.getNetworkInterfaces.foreach {
      netIT =>
        netIT.getInetAddresses.foreach {
          inetAddr =>
            localIPS = localIPS :+ inetAddr.getHostAddress
            localIPS = localIPS :+ inetAddr.getCanonicalHostName
            localIPS = localIPS :+ inetAddr.getHostAddress
        }
    }

    var result: List[(String, Int)] = List()
    model.getNodes.foreach {
      node =>
        var IP = KevoreePlatformHelper.getProperty(model, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP);
        if (IP == "") {
          IP = "127.0.0.1"
        }
        var PORT = KevoreePlatformHelper.getProperty(model, node.getName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT);
        if (PORT == "") {
          PORT = "8000"
        }

        if (localIPS.exists(localIP => localIP.contains(IP))) {
          result = result :+ (node.getName, Integer.parseInt(PORT))
        }

    }
    result
  }


}