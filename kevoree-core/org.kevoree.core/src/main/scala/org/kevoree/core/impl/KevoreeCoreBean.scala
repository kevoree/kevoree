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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.core.impl

import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.api.configuration.ConfigurationService
import org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.kompare.ModelKompareService
import org.kevoree.framework.KevoreeActor
import org.kevoree.framework.KevoreePlatformHelper
import org.kevoree.framework.KevoreeXmiHelper
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import org.kevoree.framework.merger.KevoreePlatformMerger
import org.kevoree.framework.message.PlatformModelUpdate
import org.kevoree.framework.message._
import scala.actors.Actor
import scala.collection.JavaConversions._
import org.kevoree.api.configuration.ConfigConstants

class KevoreeCoreBean extends KevoreeModelHandlerService with KevoreeActor {

  @BeanProperty var configService : ConfigurationService = null
  @BeanProperty var bundleContext : BundleContext = null;
  @BeanProperty var kompareService :org.kevoree.api.service.core.kompare.ModelKompareService = null
  @BeanProperty var deployService :org.kevoree.api.service.adaptation.deploy.KevoreeAdaptationDeployService = null
  @BeanProperty var nodeName : String = ""

  var models : java.util.ArrayList[ContainerRoot] = new java.util.ArrayList()
  var model : ContainerRoot = KevoreeFactory.eINSTANCE.createContainerRoot()

  var logger = LoggerFactory.getLogger(this.getClass);

  private def switchToNewModel(c : ContainerRoot)={
    models.add(model)
    model = c
  }

  override def start : Actor={
    logger.info("Start event : node name = "+configService.getProperty(ConfigConstants.KEVOREE_NODE_NAME))
    setNodeName(configService.getProperty(ConfigConstants.KEVOREE_NODE_NAME));
    super.start

    var lastModelssaved = bundleContext.getDataFile("lastModel.xmi");
    if (lastModelssaved.length() != 0) {
      /* Load previous state */
      var model = KevoreeXmiHelper.load(lastModelssaved.getAbsolutePath());
      switchToNewModel(model)
    }
    this
  }

  override def stop() : Unit = {
    super[KevoreeActor].forceStop
    //TODO CLEAN AND REACTIVATE

   // KevoreeXmiHelper.save(bundleContext.getDataFile("lastModel.xmi").getAbsolutePath(), models.head);
  }

  def internal_process(msg : Any) = msg match {
    case updateMsg : PlatformModelUpdate => KevoreePlatformHelper.updateNodeLinkProp(model,nodeName, updateMsg.targetNodeName, updateMsg.key, updateMsg.value, updateMsg.networkType, updateMsg.weight)
    case PreviousModel() => reply(models)
    case LastModel() => reply(model) /* TODO DEEP CLONE */
    case UpdateModel(newmodel) => {
        if (newmodel == null) { logger.error("Null model")} else {

          var adaptationModel = kompareService.kompare(model, newmodel, nodeName);
          var deployResult = deployService.deploy(adaptationModel,nodeName);

          if(deployResult){
            //MErge previous model on new model for platform model
            KevoreePlatformMerger.merge(newmodel,model)
            switchToNewModel(newmodel)
            logger.info("Deploy result " + deployResult)
          } else {
            //KEEP FAIL MODEL
          }
          reply(deployResult)

        }
      }
    case _ @ unknow=> logger.warn("unknow message  "+unknow)
  }

  
  override def getLastModel : ContainerRoot = (this !? LastModel()).asInstanceOf[ContainerRoot]
  override def updateModel(model : ContainerRoot) : java.lang.Boolean ={ (this ! UpdateModel(model));true }
  override def getPreviousModel : java.util.List[ContainerRoot] = (this !? PreviousModel).asInstanceOf[java.util.List[ContainerRoot]]

}
