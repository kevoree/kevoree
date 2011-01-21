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

package org.kevoree.framework.annotation.processor

import org.kevoree.KevoreePackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.XMIResource
import org.eclipse.emf.ecore.xmi.XMLResource
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.kevoree.ContainerRoot
import java.io.InputStream
import java.io.OutputStream
import java.util.HashMap
import org.eclipse.emf.common.util.URI

object Art2XmiHelper {
  def save(uri:String,root : ContainerRoot) = {
    var rs :ResourceSetImpl = new ResourceSetImpl();

    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    var uri1:URI   = URI.createURI(uri)
    var res : Resource = rs.createResource(uri1)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.asInstanceOf[XMIResource].getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.getContents.add(root)
    res.save(new HashMap());
  }

  def load(uri:String) : ContainerRoot = {
    var rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    var res = rs.getResource(URI.createURI(uri), true)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.asInstanceOf[XMIResource].getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    var result = res.getContents().get(0);
    //println(res)
    return result.asInstanceOf[ContainerRoot];
  }

  def loadStream(input : InputStream) : ContainerRoot = {
    var rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    var ressource = rs.createResource(URI.createURI(KevoreePackage.eNS_URI));
    ressource.asInstanceOf[XMIResource].getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    ressource.asInstanceOf[XMIResource].getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    ressource.load(input, new HashMap());
    ressource.getContents.get(0).asInstanceOf[ContainerRoot];
  }

  def saveStream(output : OutputStream,root : ContainerRoot) :Unit = {
    var rs :ResourceSetImpl = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    var uri1:URI   = URI.createURI(KevoreePackage.eNS_URI+"MEMORY")
    var res : Resource = rs.createResource(uri1)
    res.asInstanceOf[XMIResource].getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.asInstanceOf[XMIResource].getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.getContents.add(root)
    res.save(output, new HashMap())
  }

}
