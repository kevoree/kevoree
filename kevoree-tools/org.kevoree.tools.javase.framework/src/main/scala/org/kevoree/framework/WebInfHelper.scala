package org.kevoree.framework

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

import java.util.HashMap
import xml.{Node, XML}
import java.io.{FileWriter, File}
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/01/12
 * Time: 09:13
 */

object WebInfHelper extends App {

  val dictionary: HashMap[String, Object] = new java.util.HashMap[String, Object]
  dictionary.put("HUDSON_HOME", "bvalue_tu_put")
  println(setWebInfParams(new File("/var/folders/w0/jcb58c294sq41fgc_m2b74440000gn/T/-t-5692225202489535855-t-"), dictionary))

  def setWebInfParams(warDir: File, dictionary: HashMap[String, Object]): File = {
    val f = new File(warDir.getAbsolutePath + File.separator + "web.xml")
    try {
      val xmlnode = XML.loadFile(f)

      var listChilds = List[Node]()
      xmlnode.child.foreach {
        cNode =>
          cNode.label match {
            case "env-entry" => { //NOOP
            }
            case _@e => listChilds = listChilds ++ List(cNode)
          }
      }

      dictionary.foreach{ dicP =>
        listChilds = listChilds ++ List(<env-entry>
            <env-entry-name>{dicP._1}</env-entry-name>
            <env-entry-type>java.lang.String</env-entry-type>
            <env-entry-value>{dicP._2}</env-entry-value>
          </env-entry>)
      }

      val newFile = new File(warDir.getAbsolutePath + File.separator + "web2.xml")
      val fileWriter = new FileWriter(newFile)
      fileWriter.write(xmlnode.copy(child=listChilds).toString())
      fileWriter.close()
      newFile
    } catch {
      case _@e => e.printStackTrace() ; null
    }
  }


}