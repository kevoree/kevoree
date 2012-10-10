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
package org.kevoree.tools.annotation.bytecodeprocessor

import org.clapper.classutil.ClassFinder
import java.io.File
import org.kevoree.framework.AbstractComponentType

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/10/12
 * Time: 16:13
 */
object Tester extends App {

  println("Hello")

  val finder = ClassFinder(List(new File("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-corelibrary/javase/org.kevoree.library.javase.fakeDomo/target/classes")))
  val classes = finder.getClasses().filter(_.superClassName == classOf[AbstractComponentType].getName)
  classes.foreach {
    cl => {

      cl.modifiers.foreach{
        m => m.id
      }
      cl.methods.foreach{
        m=> m.name
      }

      /*
      cl.fields.foreach{
        f => println(f.name)
      } */

      println(cl.toString)
    }
  }

}
