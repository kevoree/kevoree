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
import org.slf4j.LoggerFactory
import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShellTransform.{KevScriptWrapper, AdaptationModelWrapper}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/03/12
 * Time: 20:28
 */

class SaveAsKevScript extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  val kompareBean = new org.kevoree.kompare.KevoreeKompareBean();
  
  def execute(p: AnyRef) {
    val emptyModel = KevoreeFactory.createContainerRoot
    val currentModel = kernel.getModelHandler.getActualModel

    val scriptBuffer = new StringBuffer()
    scriptBuffer.append(" {\n")
    currentModel.getDeployUnits.foreach{du => scriptBuffer.append("merge 'mvn:"+du.getGroupName+"/"+du.getUnitName+"/"+du.getVersion+"'\n") }
    currentModel.getNodes.foreach( n => {
      scriptBuffer.append("addNode "+n.getName+":"+n.getTypeDefinition.getName+"\n")
      val adapModel =kompareBean.kompare(emptyModel,currentModel,n.getName)
      val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adapModel)
      val planScript = KevScriptWrapper.miniPlanKevScript(script)
      scriptBuffer.append(planScript.getTextualForm)
    })
    scriptBuffer.append("}\n")
    
    
    println("Script =================> \n"+scriptBuffer.toString)
    
  }

}
