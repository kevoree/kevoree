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

package org.kevoree.merger.tests

import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import org.kevoree.KevoreeFactory
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoreeAdaptation._
import org.scalatest.junit.JUnitSuite
import scala.collection.JavaConversions._
import org.junit.Assert._

trait MergerTestSuiteHelper extends JUnitSuite {

  /* UTILITY METHOD */
  def model(url:String):ContainerRoot={
    var modelPath = this.getClass.getClassLoader.getResource(url).getPath
    KevoreeXmiHelper.load(modelPath)
  }

  def emptyModel = KevoreeFactory.eINSTANCE.createContainerRoot

  def hasNoRelativeReference(path:String, file:String) = {
    var modelPath = this.getClass.getClassLoader.getResource(path).getPath+"/"+file
    var bufferedReader = new BufferedReader(new FileReader(new File(modelPath)))

    var stringBuffer = new StringBuffer
    var line : String = bufferedReader.readLine
    while( line != null) {
      stringBuffer.append(line)
      line = bufferedReader.readLine
    }

    !stringBuffer.toString.contains("#")
  }

  implicit def utilityMerger(self : ContainerRoot) = RichContainerRoot(self)

}


case class RichContainerRoot(self : ContainerRoot) extends MergerTestSuiteHelper {
  /*
  def testSave = {
    try{
      var fileTemp = File.createTempFile("art2temp", ".art2")
      Art2XmiHelper.save(fileTemp.getAbsolutePath, self)
    } catch {
      case _ @ e => e.printStackTrace; fail()
    }
  }*/

  def testSave(path : String,file:String) {
    try{
      KevoreeXmiHelper.save(this.getClass.getClassLoader.getResource(path).getPath+"/"+file, self)
      assert(hasNoRelativeReference(path,file) )
    } catch {
      case _ @ e => e.printStackTrace(); fail()
    }
  }

  def setLowerHashCode : ContainerRoot = {
       self.getDeployUnits.foreach(du=> du.setHashcode(0+""))
      self
  }

  

}
