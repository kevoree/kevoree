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

    scriptBuffer.append("tblock {\n")
    currentModel.getDeployUnits.foreach{du => scriptBuffer.append("merge 'mvn:"+du.getGroupName+"/"+du.getUnitName+"/"+du.getVersion+"\n") }
    currentModel.getNodes.foreach( n => {
      val adapModel =kompareBean.kompare(emptyModel,currentModel,n.getName)
      val script = AdaptationModelWrapper.generateScriptFromAdaptModel(adapModel)
      val planScript = KevScriptWrapper.miniPlanKevScript(script)
      scriptBuffer.append(planScript.getTextualForm)
    })
    scriptBuffer.append("}\n")
    
    
    println("Script =>"+scriptBuffer.toString)
    
  }

}
