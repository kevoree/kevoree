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
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import reflect.BeanProperty
import org.kevoree.core.basechecker.RootChecker
import scala.collection.JavaConversions._


class CheckCurrentModel extends Command {

  var kernel : KevoreeUIKernel = null
  def setKernel(k : KevoreeUIKernel) = kernel = k

  var checker = new RootChecker

  def execute(p :Object) {

    var result = checker.check(kernel.getModelHandler.getActualModel)

    result.foreach({ res=>
         println("Violation msg="+res.getMessage)
    })

    if(result.size == 0){
      println("Model checked !")
    }

  }

}