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
	def emptyModel = KevoreeFactory.eINSTANCE.createContainerRoot

	@Before def initialize() {
		component = new KevoreeKompareBean
		adaptationDeploy = new KevoreeAdaptationDeployServiceOSGi
	}

	def adaptationModel(url1 : String, nodeName : String):AdaptationModel={
		var node = KevoreeFactory.eINSTANCE.createContainerNode()
		node.setName(nodeName);
		emptyModel.getNodes.add(node)
		component.kompare(emptyModel, model(url1), nodeName)
	}

	/*@Test def noSchedule() {
	 val adaptationSchedule = adaptationModel("test_scheduling/noscheduling.kev")
	 val scheduler = new SchedulingWithTopologicalOrderAlgo()
	 // TODO build list of commands to schedule them
	 scheduler.schedule()
	 // TODO end the test
	 error("NOT IMPLEMENTED YET")
	 }

	 @Test def schedulingWith2Components() {
	 val adaptationSchedule = adaptationModel("test_scheduling/simpleschedulingwith2components.kev")
	 val scheduler = new SchedulingWithTopologicalOrderAlgo()
	 // TODO build list of commands to schedule them
	 scheduler.schedule()
	 // TODO end the test
	 error("NOT IMPLEMENTED YET")
	 }

	 @Test def schedulingWith4Components() {
	 val adaptationSchedule = adaptationModel("test_scheduling/simpleschedulingwith4components.kev")
	 val scheduler = new SchedulingWithTopologicalOrderAlgo()
	 // TODO build list of commands to schedule them
	 scheduler.schedule()
	 // TODO end the test
	 error("NOT IMPLEMENTED YET")
	 }

	 @Test def schedulingWith5Components2DistinctsGroups() {
	 val adaptationSchedule = adaptationModel("test_scheduling/schedulingwith5component+2distinctsgroups.kev")
	 val scheduler = new SchedulingWithTopologicalOrderAlgo()
	 // TODO build list of commands to schedule them
	 scheduler.schedule()
	 // TODO end the test
	 error("NOT IMPLEMENTED YET")
	 }

	 @Test def schedulingWith6Components2DistinctsGroups() {
	 val adaptationSchedule = adaptationModel("test_scheduling/schedulingwith6component+2distinctsgroups.kev")
	 val scheduler = new SchedulingWithTopologicalOrderAlgo()
	 // TODO build list of commands to schedule them
	 scheduler.schedule()
	 // TODO end the test
	 error("NOT IMPLEMENTED YET")
	 }

	 @Test def schedulingComplexModel() {
	 val adaptationSchedule = adaptationModel("scheduling_test/complexScheduling.kev", "home")
	 val scheduler = new SchedulingWithTopologicalOrderAlgo()
	 // TODO build list of commands to schedule them
	 var commands = adaptationDeploy.buildCommandLists(adaptationSchedule, "home")
	 var stopCommands = commands.getOrElse("stop", List())
	 var startCommands = commands.getOrElse("stop", List())
	 if (!stopCommands.isEmpty) {
	 var tmpCommands = scheduler.schedule(stopCommands.asInstanceOf[List[LifeCycleCommand]], false)
	 for (command <- tmpCommands) {
	 println(command)
	 }
	 }
	 if (!startCommands.isEmpty) {
	 var tmpCommands = scheduler.schedule(startCommands.asInstanceOf[List[LifeCycleCommand]], true)
	 for (command <- tmpCommands) {
	 println(command)
	 }
	 }
	 // TODO end the test
	 //error("NOT IMPLEMENTED YET")
	 }*/
}