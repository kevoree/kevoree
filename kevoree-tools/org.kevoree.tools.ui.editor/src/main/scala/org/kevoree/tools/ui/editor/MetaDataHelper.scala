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
package org.kevoree.tools.ui.editor

import org.kevoree.Instance


object MetaDataHelper {

  def getMetaDataFromInstance(i: Instance): java.util.HashMap[String, String] = {
    val res = new java.util.HashMap[String, String]()
    if (i.getMetaData != null) {
      i.getMetaData.split(',').foreach {
        meta =>

          val values = meta.split('=')
          if (values.size >= 2) {
            res.put(values(0), values(1))
          }

      }
    }

    res
  }

  def containKeys(keys: java.util.List[String], map: java.util.HashMap[String, String]): Boolean = {
    import scala.collection.JavaConversions._
    keys.forall(key => map.contains(key))
  }


}