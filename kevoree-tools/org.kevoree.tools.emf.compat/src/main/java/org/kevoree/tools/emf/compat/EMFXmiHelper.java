/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
package org.kevoree.tools.emf.compat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kemf.compat.kevoree.KevoreePackage;

/**
 *
 * @author duke
 */
public class EMFXmiHelper {
    
   public static org.kemf.compat.kevoree.ContainerRoot loadStream(InputStream input) throws IOException {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(org.kemf.compat.kevoree.KevoreePackage.eNS_URI, org.kemf.compat.kevoree.KevoreePackage.eINSTANCE);
    Resource ressource = rs.createResource(URI.createURI(org.kemf.compat.kevoree.KevoreePackage.eNS_URI));
    ((XMIResource)ressource).getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    ((XMIResource)ressource).getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    ressource.load(input, new HashMap());
    return (org.kemf.compat.kevoree.ContainerRoot) ressource.getContents().get(0);
  }

  public static void saveStream(OutputStream output, org.kemf.compat.kevoree.ContainerRoot root) throws IOException {
    ResourceSet rs = new ResourceSetImpl();
    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
    rs.getPackageRegistry().put(KevoreePackage.eNS_URI, KevoreePackage.eINSTANCE);
    URI uri1 = URI.createURI(KevoreePackage.eNS_URI + "MEMORY");
    Resource res = rs.createResource(uri1);
    ((XMIResource)res).getDefaultLoadOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    ((XMIResource)res).getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");
    res.getContents().add(root);
    res.save(output, new HashMap());
  }
    
    
}
