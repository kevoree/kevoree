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

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/03/13
 * Time: 08:12
 */
public class MethodAnnotationResolver {

    private Class base = null;
    private HashMap<Class, Method> methods = new HashMap<Class, Method>();

    public MethodAnnotationResolver(Class _base) {
        base = _base;
    }

    public Method resolve(Class annotationClass) {
        return resolve(annotationClass, base);
    }

    private Method resolve(Class annotationClass, Class baseClazz) {
        Method met = methods.get(methods);
        if (met == null) {
            for (Method metLoop : baseClazz.getDeclaredMethods()) {
                if (metLoop.getAnnotation(annotationClass) != null) {
                    met = metLoop;
                    methods.put(annotationClass, met);
                    return met;
                }
            }
        }
        if(baseClazz.getSuperclass() != null){
            met = resolve(annotationClass, baseClazz.getSuperclass());
            if (met != null) {
                return met;
            }
        }
        for (Class it : baseClazz.getInterfaces()) {
            met = resolve(annotationClass, it);
            if (met != null) {
                return met;
            }
        }
        return met;
    }


}
