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
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.slf4j.LoggerFactory
import org.kevoree.tools.marShellTransform.{KevScriptWrapper, AdaptationModelWrapper}
import org.kevoree.{DeployUnit, KevoreeFactory}
import io.Source
import java.io.{FileWriter, File}

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

    val duS: scala.collection.mutable.HashSet[DeployUnit] = scala.collection.mutable.HashSet()
    currentModel.getTypeDefinitions.foreach {
      td =>
        td.getDeployUnits.foreach {
          du =>
            duS.add(du)
        }
    }

    duS.foreach {

      du => scriptBuffer.append("merge 'mvn:" + du.getGroupName + "/" + du.getUnitName + "/" + (if(du.getVersion==KevoreeFactory.getVersion){"{kevoree.version}"}else {du.getVersion})  + "'\n")
    }

    currentModel.getNodes.foreach(n => {
      scriptBuffer.append("addNode " + n.getName + ":" + n.getTypeDefinition.getName + "\n")
    })

    currentModel.getNodes.foreach(n => {
      val adapModel = kompareBean.kompare(emptyModel, currentModel, n.getName)
      val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adapModel)
      val planScript = KevScriptWrapper.miniPlanKevScript(script)
      scriptBuffer.append(planScript.getTextualForm)
    })
    scriptBuffer.append("}\n")

    if(p.isInstanceOf[String]){
      val f = new File(p.asInstanceOf[String]);
      val fw = new FileWriter(f)
      try {
        fw.write(scriptBuffer.toString)
      } finally {
        fw.close()
      }
    }

  }

}
