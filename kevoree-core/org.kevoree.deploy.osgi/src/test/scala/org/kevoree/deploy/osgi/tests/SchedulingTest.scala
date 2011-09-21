package org.kevoree.deploy.osgi.tests

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

import org.scalatest.junit.AssertionsForJUnit
import org.junit._
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi
import org.kevoree.adaptation.deploy.osgi.command.LifeCycleCommand
import org.kevoree.adaptation.deploy.osgi.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoree.kompare.KevoreeKompareBean
import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.framework.KevoreeXmiHelper
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.kevoree.{KevoreePackage, ContainerRoot, KevoreeFactory}
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.xmi.{XMLResource, XMIResource}
import java.util.HashMap

//import scala.collection.JavaConversions._

class SchedulingTest extends AssertionsForJUnit with SchedulingSuite {

  var component: KevoreeKompareBean = null
  var adaptationDeploy: BaseDeployOSGi = null

  def emptyModel = "scheduling_test/emptyModel.kev"

  @Before def initialize () {
    component = new KevoreeKompareBean
    adaptationDeploy = new BaseDeployOSGi
  }

  def adaptationModelStart (url1: String, nodeName: String): AdaptationModel = {
    var node = KevoreeFactory.eINSTANCE.createContainerNode()
    component.kompare(model(emptyModel), model(url1), nodeName)
  }

  def adaptationModelStop (url1: String, nodeName: String): AdaptationModel = {
    var node = KevoreeFactory.eINSTANCE.createContainerNode()
    /*node.setName(nodeName);
       emptyModel.getNodes.add(node)*/
    component.kompare(model(url1), model(emptyModel), nodeName)
  }

  /*@Test def noSchedule() {
   val adaptationSchedule = adaptationModelStart("scheduling_test/noscheduling.kev", "node0")
   val scheduler = new SchedulingWithTopologicalOrderAlgo()
   // TODO build list of commands to schedule them
   scheduler.schedule()
   // TODO end the test
   error("NOT IMPLEMENTED YET")
   }

   @Test def schedulingWith2Components() {
   val adaptationSchedule = adaptationModelStart("scheduling_test/simpleschedulingwith2components.kev", "node0")
   val scheduler = new SchedulingWithTopologicalOrderAlgo()
   // TODO build list of commands to schedule them
   scheduler.schedule()
   // TODO end the test
   error("NOT IMPLEMENTED YET")
   }

   @Test def schedulingWith4Components() {
   val adaptationSchedule = adaptationModelStart("scheduling_test/simpleschedulingwith4components.kev", "node0")
   val scheduler = new SchedulingWithTopologicalOrderAlgo()
   // TODO build list of commands to schedule them
   scheduler.schedule()
   // TODO end the test
   error("NOT IMPLEMENTED YET")
   }

   @Test def schedulingWith5Components2DistinctsGroups() {
   val adaptationSchedule = adaptationModelStart("scheduling_test/schedulingwith5component+2distinctsgroups.kev", "node0")
   val scheduler = new SchedulingWithTopologicalOrderAlgo()
   // TODO build list of commands to schedule them
   scheduler.schedule()
   // TODO end the test
   error("NOT IMPLEMENTED YET")
   }

   @Test def schedulingWith6Components2DistinctsGroups() {
   val adaptationSchedule = adaptationModelStart("scheduling_test/schedulingwith6component+2distinctsgroups.kev", "node0")
   val scheduler = new SchedulingWithTopologicalOrderAlgo()
   // TODO build list of commands to schedule them
   scheduler.schedule()
   // TODO end the test
   error("NOT IMPLEMENTED YET")
   }*/

 /* @Test def schedulingComplexModelTest () {
    val scheduler = new SchedulingWithTopologicalOrderAlgo()

    var adaptationSchedule = adaptationModelStart("scheduling_test/complexScheduling.kev", "node0")
    var commands = adaptationDeploy.buildCommandLists(adaptationSchedule, "node0")

    var stopCommands = commands.getOrElse("stop", List())
    assert(stopCommands.isEmpty)

    var startCommands = commands.getOrElse("start", List())
    assert(startCommands.size == 3)
    if (!startCommands.isEmpty) {
      val tmpCommands = scheduler.schedule(startCommands.asInstanceOf[List[LifeCycleCommand]], true)
      assert(tmpCommands.size == startCommands.size)
      assert(tmpCommands.apply(0).getInstance.getName.equals("TOBECHANGED"))
      assert(tmpCommands.apply(1).getInstance.getName.equals("hubuiService1"))
      assert(tmpCommands.apply(2).getInstance.getName.equals("SimpleLight1"))
    }

    adaptationSchedule = adaptationModelStop("scheduling_test/complexScheduling.kev", "node0")
    commands = adaptationDeploy.buildCommandLists(adaptationSchedule, "node0")

    startCommands = commands.getOrElse("start", List())
    assert(startCommands.isEmpty)

    stopCommands = commands.getOrElse("stop", List())
    assert(stopCommands.size == 3)
    if (!stopCommands.isEmpty) {
      val tmpCommands = scheduler.schedule(stopCommands.asInstanceOf[List[LifeCycleCommand]], false)
      assert(tmpCommands.size == stopCommands.size)
      assert(tmpCommands.apply(0).getInstance.getName.equals("SimpleLight1"))
      assert(tmpCommands.apply(1).getInstance.getName.equals("hubuiService1"))
      assert(tmpCommands.apply(2).getInstance.getName.equals("TOBECHANGED"))
    }
  }*/

  /*@Test def localSchedulingWithCycle () {
    val scheduler = new SchedulingWithTopologicalOrderAlgo()

    val adaptationSchedule = adaptationModelStart("scheduling_test/localSchedulingWithCycle.kev", "test")
    val commands = adaptationDeploy.buildCommandLists(adaptationSchedule, "test")

    val stopCommands = commands.getOrElse("stop", List())
    assert(stopCommands.isEmpty)

    val startCommands = commands.getOrElse("start", List())
    assert(scheduler.schedule(startCommands.asInstanceOf[List[LifeCycleCommand]], true).forall(c => startCommands.contains(c)))
    //assert(startCommands.equals(scheduler.schedule(startCommands.asInstanceOf[List[LifeCycleCommand]], true)))
  }*/

  @Test def localScheduling () {
    val scheduler = new SchedulingWithTopologicalOrderAlgo()

    val adaptationSchedule = component.kompare(model("scheduling_test/m1.kev"), model("scheduling_test/m2.kev"), "home")
    //save("adaptationModel", adaptationSchedule)
    
    //val adaptationSchedule = adaptationModelStart("scheduling_test/m1", "home")
    val commands = adaptationDeploy.buildCommandLists(adaptationSchedule, "home")

    var stopCommands = commands.getOrElse("stop", List())
    stopCommands = scheduler.schedule(stopCommands.asInstanceOf[List[LifeCycleCommand]], false)
    stopCommands.foreach(c => println(c))

  }

  /*def save(uri:String,root : AdaptationModel) {
    val rs :ResourceSetImpl = new ResourceSetImpl();

    rs.getResourceFactoryRegistry.getExtensionToFactoryMap.put("*",new XMIResourceFactoryImpl())
    rs.getPackageRegistry.put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE)
    val uri1:URI   = URI.createURI(uri)
    val res : Resource = rs.createResource(uri1)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions.put(XMLResource.OPTION_ENCODING, "UTF-8")
    res.asInstanceOf[XMIResource].getDefaultSaveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8")
    res.getContents.add(root)
    res.save(new HashMap())
  }*/
}