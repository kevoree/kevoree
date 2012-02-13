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

import _root_.org.kevoree.KevoreeFactory
import _root_.org.kevoree.ContainerRoot
import _root_.org.kevoree.api.configuration.ConfigurationService
import _root_.org.slf4j.LoggerFactory
import _root_.scala.reflect.BeanProperty
import _root_.org.kevoree.api.configuration.ConfigConstants
import _root_.org.kevoree.framework._
import _root_.org.kevoree.core.impl.message._
import deploy.PrimitiveCommandExecutionHelper
import _root_.org.kevoree.cloner.ModelCloner
import _root_.org.kevoree.core.basechecker.RootChecker
import _root_.java.util.{UUID, Date}
import org.kevoree.api.service.core.handler._
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.api.Bootstraper
import java.lang.Long
import actors.{DaemonActor, TIMEOUT, Actor}

class KevoreeCoreBean extends KevoreeModelHandlerService with KevoreeThreadActor {

  val listenerActor = new KevoreeListeners
  listenerActor.start()

  @BeanProperty var configService: ConfigurationService = null
  var kevsEngineFactory: KevScriptEngineFactory = null

  def setKevsEngineFactory(k: KevScriptEngineFactory) {
    kevsEngineFactory = k
  }

  @BeanProperty var bootstraper: Bootstraper = null
  @BeanProperty var nodeName: String = ""
  @BeanProperty var nodeInstance: org.kevoree.api.NodeType = null

  var models: scala.collection.mutable.ArrayBuffer[ContainerRoot] = new scala.collection.mutable.ArrayBuffer[ContainerRoot]()
  var model: ContainerRoot = KevoreeFactory.eINSTANCE.createContainerRoot
  var lastDate: Date = new Date(System.currentTimeMillis)
  var currentModelUUID: UUID = UUID.randomUUID();

  def getLastModification = lastDate

  var logger = LoggerFactory.getLogger(this.getClass);
  val modelClone = new ModelCloner

  var selfActorPointer = this

