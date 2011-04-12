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

package org.kevoree.kompare.tests

import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.NamedElement
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoreeAdaptation.AdaptationModel
import org.kevoreeAdaptation._
import org.scalatest.junit.JUnitSuite
import scala.collection.JavaConversions._

trait KompareSuite extends JUnitSuite {

  /* UTILITY METHOD */
  def model(url: String): ContainerRoot = {
    if (this.getClass.getClassLoader.getResource(url) == null) {
      println("Warning File not found for test !!!")
    }
    KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource(url).getPath)
  }

  implicit def utilityKompareModel(self: AdaptationModel) = RichAdaptationModel(self)

  implicit def richRoot(self: ContainerRoot) = RichContainerRoot(self)

  def emptyModel = KevoreeFactory.eINSTANCE.createContainerRoot

}

case class RichAdaptationModel(self: AdaptationModel) {

  def verifySize(size: Int) = {
    assert(self.getAdaptations.size == size)
  }

  def shouldContain[A](c: Class[A], refName: String) = {
    assert(
      self.getAdaptations.exists(adaptation => {
        adaptation match {
          case e: InstanceAdaptation if (adaptation.getClass.getSimpleName.contains(c.getSimpleName)) => e.getRef.getName == refName
          case e: TypeAdaptation if (adaptation.getClass.getSimpleName.contains(c.getSimpleName)) => e.getRef.getName == refName
          case _ => false
        }
      })
    )
  }

  def shouldContainSize[A](c: Class[A], nb: Int) = {
    assert(
      self.getAdaptations.filter(adaptation => adaptation.getClass.getSimpleName.contains(c.getSimpleName)).size == nb
    )
  }


  def shouldNotContain(c: Class[_]) = {
    assert(
      self.getAdaptations.forall(adaptation => !adaptation.getClass.getSimpleName.contains(c.getSimpleName))
    )
  }

  def print = {

    println("Adaptations")
    self.getAdaptations.toArray.foreach {
      adapt =>
        println(adapt.getClass.getName)
        adapt match {
          case i: UpdateDeployUnit => println("=>" + i.getRef.getUnitName)
          case i: TypeAdaptation => println("=>" + i.getRef.getName)
          case i: InstanceAdaptation => println("=>" + i.getRef.getName)
          case i: BindingAdaptation => {
            println("=>" + i.getRef.getHub.getName + "->" + i.getRef.getPort.getPortTypeRef.getName + "-" + i.getRef.getPort.eContainer.asInstanceOf[NamedElement].getName)
          }
          case _ =>
        }

    }
  }
}


case class RichContainerRoot(self: ContainerRoot) {

  def setLowerHashCode: ContainerRoot = {
    self.getDeployUnits.foreach(du => du.setHashcode(0 + ""))
    self
  }


}