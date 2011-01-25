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
//import org.kevoree.tools.ui.editor.KevoreeUIKernel
//import scala.reflect.BeanProperty
//import scala.collection.JavaConversions._
//
//class SynchPlatformCommand extends Command {
//
//  @BeanProperty
//  var kernel : KevoreeUIKernel = null
//
//  @BeanProperty
//  var destNodeName : String = null
//
//  def execute(p :Object) {
//
//
//    var outStream = new ByteArrayOutputStream
//   // KevoreeXmiHelper.saveStream(outStream, kernel.getModelHandler.getActualModel)
//    outStream.flush
//    // var msg = outStream.toString
//   // var msg = new ModelSynchMessage
//   // msg.setNodeSenderName("art2editor")
//   // msg.setNewModelAsString(outStream.toString)
////    Art2Cluster.push(msg.toJSON)
//    outStream.close
//
//  }
//
//}