  private def checkBootstrapNode(currentModel: ContainerRoot): Unit = {
    try {
      if (nodeInstance == null) {
        currentModel.getNodes.find(n => n.getName == nodeName) match {
          case Some(foundNode) => {
            //  val bt = new NodeTypeBootstrapHelper
            bootstraper.bootstrapNodeType(currentModel, nodeName, this, kevsEngineFactory) match {
              case Some(ist: org.kevoree.api.NodeType) => {
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
      case _@e => {
        logger.error("Error while bootstraping node instance ", e)
        try {
          nodeInstance.stopNode()
        } catch {
          case _ =>
        }
        nodeInstance = null
      }
    }
  }

  private def checkUnbootstrapNode(currentModel: ContainerRoot): Option[ContainerRoot] = {
    try {
      if (nodeInstance != null) {
        currentModel.getNodes.find(n => n.getName == nodeName) match {
          case Some(foundNode) => {
            val modelTmp = modelClone.clone(currentModel)
            modelTmp.removeAllGroups()
            modelTmp.removeAllHubs()
            modelTmp.removeAllMBindings()
            modelTmp.getNodes.filter(n => n.getName != nodeName).foreach {
              node =>
                modelTmp.removeNodes(node)
            }
            modelTmp.getNodes(0).removeAllComponents()
            modelTmp.getNodes(0).removeAllHosts()
            Some(modelTmp)
          }
          case None => logger.error("TypeDef installation fail !"); None
        }
      } else {
        logger.error("node instance is not available on current model !")
        None
      }
    } catch {
      case _@e => logger.error("Error while unbootstraping node instance ", e); None
    }
  }

  private def switchToNewModel(c: ContainerRoot) = {
    //current model is backed-up
    models.append(model)

    // MAGIC NUMBER ;-) , ONLY KEEP 10 PREVIOUS MODEL
    if (models.size > 15) {
      models = models.drop(5)
      logger.debug("Garbage old previous model")
    }

    //Changes the current model by the new model
    model = c
    currentModelUUID = UUID.randomUUID()
    lastDate = new Date(System.currentTimeMillis)
    //Fires the update to listeners
    new Actor {
      def act() {
        listenerActor.notifyAllListener()
      }
    }.start()
  }

  override def start: Actor = {
    logger.info("Kevoree Start event : node name = " + configService.getProperty(ConfigConstants.KEVOREE_NODE_NAME))
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
    super[KevoreeThreadActor].forceStop
    if (nodeInstance != null) {
      try {
        val stopModel = checkUnbootstrapNode(model)
        if (stopModel.isDefined) {
          val adaptationModel = nodeInstance.kompare(model, stopModel.get);
          val deployResult = PrimitiveCommandExecutionHelper.execute(adaptationModel, nodeInstance)
        } else {
          logger.error("Unable to use the stopModel !")
        }

      } catch {
        case _@e => {
          logger.error("Error while unbootstrap ", e)
        }
      }
      try {
        logger.debug("Call instance stop")
        nodeInstance.stopNode()
        nodeInstance == null
        bootstraper.clear
      } catch {
        case _@e => {
          logger.error("Error while stopping node instance ", e)
        }
      }
    }
    logger.debug("Kevoree core stopped ")
  }

  val cloner = new ModelCloner
  val modelChecker = new RootChecker

  // treatment of incoming messages
  def internal_process(msg: Any) = msg match {
    //Returns the collection of previous models
    case PreviousModel() => reply(models)
    //Returns a clone of the current system model
    case LastModel() => {
      reply(cloner.clone(model))
    }
    // returns a clone of the current system model with a companion UUID for concurrency management
    case LastUUIDModel() => {
      reply(UUIDModelImpl(currentModelUUID, cloner.clone(model)))
    }
    //Update the system with the model given in parameter, with the UUID for checking
    case UpdateUUIDModel(prevUUIDModel, targetModel) => {
      if (currentLock != null) {
        if (prevUUIDModel.getUUID.compareTo(currentLock._1) == 0) {
          reply(internal_update_model(targetModel))
        } else {
          logger.debug("Core Locked , bad UUID "+prevUUIDModel.getUUID)
          reply(false) //LOCK REFUSED !
        }
      } else {
        //COMMON CHECK
        if (prevUUIDModel.getUUID.compareTo(currentModelUUID) == 0) {
          //TODO CHECK WITH MODEL SHA-1 HASHCODE
          reply(internal_update_model(targetModel))
        } else {
          reply(false)
        }
      }
    }
    //Updates the system to fit to the new model
    case UpdateModel(pnewmodel) => {
      if (currentLock == null) {
        reply(internal_update_model(pnewmodel))
      } else {
        logger.debug("Core Locked , UUID mandatory")
        reply(false)
      }
    }

    case ACQUIRE_LOCK(handler, timeout) => {
      if (currentLock != null) {
        handler.lockRejected()
      } else {
        val lockUUID = UUID.randomUUID()
        currentLock = (lockUUID, handler)
        //RUN ACTOR
        currentLockTimer = new DaemonActor {
          def act() {
            reactWithin(timeout) {
              case TIMEOUT => {
                selfActorPointer ! LOCK_TIMEOUT();
                exit()
              }
              case RELEASE_LOCK(uuid) => exit()
            }
          }
        }
        currentLockTimer.start()
        handler.lockAcquired(lockUUID)
      }
    }
    case LOCK_TIMEOUT() => {
      if (currentLock != null) {
        currentLock._2.lockTimeout()
        currentLock = null
        currentLockTimer = null
      }
    }
    case RELEASE_LOCK(uuid: UUID) => {
      if (currentLock != null) {
        if (currentLock._1.compareTo(uuid) == 0) {
          currentLock = null
          currentLockTimer ! RELEASE_LOCK(uuid) // KILL ACTOR
          currentLockTimer = null
        }
      }
    }


    case _@unknow => logger
      .warn("unknow message  " + unknow.toString + " - sender" + sender.toString + "-" + this.getClass.getName)
  }

  private def internal_update_model(pnewmodel: ContainerRoot): Boolean = {
    if (pnewmodel == null || !pnewmodel.getNodes.exists(p => p.getName == getNodeName())) {
      logger.error("Asking for update with a NULL model or node name was not found in target model !")
      false
    }  else {
      try {

        //Model checking
        val checkResult = modelChecker.check(pnewmodel)
        if (!checkResult.isEmpty) {
          logger.error("There is check failure on update model, update refused !")
          import _root_.scala.collection.JavaConversions._
          checkResult.foreach {
            cr =>
              logger.error("error=>" + cr.getMessage + ",objects" + cr.getTargetObjects.mkString(","))
          }
          false
        } else {
          //Model check is OK.
          val precheckModel = cloner.clone(pnewmodel)
          logger.debug("Before listeners PreCheck !")
          val preCheckResult = listenerActor.preUpdate(precheckModel)
          logger.debug("PreCheck result = " + preCheckResult)
          if (preCheckResult) {
            var newmodel = cloner.clone(pnewmodel)
            //CHECK FOR HARA KIRI
            if (HaraKiriHelper.detectNodeHaraKiri(model, newmodel, getNodeName())) {
              logger.warn("HaraKiri detected , flush platform")
              // Creates an empty model, removes the current node (harakiri)
              newmodel = checkUnbootstrapNode(model).get
              try {
                // Compare the two models and plan the adaptation
                val adaptationModel = nodeInstance.kompare(model, newmodel)
                if (logger.isDebugEnabled) {
                  //Avoid the loop if the debug is not activated
                  logger.debug("Adaptation model size " + adaptationModel.getAdaptations.size)
                  adaptationModel.getAdaptations.foreach {
                    adaptation =>
                      logger.debug("primitive " + adaptation.getPrimitiveType.getName)
                  }
                }
                //Executes the adaptation
                PrimitiveCommandExecutionHelper.execute(adaptationModel, nodeInstance)
                nodeInstance.stopNode()
                //end of harakiri
                nodeInstance = null
                bootstraper.clear //CLEAR
                //place the current model as an empty model (for backup)
                switchToNewModel(KevoreeFactory.createContainerRoot)

                //prepares for deployment of the new system
                newmodel = cloner.clone(pnewmodel) //OVERRIDE NEW MODEL WITH NEW VALUE
              } catch {
                case _@e => {
                  logger.error("Error while update ", e)
                }
              }
              logger.debug("End HaraKiri")
            }

            //Checks and bootstrap the node
            checkBootstrapNode(newmodel)
            val milli = System.currentTimeMillis
            logger.debug("Begin update model " + milli)
            var deployResult = true
            try {
              // Compare the two models and plan the adaptation
              logger.info("Comparing models and planning adaptation.")
              val adaptationModel = nodeInstance.kompare(model, newmodel);

              //Execution of the adaptation
              logger.info("Launching adaptation of the system.")
              deployResult = PrimitiveCommandExecutionHelper.execute(adaptationModel, nodeInstance)
            } catch {
              case _@e => {
                logger.error("Error while update ", e)
                deployResult = false
              }
            }
            if (deployResult) {
              switchToNewModel(newmodel)
              logger.info("Update sucessfully completed.")
            } else {
              //KEEP FAIL MODEL, TODO
              logger.warn("Update failed")
            }
            val milliEnd = System.currentTimeMillis - milli
            logger.debug("End deploy result=" + deployResult + "-" + milliEnd)
            deployResult
          } else {
            logger.debug("PreCheck Step was refused, update aborded !")
            false
          }
        }
      } catch {
        case _@e =>
          logger.error("Error while update", e)
          false
      }
    }
  }

  /**
   * Returns the current model.
   * @Deprecated : Consider using #getLastUUIDModel for concurrency reasons
   */
  @Deprecated
  override def getLastModel: ContainerRoot = {
    (this !? LastModel()).asInstanceOf[ContainerRoot]
  }

  /**
   * Returns the current model with a unique token
   */
  override def getLastUUIDModel: UUIDModel = {
    (this !? LastUUIDModel()).asInstanceOf[UUIDModel]
  }

  /**
   * Asks for an update of the system to the new system described by the model.
   * @Deprecated Consider using #compareAndSwapModel for concurrency reasons
   */
  @Deprecated
  override def updateModel(model: ContainerRoot) {
    this ! UpdateModel(model)
  }

  /**
   * Asks for an update of the system to fit the model given in parameter.<br/>
   * This method is blocking until the update is done.
   * @Deprecated Consider using #atomicCompareAndSwapModel for concurrency reasons.
   */
  override def atomicUpdateModel(model: ContainerRoot) = {
    (this !? UpdateModel(model))
    lastDate
  }

  /**
   * Compares the UUID of the model and the current UUID to verify that no update occurred in between the moment the model had been delivered and the moment the update is asked.<br/>
   * If OK, updates the system and switches to the new model, asynchronously
   */
  override def compareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot) {
    this ! UpdateUUIDModel(previousModel, targetModel)
  }

  /**
   * Compares the UUID of the model and the current UUID to verify that no update occurred in between the moment the model had been delivered and the moment the update is asked.<br/>
   * If OK, updates the system and switches to the new model, synchronously (blocking)
   */
  override def atomicCompareAndSwapModel(previousModel: UUIDModel, targetModel: ContainerRoot): Date = {
    val result = (this !? UpdateUUIDModel(previousModel, targetModel)).asInstanceOf[Boolean]
    if (!result) {
      throw new KevoreeModelUpdateException //SEND AND EXCEPTION - Compare&Swap fail !
    }
    lastDate
  }

  /**
   * Provides the collection of last models (short system history)
   */
  override def getPreviousModel: java.util.List[ContainerRoot] = {
    import scala.collection.JavaConversions._
    (this !? PreviousModel()).asInstanceOf[scala.collection.mutable.ArrayBuffer[ContainerRoot]].toList
  }

  override def registerModelListener(listener: ModelListener) {
    listenerActor.addListener(listener)
  }

  override def unregisterModelListener(listener: ModelListener) {
    listenerActor.removeListener(listener)
  }

  def getContextModel: ContextModel = {
    nodeInstance.getContextModel
  }

  /* Lock Management */

  private var currentLock: Tuple2[UUID, ModelHandlerLockCallBack] = null
  private var currentLockTimer: Actor = null

  case class RELEASE_LOCK(uuid: UUID)

  case class ACQUIRE_LOCK(callBack: ModelHandlerLockCallBack, timeout: Long)

  case class LOCK_TIMEOUT()

  def acquireLock(callBack: ModelHandlerLockCallBack, timeout: Long) {
    this ! ACQUIRE_LOCK(callBack, timeout)
  }

  def releaseLock(uuid: UUID) {
    this ! RELEASE_LOCK(uuid)
  }
}
