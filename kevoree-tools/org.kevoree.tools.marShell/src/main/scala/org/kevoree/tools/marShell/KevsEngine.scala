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
package org.kevoree.tools.marShell

import interpreter.KevsInterpreterContext
import org.kevoree.ContainerRoot
import org.kevoree.cloner.ModelCloner
import parser.KevsParser
import interpreter.KevsInterpreterAspects._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/11/11
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */

object KevsEngine {

  val modelCloner = new ModelCloner
  val parser = new KevsParser

  def executeScript(script : String,model : ContainerRoot) : Option[ContainerRoot] = {

    parser.parseScript(script) match {
      case Some(s)=> {
        val inputModel = modelCloner.clone(model)
        if(s.interpret(KevsInterpreterContext(inputModel))){
          Some(inputModel)
        } else {

          None
        }
      }
      case None => None
    }
  }

}