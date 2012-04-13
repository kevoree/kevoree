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

import reflect.BeanProperty
import org.slf4j.LoggerFactory
import org.kevoree.tools.ui.editor._
import java.lang.Thread
import org.kevoree.tools.aether.framework.{NodeTypeBootstrapHelper}
import javax.swing.{JProgressBar, JLabel}
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.{KevoreeFactory, ContainerRoot}

class SynchNodeTypeCommand extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)

  @BeanProperty
  var autoMerge: Boolean = true

  @BeanProperty
  var kernel: KevoreeUIKernel = null

  @BeanProperty
  var destNodeName: String = null

  @BeanProperty
  var viaGroupName: String = null

  val bootstrap = new NodeTypeBootstrapHelper
  val dummyKCL = new KevoreeJarClassLoader();

  /* Manually register */
  bootstrap.registerManuallyDeployUnit( "scala-library", "org.scala-lang", "2.10.0-M2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.aether.framework", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "cglib-nodep", "cglib", "2.2.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "slf4j-api", "org.slf4j", "1.6.4", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "slf4j-api", "org.slf4j", "1.6.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "objenesis", "org.objenesis", "1.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.adaptation.model", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.api", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.basechecker", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.core", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.framework", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.kcl", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.kompare", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.merger", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.model", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.annotation.api", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.javase.framework", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.extra.kserial", "org.kevoree.extra", "1.1", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "jna", "net.java.dev.jna", "3.3.0", dummyKCL);

  @BeanProperty
  var resultLabel : JLabel = null

  @BeanProperty
  var progressBar : JProgressBar = null

  def execute(p: AnyRef) {
    try {
      bootstrap.clear
      PositionedEMFHelper.updateModelUIMetaData(kernel);
      if(autoMerge){
        resultLabel.setText("Update model...")
        val autoUpdate = new AutoUpdateCommand
        autoUpdate.setKernel(kernel)
        autoUpdate.execute(null)
      }

      val model: ContainerRoot = kernel.getModelHandler.getActualModel

      new Thread() {
        override def run() {
          bootstrap.bootstrapGroupType(model, viaGroupName, new ModelHandlerServiceWrapper(kernel)) match {
            case Some(groupTypeInstance) => {
              groupTypeInstance.push(model,destNodeName)
              progressBar.setIndeterminate(false);
              progressBar.setEnabled(false);
              resultLabel.setText("Pushed ;-)")
            }

            case None => {
              logger.error("Error while bootstraping group type")
              progressBar.setIndeterminate(false);
              progressBar.setEnabled(false);
              resultLabel.setText("Error boot Group, bad merge ?")
            }
          }
        }
      }.start()
    } catch {
      case _@e => {
        logger.error("", e)
        progressBar.setIndeterminate(false);
        progressBar.setEnabled(false);
        resultLabel.setText(e.getMessage)
      }
    }
  }

}