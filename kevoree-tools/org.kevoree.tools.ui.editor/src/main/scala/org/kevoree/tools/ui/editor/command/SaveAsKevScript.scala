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

import org.kevoree.cloner.ModelCloner
import org.kevoree.tools.marShell.ast.MergeStatement
import org.kevoree.tools.ui.editor.{ModelHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import org.kevoree.tools.marShellTransform.{KevScriptWrapper, AdaptationModelWrapper}
import org.kevoree.{DeployUnit, KevoreeFactory}
import io.Source
import java.io.{FileWriter, File}
import scala.collection.JavaConversions._
import collection.mutable


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

  val kompareBean = new org.kevoree.kompare.KevoreeKompareBean()

  def execute(p: AnyRef) {

    var alreadyGeneratedMergeStmt = List[String]()

    val modelCloner = new ModelCloner()

    val emptyModel = modelCloner.clone(kernel.getModelHandler.getActualModel)
    emptyModel.removeAllHubs()
    emptyModel.removeAllGroups()
    emptyModel.removeAllNodes()
    emptyModel.removeAllMBindings()
    emptyModel.removeAllNodeNetworks()

    val currentModel = kernel.getModelHandler.getActualModel

    val scriptBuffer = new StringBuffer()
    scriptBuffer.append(" {\n")

    /*
    val duS: scala.collection.mutable.HashSet[DeployUnit] = scala.collection.mutable.HashSet()
    currentModel.getTypeDefinitions.foreach {
      td =>
        td.getDeployUnits.foreach {
          du =>
            duS.add(du)
        }
    }  */

    currentModel.getRepositories.foreach {
      repo =>
        scriptBuffer.append("addRepo \"" + repo.getUrl + "\"\n")
    }

    /*
    duS.foreach {
      du => scriptBuffer.append("merge 'mvn:" + du.getGroupName + "/" + du.getUnitName + "/" + (if (du.getVersion == ModelHelper.kevoreeFactory.getVersion) {
        "{kevoree.version}"
      } else {
        du.getVersion
      }) + "'\n")
    }*/

    currentModel.getNodes.foreach{
      node =>
      node.getTypeDefinition.getDeployUnits.foreach {
        deployUnit =>
          scriptBuffer.append("merge 'mvn:"+deployUnit.getGroupName+"/"+deployUnit.getUnitName+"/"+deployUnit.getVersion + "'\n")
      }
    }



    currentModel.getNodes.foreach(n => {
      val adapModel = kompareBean.kompare(emptyModel, currentModel, n.getName)
      val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adapModel)
      //val planScript = KevScriptWrapper.miniPlanKevScript(script)
      script.blocks.foreach {
        b =>
          b.l.filter(s => s.isInstanceOf[MergeStatement]).foreach {
            s =>
              if (!alreadyGeneratedMergeStmt.contains(s.getTextualForm)) {
                alreadyGeneratedMergeStmt = alreadyGeneratedMergeStmt ++ List(s.getTextualForm)
                scriptBuffer.append(s.getTextualForm + "\n")
              }
          }
      }


      scriptBuffer.append("addNode " + n.getName + ":" + n.getTypeDefinition.getName + "\n")
      val dico = n.getDictionary
      if (dico != null) {
        scriptBuffer.append("updateDictionary " + n.getName + "{")
        scriptBuffer.append(dico.getValues.map(v => v.getAttribute.getName + "=\"" + v.getValue + "\"").mkString(","))
        scriptBuffer.append("}\n")
      }
    })

    currentModel.getNodes.foreach(n => {
      val adapModel = kompareBean.kompare(emptyModel, currentModel, n.getName)
      val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adapModel)
      val planScript = KevScriptWrapper.miniPlanKevScript(script)
      scriptBuffer.append(planScript.getTextualForm)

      n.getHosts.foreach {
        child =>
          scriptBuffer.append("addChild " + child.getName + "@" + n.getName + "\n")
      }
    })

    currentModel.getGroups.foreach {
      g =>
        scriptBuffer.append("addGroup " + g.getName + ":" + g.getTypeDefinition.getName + "\n")
        val dico = g.getDictionary
        if (dico != null) {
          val localVals = dico.getValues.filter(v => v.getTargetNode == null)
          if (localVals.size > 0) {
            scriptBuffer.append("updateDictionary " + g.getName + "{")
            scriptBuffer.append(localVals.map(v => v.getAttribute.getName + "=\"" + v.getValue + "\"").mkString(","))
          }
        }

        g.getSubNodes.foreach {
          subN =>
            scriptBuffer.append("addToGroup " + g.getName + " " + subN.getName + "\n")
            val dico = g.getDictionary
            if (dico != null) {
              val localVals = dico.getValues.filter(v => v.getTargetNode != null && v.getTargetNode == subN)
              if (localVals.size > 0) {
                scriptBuffer.append("updateDictionary " + g.getName + "{")
                scriptBuffer.append(localVals.map(v => v.getAttribute.getName + "=\"" + v.getValue + "\"").mkString(","))
                scriptBuffer.append("}@" + subN.getName + "\n")
              }

            }


          //updateDictionary mysockChannel { port="10000"}@node1,{ port="10001"}@node2

        }
    }




    scriptBuffer.append("}\n")

    if (p.isInstanceOf[String]) {
      val f = new File(p.asInstanceOf[String]);
      if (f.exists()) {
        f.delete()
      }
      val fw = new FileWriter(f)
      try {
        fw.write(scriptBuffer.toString)
      } finally {
        fw.close()
      }
    }
    if (p.isInstanceOf[StringBuffer]) {
      p.asInstanceOf[StringBuffer].append(scriptBuffer.toString)
    }

  }

}
