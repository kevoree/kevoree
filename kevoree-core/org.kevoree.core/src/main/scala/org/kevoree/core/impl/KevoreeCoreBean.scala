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
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import scala.reflect.BeanProperty
import org.kevoree.framework.message._
import scala.actors.Actor
import org.kevoree.api.configuration.ConfigConstants
import java.util.Date
import org.kevoree.api.service.core.handler.{ModelListener, KevoreeModelHandlerService}
import org.kevoree.framework._
import org.kevoree.framework.context.KevoreeDeployManager
import deploy.PrimitiveCommandExecutionHelper
import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper
import java.io.{BufferedOutputStream, BufferedInputStream, InputStream, OutputStream}
import org.kevoree.cloner.ModelCloner
import org.kevoree.core.basechecker.RootChecker

class KevoreeCoreBean extends KevoreeModelHandlerService with KevoreeActor {

  @BeanProperty var configService: ConfigurationService = null
  var bundleContext: BundleContext = null;

  def getBundleContext = bundleContext

  def setBundleContext(bc: BundleContext) {
    bundleContext = bc
    KevoreeDeployManager.setBundle(bc.getBundle)
  }


  @BeanProperty var nodeName: String = ""
  @BeanProperty var nodeInstance: AbstractNodeType = null

  var models: java.util.ArrayList[ContainerRoot] = new java.util.ArrayList()
  var model: ContainerRoot = KevoreeFactory.eINSTANCE.createContainerRoot

  var lastDate: Date = new Date(System.currentTimeMillis)

  def getLastModification = lastDate

  var logger = LoggerFactory.getLogger(this.getClass);
  val modelClone = new ModelCloner

  private def checkBootstrapNode(currentModel: ContainerRoot): Unit = {
    try {
      if (nodeInstance == null) {
        currentModel.getNodes.find(n => n.getName == nodeName) match {
          case Some(foundNode) => {
            val bt = new NodeTypeBootstrapHelper
            bt.bootstrapNodeType(currentModel, nodeName, bundleContext) match {
              case Some(ist: AbstractNodeType) => {
                nodeInstance = ist;
                nodeInstance.startNode()

                //SET CURRENT MODEL 

                model = modelClone.clone(currentModel)
                model.removeAllGroups()
                model.removeAllHubs()
                model.removeAllMBindings()
                model.getNodes.filter(n => n.getName != nodeName).foreach {
                  node =>
                    model.removeNodes(node)
                }
                model.getNodes(0).removeAllComponents()
                model.getNodes(0).removeAllHosts()

              }
              case None => logger.error("TypeDef installation fail !")
            }
          }
          case None => logger.error("Node instance name " + nodeName + " not found in bootstrap model !")
        }
      }
    } catch {
      case _@e => logger.error("Error while bootstraping node instance ", e)
    }


  }

  private def switchToNewModel(c: ContainerRoot) = {
    models.add(model)
    model = c
    lastDate = new Date(System.currentTimeMillis)
    //TODO ADD LISTENER

    new Actor {
      def act() {
        //NOTIFY LOCAL REASONER
        listenerActor.notifyAllListener()
        //NOTIFY GROUP
        val srs = bundleContext.getServiceReferences(classOf[KevoreeGroup].getName, null)
        if (srs != null) {
          srs.foreach {
            sr =>
              bundleContext.getService(sr).asInstanceOf[KevoreeGroup].triggerModelUpdate()
          }
        }
      }
    }.start()
  }

  override def start: Actor = {
    logger.info("Start event : node name = " + configService.getProperty(ConfigConstants.KEVOREE_NODE_NAME))
    setNodeName(configService.getProperty(ConfigConstants.KEVOREE_NODE_NAME));
    super.start()

    //State recovery phase
    /*
    val lastModelssaved = bundleContext.getDataFile("lastModel.xmi");
    if (lastModelssaved.length() != 0) {
      /* Load previous state */
      val model = KevoreeXmiHelper.load(lastModelssaved.getAbsolutePath());
      switchToNewModel(model)
    }  */

