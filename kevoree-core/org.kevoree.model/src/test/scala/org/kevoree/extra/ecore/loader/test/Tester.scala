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

import java.io.File
import loader.ContainerRootLoader
import org.kevoree.serializer.ModelSerializer
import xml.PrettyPrinter

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/10/11
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  val current = System.currentTimeMillis()
  val localModel = ContainerRootLoader.loadModel(new File(("/Users/duke/Documents/dev/dukeboard/kevoree-experiment/org.kevoree.experiment.smartForest/duke.irisa.fr-generated/models/Models580")));

  localModel match {
    case Some(m) => {


      val serializer = new ModelSerializer

      val result = serializer.serialize(m)
       println(System.currentTimeMillis() - current)
      val pp = new PrettyPrinter(3000,1)
      println(pp.format(result))
    }
    case None =>
  }
}