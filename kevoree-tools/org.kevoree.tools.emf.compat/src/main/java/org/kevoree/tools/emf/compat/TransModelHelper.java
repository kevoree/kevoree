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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.kevoree.framework.KevoreeXmiHelper;

/**
 *
 * @author duke
 */
public class TransModelHelper {

    public org.kemf.compat.kevoree.ContainerRoot konvert(org.kevoree.ContainerRoot kmfModel) {
        ByteArrayOutputStream kmfOut = new ByteArrayOutputStream();
        KevoreeXmiHelper.saveStream(kmfOut, kmfModel);
        ResourceSet resourceSetMetamodel = new ResourceSetImpl();
        resourceSetMetamodel.getPackageRegistry().put(org.kemf.compat.kevoree.KevoreePackage.eNS_URI, org.kemf.compat.kevoree.KevoreePackage.eINSTANCE);
        resourceSetMetamodel.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
        Resource resourceModel = resourceSetMetamodel.createResource(URI.createURI(org.kemf.compat.kevoree.KevoreePackage.eNS_URI));
        byte[] currentModel = kmfOut.toByteArray();
        ByteArrayInputStream inStream = new ByteArrayInputStream(currentModel);
        org.kemf.compat.kevoree.ContainerRoot emfRoot = null;
        try {
            resourceModel.load(inStream, null);
            emfRoot = (org.kemf.compat.kevoree.ContainerRoot) resourceModel.getContents().get(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            
        }
        return emfRoot;
    }
    
    
    public org.kevoree.ContainerRoot konvert(org.kemf.compat.kevoree.ContainerRoot emfModel){
        return null; //TODO
    }
    
    
}
