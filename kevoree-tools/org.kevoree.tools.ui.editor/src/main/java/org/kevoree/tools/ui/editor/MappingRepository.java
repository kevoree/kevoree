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
/* $Id: MappingRepository.java 12827 2010-10-07 09:28:51Z francoisfouquet $ 
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */
package org.kevoree.tools.ui.editor;

import java.util.HashMap;
import org.eclipse.emf.ecore.EObject;

/**
 *
 * @author ffouquet
 */
public class MappingRepository {

    private HashMap<Object, EObject> uiTOemf = new HashMap<Object, EObject>();
    private HashMap<EObject, Object> emfTOui = new HashMap<EObject, Object>();

    public void unbind(Object uio, EObject emfo){
        uiTOemf.remove(uio);
        emfTOui.remove(emfo);
    }

    public void bind(Object uio, EObject emfo) {
        uiTOemf.put(uio, emfo);
        emfTOui.put(emfo, uio);
    }

    public Object get(Object o) {
        if (uiTOemf.containsKey(o) || emfTOui.containsKey(o)) {
            if (uiTOemf.containsKey(o)) {
                return uiTOemf.get(o);
            }
            if (emfTOui.containsKey(o)) {
                return emfTOui.get(o);
            }

        } else {
            return null;
        }
        return null;
    }
}
