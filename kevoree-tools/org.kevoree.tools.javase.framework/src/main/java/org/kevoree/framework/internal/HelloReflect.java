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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/03/13
 * Time: 08:55
 */
public class HelloReflect {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {

        HelloReflect instance = new HelloReflect();

        Long before = System.nanoTime();
        for(int i=0;i<10000;i++){
            instance.start();
        }
        System.out.println(System.nanoTime()-before);


        Method met = null;
        before = System.nanoTime();
        for(Method metLoop : instance.getClass().getMethods()){
            if(metLoop.getName().equals("start")){
                met = metLoop;
            }
        }
        System.out.println("lookup="+(System.nanoTime()-before));

        before = System.nanoTime();
        for(int i=0;i<10000;i++){
            met.invoke(instance);
        }
        System.out.println(System.nanoTime()-before);
    }

    public void start(){
        String s = "Hellllo";
        s = s.concat("lkjlkjlkj");
        s = s.concat("lkjlkjlkj");
        s = s.concat("lkjlkjlkj");
        s = s.concat("lkjlkjlkj");
        s = s.concat("lkjlkjlkj");
    }

}
