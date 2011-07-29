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

package org.kevoree.tools.model2code.test

import org.junit._
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.model2code.Model2Code
import org.scalatest.junit.JUnitSuite
import java.io.File


class _10_FromExistingSourceTest extends JUnitSuite {

 val baseGenerationLocation : String = "target/test-classes/generated"
 val componentsGenerationLocation : String = baseGenerationLocation + "/ComponentTypeFromExisting"

  @Test
  def BaseComponentTypeTest() {

    val model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/fakeDomoComponentTypes.kev").getPath)

    val m2c = new Model2Code()

    val outputFolder = new File(componentsGenerationLocation)
    val srcRoot = new File(this.getClass.getClassLoader.getResource("targetCode").getPath)

    m2c.modelToCode(model, srcRoot.toURI, outputFolder.toURI)

  }

  /*
  @Test
  def BaseComponentTypeTest2ndPass = {

    System.out.println("Loading Model FakeSimpleLight.kev")

    var model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/fakeDomoComponentTypes.kev").getPath)

    System.out.println("Model loaded.")

    var m2c = new Model2Code()
    model.getTypeDefinitions.filter(typeDef => typeDef.isInstanceOf[ComponentType]).foreach { componentType =>
      System.out.println("Model2Code on " + componentType.getBean)

      var outputFolder = new File("target/test-classes/generated")

      m2c.modelToCode(componentType.asInstanceOf[ComponentType], outputFolder.toURI)

      System.out.println("Model2Code done for " + componentType.getBean)
    }
  }
  */

}
