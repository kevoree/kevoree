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
package org.kevoree.framework.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/03/13
 * Time: 08:12
 */
public class FieldAnnotationResolver {

    private Class base = null;
    private HashMap<String, List<Field>> fields = new HashMap<String, List<Field>>();

    public FieldAnnotationResolver(Class _base) {
        base = _base;
    }

    public List<Field> resolve(Class annotationClass, Class fieldClass) {
        return resolve(annotationClass, fieldClass, base);
    }

    private List<Field> resolve(Class annotationClass, Class fieldClass, Class baseClazz) {
        List<Field> met = fields.get(fieldClass.getName() + annotationClass.getName());
        if (met == null) {
            met = new ArrayList<Field>();
        }
        for (Field fieldLoop : baseClazz.getDeclaredFields()) {
            if (fieldLoop.getAnnotation(annotationClass) != null && fieldLoop.getType().equals(fieldClass)) {
                met.add(fieldLoop);
                fields.put(fieldClass.getName() + annotationClass.getName(), met);
            }
        }
        if (baseClazz.getSuperclass() != null) {
            met.addAll(resolve(annotationClass, fieldClass, baseClazz.getSuperclass()));
        }
        for (Class it : baseClazz.getInterfaces()) {
            met.addAll(resolve(annotationClass, fieldClass, it));
        }
        return met;
    }


}
