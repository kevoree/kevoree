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

package org.kevoree.merger

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.{KevoreeFactory, ContainerRoot}

object MergerApp {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {

    val merger = new RootMerger
    val model1 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/sky/org.kevoree.library.sky.minicloud/target/classes/KEV-INF/lib.kev")
   // val model2 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.javaseNode/target/classes/KEV-INF/lib.kev")
    val model2 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/arduino/org.kevoree.library.arduino.nodeType/target/classes/KEV-INF/lib.kev")

    merger.merge(model1, model2)

    println(KevoreeXmiHelper.saveToString(model1,true))



  }
  


}
