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
package org.kevoree.tools.marShell.parser

import java.util.HashMap
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.XMIResource
import org.eclipse.emf.ecore.xmi.XMLResource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.kevoree.ContainerRoot
import org.kevoree.KevoreePackage
import scala.io.Source
import scala.util.parsing.input.CharArrayReader.EofCh

/**
 * Singleton util methods used by the parser
 */
object ParserUtil {

  def loadFile(uri: String): String = {
    val res = new StringBuilder
    Source.fromFile(uri).getLines.foreach {
      l => res.append(l); res.append('\n')
    }
    res.toString
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

  def load(uri: String): ContainerRoot = {
    val rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry.getExtensionToFactoryMap.put("*", new XMIResourceFactoryImpl());
    rs.getPackageRegistry.put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    val res = rs.getResource(URI.createURI(uri), true)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.asInstanceOf[XMIResource].getDefaultSaveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
    val result = res.getContents.get(0);
    return result.asInstanceOf[ContainerRoot];
  }


}
