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
package org.kevoree.tools.annotation.generator

import org.kevoree.NodeType

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/09/11
 * Time: 16:11
 */

object AdaptationPrimitiveMapping {

  var mappings = Map[NodeType, Map[String, String]]()

  def getMappings(nodeType : NodeType) : Map[String, String] = {mappings(nodeType)}

  def addMapping(nodeType : NodeType, name : String, className : String) {
    var nodeMapping = mappings(nodeType)
    nodeMapping = nodeMapping ++ Map(name -> className)
    mappings = mappings ++ Map(nodeType -> nodeMapping)
  }

  def clear() {
    mappings = Map[NodeType, Map[String, String]]()
  }

}