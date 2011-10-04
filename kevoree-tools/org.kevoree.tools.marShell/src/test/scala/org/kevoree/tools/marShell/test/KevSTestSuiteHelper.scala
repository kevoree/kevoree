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
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.kevoree.{KevoreePackage, KevoreeFactory, ContainerRoot}
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.{XMLResource, XMIResource}
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import java.util.HashMap
import org.kevoree.tools.marShell.parser.{ParserUtil, KevsParser}
import org.kevoree.tools.marShell.ast.Script

trait KevSTestSuiteHelper extends JUnitSuite {

  /* UTILITY METHOD */
  def model(url: String): ContainerRoot = {
    val modelPath = this.getClass.getClassLoader.getResource(url).getPath
    load(modelPath)
  }

  def emptyModel = KevoreeFactory.eINSTANCE.createContainerRoot

  def load(uri: String): ContainerRoot = {
    val rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("kev", new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    val res = rs.getResource(URI.createURI(uri), true)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.asInstanceOf[XMIResource].getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    val result = res.getContents().get(0);
    return result.asInstanceOf[ContainerRoot];
  }

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
      save(this.getClass.getClassLoader.getResource(path).getPath + "/" + file, self)
      assertTrue("At least one relative reference have been detected in model " + path + "/" + file, hasNoRelativeReference(path, file))
    } catch {
      case _@e => e.printStackTrace(); fail()
    }
  }

  def save(uri: String, root: ContainerRoot) = {
    val rs: ResourceSetImpl = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    val uri1: URI = URI.createURI(uri)
    val res: Resource = rs.createResource(uri1)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.asInstanceOf[XMIResource].getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.getContents.add(root)
    res.save(new HashMap());
  }


  def setLowerHashCode(): ContainerRoot = {
    self.getDeployUnits.foreach(du => du.setHashcode(0 + ""))
    self
  }


}
