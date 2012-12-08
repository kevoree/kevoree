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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.framework

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, BufferedOutputStream}
import java.util


/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 13/04/12
 * Time: 13:17
 */

object Tester extends App {

  /*
var model= KevoreeXmiHelper.load("/home/jed/Desktop/model.kev")
val output = new ByteArrayOutputStream()
KevoreeXmiHelper.saveCompressedStream(output,model)
val input=new ByteArrayInputStream(output.toByteArray());
var model2 = KevoreeXmiHelper.loadCompressedStream(input)
*/

  /*
  val map = new util.HashMap[String, String]()
  map.put("daf", "daf2")
  map.put("daf2", "daf22")
  map.put("daf3", "daf233")

  import scala.collection.JavaConversions._

  map.toMap.foreach {
    m =>
      map.remove(m._1)
  }

  println(map.size())
    */

  var model= KevoreeXmiHelper.load("/Users/duke/Desktop/badRemoveAllModel.kev")

  val cloner = new org.kevoree.cloner.ModelCloner
  val clonerModel = cloner.clone(model)

  clonerModel.removeAllHubs()


}