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
package org.kevoree.framework.template

import io.Source
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 20/01/12
 * Time: 10:12
 */

object MicroTemplate {

  def fromClassPath(path: String, clazz : Class[_]): String = {
    Source.fromInputStream(clazz.getClassLoader.getResourceAsStream(path), "utf-8").getLines().mkString("\n")
  }

  def fromClassPathReplace(path: String, clazz : Class[_], keyvals :  java.util.HashMap[String,String]): String = {
    Source.fromInputStream(clazz.getClassLoader.getResourceAsStream(path), "utf-8").getLines().map(line => {
      var repLine = line
      keyvals.foreach{ kv =>
        repLine = repLine.replace(kv._1,kv._2)
      }
      repLine
    }).mkString("\n")
  }
  
}