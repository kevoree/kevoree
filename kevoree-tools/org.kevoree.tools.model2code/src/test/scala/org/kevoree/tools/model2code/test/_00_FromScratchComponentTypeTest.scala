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

import org.kevoree.framework.KevoreeXmiHelper
import org.scalatest.junit.JUnitSuite
import java.io.File
import org.junit._
import junit.framework.Assert._
import org.kevoree.tools.model2code.{CompilationUnitHelpers, Model2Code}

class _00_FromScratchComponentTypeTest extends JUnitSuite with CompilationUnitHelpers{

  val baseGenerationLocation : String = "target/test-classes/generated"
  val componentsGenerationLocation : String = baseGenerationLocation + "/ComponentType"
  val artefactGenerationLocation : String = baseGenerationLocation + "/Artefact"

  def recursiveDelete(dirPath : File) {
    dirPath.listFiles.foreach({ file =>
      if (file.isDirectory) {
        recursiveDelete (file)
      }
      file.delete
    })
    dirPath.delete
  }

  @Test
  def BaseComponentTypeTest() {


    System.out.println(" ---- FIRST PASS ----")
    val outputFolder = new File(componentsGenerationLocation)
    if (outputFolder.exists) {
      recursiveDelete(outputFolder)
    }
    assertFalse("Generation folder already exist.", outputFolder.exists)
    outputFolder.mkdirs


    val model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/fakeDomoComponentTypes.kev").getPath)

    val m2c = new Model2Code()
    m2c.modelToCode(model, outputFolder.toURI)

    val devicesPackageFolder = new File(componentsGenerationLocation + "/org/entimid/fakeStuff/devices")
    assertTrue("Package not generated.",devicesPackageFolder.exists)

    val generatedFiles = devicesPackageFolder.listFiles()
    assertTrue("Wrong number of generated files", generatedFiles.length==5)
    

  }

  @Test
  def BaseComponentTypeTest2ndPass() {

    System.out.println(" ---- SECOND PASS ----")

    val model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/fakeDomoComponentTypes.kev").getPath)

    val m2c = new Model2Code()
    val outputFolder = new File(componentsGenerationLocation)

    m2c.modelToCode(model, outputFolder.toURI)

    /*
    val model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/fakeDomoComponentTypes.kev").getPath)

    val m2c = new Model2Code()
    model.getTypeDefinitions.filter(typeDef => typeDef.isInstanceOf[ComponentType]).foreach {
      componentType =>
        System.out.println("Model2Code on " + componentType.getBean)

        val outputFolder = new File("target/test-classes/generated")

        m2c.modelToCode(model, componentType.asInstanceOf[ComponentType], outputFolder.toURI)

        System.out.println("Model2Code done for " + componentType.getBean)
    }
    */
  }

  @Test
  def GenerateDeployUnitTest() {

    System.out.println(" ---- COMPILATION UNIT GENERATION ----")

    val duFolder = new File(artefactGenerationLocation)
      if (duFolder.exists) {
        recursiveDelete(duFolder)
      }
      duFolder.mkdirs()

    val model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/fakeDomoComponentTypes.kev").getPath)

    val m2c = new Model2Code()
    m2c.modelToDeployUnit(model, duFolder.toURI)

  }
}
