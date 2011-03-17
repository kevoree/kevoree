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
package org.kevoree.editor.component.creator.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.kevoree.editor.component.creator.model.Art2ModelElement;

/**
 *
 * @author gnain
 */
public class ModelMapper {

    private Map<Art2ModelElement, Object> map;

    public ModelMapper() {
        map = new HashMap<Art2ModelElement, Object>();
    }

    public void put(Art2ModelElement graphicalElement, Object modelElement) {
        map.put(graphicalElement, modelElement);
    }

    public Object getModelElement(Art2ModelElement graphicalElement) {
        return map.get(graphicalElement);
    }

    public Art2ModelElement getGraphicalElement(Object modelElement) {
        for (Map.Entry<Art2ModelElement, Object> e : map.entrySet()) {
            if (e.getValue() == modelElement) {
                return e.getKey();
            }
        }
        return null;
    }

    public void removeModelElement(Art2ModelElement graphicalElement) {
        map.remove(graphicalElement);
    }

    public void removeGraphicalElement(Object modelElement) {
        for (Map.Entry<Art2ModelElement, Object> e : Collections.unmodifiableSet(map.entrySet())) {
            if (e.getValue() == modelElement) {
                map.remove(e.getKey());
                return;
            }
        }
    }

    public void clear() {
        map.clear();
    }
}
