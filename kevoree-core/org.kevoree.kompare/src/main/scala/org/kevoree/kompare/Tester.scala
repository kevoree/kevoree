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
package org.kevoree.kompare

import org.kevoree.framework.KevoreeXmiHelper
import scheduling.SchedulingWithTopologicalOrderAlgo
import org.kevoreeAdaptation.ParallelStep
import org.kevoree.{MBinding, DeployUnit, NamedElement}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 09:18
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  println("Test Kompare")

  val bean = new KevoreeKompareBean

  val model1 = KevoreeXmiHelper.load("/home/edaubert/kevoree/0jenkins_planning.kev")
  val model2 = KevoreeXmiHelper.load("/home/edaubert/kevoree/jenkins_planning.kev")


  val adapModel = bean.kompare(model1, model2, "node0")


  /*adapModel.getAdaptations.foreach {
    adaptation =>
      println(adaptation.getPrimitiveType.getName)
      if (adaptation.getRef.isInstanceOf[NamedElement]) {
        if (adaptation.getRef.isInstanceOf[DeployUnit]) {
          println("ref=" + adaptation.getRef.asInstanceOf[DeployUnit].getUrl+ " -> " + adaptation.getRef.asInstanceOf[DeployUnit].getUnitName + "->" + adaptation.getTargetNodeName)
        } else {
          println("ref=" + adaptation.getRef.asInstanceOf[NamedElement].getName + "->" + adaptation.getTargetNodeName)

        }

      } else {
        println("ref=" + adaptation.getRef + "->" + adaptation.getTargetNodeName)

      }
  }*/

  if (adapModel.getOrderedPrimitiveSet.isDefined) {
    printStep(adapModel.getOrderedPrimitiveSet.get)
  }

  private def printStep (step: ParallelStep) {
    step.getAdaptations.foreach {
      action =>

        if (action.getRef.isInstanceOf[DeployUnit]) {
          println(action.getPrimitiveType.getName + " : " + action.getRef.asInstanceOf[DeployUnit].getUrl + " -> " +
            action.getRef.asInstanceOf[DeployUnit].getUnitName + "->" + action.getTargetNodeName)
        } else if (action.getRef.isInstanceOf[MBinding]) {
          println(action.getPrimitiveType.getName + " : " + action.getRef.asInstanceOf[MBinding].getHub + "<->" + action.getRef.asInstanceOf[MBinding].getPort.eContainer.asInstanceOf[NamedElement].getName + "." + action.getRef.asInstanceOf[MBinding].getPort.getPortTypeRef.getName)
        } else {
          println(action.getPrimitiveType.getName + " : " + action.getRef.asInstanceOf[NamedElement].getName)
        }
    }
    if (step.getNextStep.isDefined) {
      printStep(step.getNextStep.get)
    }
  }

}