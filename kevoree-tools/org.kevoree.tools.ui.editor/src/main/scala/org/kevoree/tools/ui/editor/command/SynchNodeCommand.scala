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
///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package org.kevoree.tools.ui.editor.command
//
//import java.io.ByteArrayOutputStream
//import java.net.InetSocketAddress
////import org.kevoree.framework.KevoreeXmiHelper
////import org.kevoree.framework.bus.netty.remote.TcpClientRemoteActor
////import org.kevoree.framework.message.Art2ModelSynchMessage
//import org.kevoree.tools.ui.editor.KevoreeUIKernel
////import org.kevoree.framework.Constants
////import org.kevoree.framework.JacksonSerializer._
//import scala.reflect.BeanProperty
//import scala.collection.JavaConversions._
//
//class SynchNodeCommand extends Command {
//
//  @BeanProperty
//  var kernel : KevoreeUIKernel = null
//
//  @BeanProperty
//  var destNodeName : String = null
//  //   client.start
//
//  def execute(p :Object) {
//
//    //USE Bonjour search to discover ART2 Node IP & Port
//
//
//    //var netLink = IPCache.getNode(destNodeName)
//
//    /* TO REMOVE */
//   // var de = Tuple2("","")
//   //var netLink = Some(Set(de))
///*
//    netLink match {
//      case Some(l) if(l.size > 0)=> {
//          var i = 0
//          var listIP = l.toList
//          var client = new TcpClientRemoteActor(null,2000) {
//            def getRemoteAddr : InetSocketAddress = {
//
//              var addr = new InetSocketAddress(listIP.get(i)._1,listIP.get(i)._2)
//              i = i +1
//              if(i == l.size){ i = 0 }
//
//             //var addr = new InetSocketAddress("192.168.1.103",8000)
//              addr
//            }
//          }
//          client.start
//          var outStream = new ByteArrayOutputStream
//          Art2XmiHelper.saveStream(outStream, kernel.getModelHandler.getActualModel)
//          outStream.flush
//          // var msg = outStream.toString
//          var msg = new Art2ModelSynchMessage
//          msg.setNodeSenderName("art2.editor")
//          msg.setNewModelAsString(outStream.toString)
//          outStream.close
//          client ! msg.toJSON
//          client.stop
//*/
//
//        //}
//     //   case _ => println("No IP found to push model to => "+destNodeName)
//   // }
//
//  }
//
//}
