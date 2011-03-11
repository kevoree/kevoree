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
import scala.collection.JavaConversions._
import org.junit._
import org.kevoree.adaptation.deploy.osgi.KevoreeAdaptationDeployServiceOSGi
import org.kevoree.adaptation.deploy.osgi.command.LifeCycleCommand
import org.kevoree.adaptation.deploy.osgi.scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoree.KevoreeFactory
import org.kevoree.kompare.KevoreeKompareBean
import org.kevoreeAdaptation.AdaptationModel

//import scala.collection.JavaConversions._

class SchedulingTest extends AssertionsForJUnit with SchedulingSuite {

  var component : KevoreeKompareBean = null
  var adaptationDeploy : KevoreeAdaptationDeployServiceOSGi = null
  def emptyModel = "scheduling_test/emptyModel.kev"

  @Before def initialize() {
	component = new KevoreeKompareBean
	adaptationDeploy = new KevoreeAdaptationDeployServiceOSGi
  }

  def adaptationModelStart(url1 : String, nodeName : String):AdaptationModel={
	var node = KevoreeFactory.eINSTANCE.createContainerNode()
	component.kompare(model(emptyModel), model(url1), nodeName)
  }
  
  def adaptationModelStop(url1 : String, nodeName : String):AdaptationModel={
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

  @Test def schedulingComplexModelTest() {
	val scheduler = new SchedulingWithTopologicalOrderAlgo()
	
	var adaptationSchedule = adaptationModelStart("scheduling_test/complexScheduling.kev", "node0")
	var commands = adaptationDeploy.buildCommandLists(adaptationSchedule, "node0")
	
	var stopCommands = commands.getOrElse("stop", List())
	assert(stopCommands.isEmpty)
	
	var startCommands = commands.getOrElse("start", List())
	assert(startCommands.size == 3)
	if (!startCommands.isEmpty) {
	  var tmpCommands = scheduler.schedule(startCommands.asInstanceOf[List[LifeCycleCommand]], true)
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
	  var tmpCommands = scheduler.schedule(stopCommands.asInstanceOf[List[LifeCycleCommand]], false)
	  assert(tmpCommands.size == stopCommands.size)
	  assert(tmpCommands.apply(0).getInstance.getName.equals("SimpleLight1"))
	  assert(tmpCommands.apply(1).getInstance.getName.equals("hubuiService1"))
	  assert(tmpCommands.apply(2).getInstance.getName.equals("TOBECHANGED"))
	}
  }
}