package org.kevoree.tools.model2code.test

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

import org.kevoree.framework.KevoreeXmiHelper
import org.scalatest.junit.JUnitSuite
import java.io.File
import org.junit._
import junit.framework.Assert._
import org.kevoree.tools.model2code.{CompilationUnitHelpers, Model2Code}

class _02_FromScratchNodeTypeTest extends JUnitSuite with CompilationUnitHelpers{

  val generationLocation : String = "target/test-classes/generated/NodeType"

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


    System.out.println(" ---- FIRTS PASS ----")
    val outputFolder = new File(generationLocation)
    if (outputFolder.exists) {
      recursiveDelete(outputFolder)
    }
    assertTrue("Generation folder already exist.", !outputFolder.exists())
    outputFolder.mkdirs


    val model = KevoreeXmiHelper.load(this.getClass.getClassLoader.getResource("models/javaSeNode.kev").getPath)

    val m2c = new Model2Code()
    m2c.modelToCode(model, outputFolder.toURI)

    /*
    val devicesPackageFolder = new File(generationLocation)
    assertTrue("Package not generated.",devicesPackageFolder.exists)

    val generatedFiles = devicesPackageFolder.listFiles()
    assertTrue("Wrong number of generated files", generatedFiles.length==5)
    */

  }


}