    this
  }

  override def stop() {

    logger.warn("Kevoree Core will be stopped !")

    listenerActor.stop()

    super[KevoreeActor].forceStop
    //TODO CLEAN AND REACTIVATE

    if (nodeInstance != null) {

      try {
        val stopModel = KevoreeFactory.eINSTANCE.createContainerRoot
        val adaptationModel = nodeInstance.kompare(model, stopModel);
        val deployResult = PrimitiveCommandExecutionHelper.execute(adaptationModel, nodeInstance)
      } catch {
        case _@e => {
          logger.error("Error while unbootstrap ", e)
        }
      }
      try {
        nodeInstance.stopNode()
        nodeInstance == null
      } catch {
        case _@e => {
          logger.error("Error while stopping node instance ", e)
        }
      }
    }

    logger.debug("Kevoree core stopped ")
    // KevoreeXmiHelper.save(bundleContext.getDataFile("lastModel.xmi").getAbsolutePath(), models.head);
  }

  val cloner = new ModelCloner
  val modelChecker = new RootChecker

  def internal_process(msg: Any) = msg match {
    case updateMsg: PlatformModelUpdate => KevoreePlatformHelper.updateNodeLinkProp(model, nodeName, updateMsg.targetNodeName, updateMsg.key, updateMsg.value, updateMsg.networkType, updateMsg.weight)
    case PreviousModel() => reply(models)
    case LastModel() => {
      reply(cloner.clone(model))
    }

    case UpdateModel(pnewmodel) => {
      if (pnewmodel == null) {
        logger.error("Null model")
        reply(false)
      } else {

        try {
          val checkResult = modelChecker.check(pnewmodel)
          if (!checkResult.isEmpty) {
            logger.error("There is check failure on update model, update refused !")
            import scala.collection.JavaConversions._
            checkResult.foreach {
              cr =>
                logger.error("error=>" + cr.getMessage + ",objects" + cr.getTargetObjects.mkString(","))
            }
            reply(false)
          } else {

            var newmodel = cloner.clone(pnewmodel)
            //CHECK FOR HARA KIRI
            if (HaraKiriHelper.detectNodeHaraKiri(model, newmodel, getNodeName())) {
              logger.warn("HaraKiri detected , flush platform")
              newmodel = KevoreeFactory.createContainerRoot
              try {
                val adaptationModel = nodeInstance.kompare(model, newmodel);

                logger.debug("Adaptation model size "+adaptationModel.getAdaptations.size)
                adaptationModel.getAdaptations.foreach{ adaptation =>
                    logger.debug("primitive "+adaptation.getPrimitiveType.getName)
                }


                PrimitiveCommandExecutionHelper.execute(adaptationModel, nodeInstance)
                KevoreeDeployManager.bundleMapping.foreach{ bm =>
                  try {
                    logger.debug("Try to cleanup "+bm.bundleId+","+bm.objClassName+","+bm.name)
                    KevoreeDeployManager.removeMapping(bm)
                    getBundleContext.getBundle(bm.bundleId).uninstall()
                  } catch {
                    case _ @ e => logger.debug("Error while cleanup platform ",e)
                  }
                }
                logger.debug("Deploy manager cache size after HaraKiri"+KevoreeDeployManager.bundleMapping.size)

                nodeInstance = null
                switchToNewModel(newmodel)
                newmodel = cloner.clone(pnewmodel)
              } catch {
                case _@e => {
                  logger.error("Error while update ", e)
                }
              }
              logger.debug("End HaraKiri")
            }

            checkBootstrapNode(newmodel)
            val milli = System.currentTimeMillis
            logger.debug("Begin update model " + milli)
            var deployResult = true
            try {
              val adaptationModel = nodeInstance.kompare(model, newmodel);
              deployResult = PrimitiveCommandExecutionHelper.execute(adaptationModel, nodeInstance)
            } catch {
              case _@e => {
                logger.error("Error while update ", e)
                deployResult = false
              }
            }


            if (deployResult) {
              //Merge previous model on new model for platform model
              //KevoreePlatformMerger.merge(newmodel, model)
              switchToNewModel(newmodel)
              logger.info("Deploy result " + deployResult)
            } else {
              //KEEP FAIL MODEL

              logger.warn("Failed model")

            }

            val milliEnd = System.currentTimeMillis - milli
            logger.debug("End deploy result=" + deployResult + "-" + milliEnd)

            reply(deployResult)
          }
        } catch {
          case _@e =>
            logger.error("Error while update", e)
            reply(false)
        }


      }
    }
    case _@unknow => logger.warn("unknow message  " + unknow.toString + " - sender" + sender.toString + "-" + this.getClass.getName)
  }


  override def getLastModel: ContainerRoot = {
    (this !? LastModel()).asInstanceOf[ContainerRoot]
  }

  override def updateModel(model: ContainerRoot) {
    logger.debug("update asked")
    this ! UpdateModel(model)
    logger.debug("update end")
  }

  override def atomicUpdateModel(model: ContainerRoot) = {
    logger.debug("Atomic update asked")
    (this !? UpdateModel(model))
    logger.debug("Atomic update end")
    lastDate
  }

  override def getPreviousModel: java.util.List[ContainerRoot] = (this !? PreviousModel()).asInstanceOf[java.util.List[ContainerRoot]]


  val listenerActor = new KevoreeListeners
  listenerActor.start()

  override def registerModelListener(listener: ModelListener) {
    listenerActor.addListener(listener)
  }

  override def unregisterModelListener(listener: ModelListener) {
    listenerActor.removeListener(listener)
  }


}
