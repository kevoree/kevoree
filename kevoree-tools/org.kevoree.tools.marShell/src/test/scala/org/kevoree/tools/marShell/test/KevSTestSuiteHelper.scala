package org.kevoree.tools.marShell.test

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

import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import org.scalatest.junit.JUnitSuite

import org.junit.Assert._
import org.kevoree.{KevoreeFactory, ContainerRoot}
import java.util.HashMap
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.tools.marShell.ast.Script
import org.kevoree.framework.KevoreeXmiHelper

trait KevSTestSuiteHelper extends JUnitSuite {

  /* UTILITY METHOD */
  def model(url: String): ContainerRoot = {
    val modelPath = this.getClass.getClassLoader.getResource(url).getPath
    KevoreeXmiHelper.load(modelPath)
  }

  def emptyModel = KevoreeFactory.eINSTANCE.createContainerRoot


  def getScript(url:String) : Script = {
    val parser =new KevsParser();
    val script = parser.parseScript(ParserUtil.loadFile(this.getClass.getClassLoader.getResource(url).getPath))

    if(!script.isDefined) {
      println(parser.lastNoSuccess)
    }

    assertTrue(script.isDefined)
    script.get
  }



  def hasNoRelativeReference(path: String, file: String) = {
    val modelPath = this.getClass.getClassLoader.getResource(path).getPath + "/" + file
    val bufferedReader = new BufferedReader(new FileReader(new File(modelPath)))
    val stringBuffer = new StringBuffer
    var line: String = bufferedReader.readLine
    while (line != null) {
      stringBuffer.append(line)
      line = bufferedReader.readLine
    }

    !stringBuffer.toString.contains("#")
  }

  implicit def utilityMerger(self: ContainerRoot) = RichContainerRoot(self)

}


case class RichContainerRoot(self: ContainerRoot) extends KevSTestSuiteHelper {

  def testSave(path: String, file: String) {
    try {
      KevoreeXmiHelper.save(this.getClass.getClassLoader.getResource(path).getPath + "/" + file, self)
      assertTrue("At least one relative reference have been detected in model " + path + "/" + file, hasNoRelativeReference(path, file))
    } catch {
      case _@e => e.printStackTrace(); fail()
    }
  }

  def setLowerHashCode(): ContainerRoot = {
    self.getDeployUnits.foreach(du => du.setHashcode(0 + ""))
    self
  }


}
