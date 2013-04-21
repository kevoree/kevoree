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

package org.kevoree.merger

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.KevoreeFactory
import org.kevoree.log.Log


object MergerApp {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    /*val root: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]

    root.setLevel(Level.DEBUG)*/

    Log.debug("Hello Merger")

    val kevoreeFactory = new org.kevoree.impl.DefaultKevoreeFactory
    val merger = new RootMerger
    val emtpyModel = kevoreeFactory.createContainerRoot

    //val model2 = KevoreeXmiHelper.$instance.load("/Users/duke/Downloads/models/javaModelError.kev")
    val model1 = KevoreeXmiHelper.instance$.load("/Users/duke/Documents/dev/dukeboard/kevoree-kotlin/kevoree-corelibrary/javase/org.kevoree.library.javase.javaseNode/target/classes/KEV-INF/lib.kev")
//    val model3 = KevoreeXmiHelper.load("/home/edaubert/workspace/kevoree/kevoree-corelibrary/android/org.kevoree.library.android.nanohttp/target/generated-sources/kevoree/KEV-INF/lib.kev.debug")


   // val model3 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.javaseNode/target/classes/KEV-INF/lib.kev")
  //  val model4 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.nanohttp/target/classes/KEV-INF/lib.kev")
    /*
    val model1 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.restNode/target/classes/KEV-INF/lib.kev")
    val model2 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.javaseNode/target/classes/KEV-INF/lib.kev")
    val model3 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.fakeDomo/target/generated-sources/kevoree/KEV-INF/lib.kev")
    val model4 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.fakeDomo/target/generated-sources/kevoree/KEV-INF/lib2.kev")
     */


  //   println(KevoreeXmiHelper.$instance.saveToString(model1,true))

    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 1")
    merger.merge(emtpyModel, model1)
//    merger.merge(emtpyModel, model2)
//        merger.merge(emtpyModel, model3)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 2")
    //merger.merge(model1, model3)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 3")
   // merger.merge(model1, model2)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 4")
  //  merger.merge(model1, model4)
    println("!!!!!!!!!!!!!!!!!!!!!!!!!! end")



    println(KevoreeXmiHelper.instance$.saveToString(emtpyModel,true))
//    println(KevoreeXmiHelper.saveToString(model2,true))
//    println(KevoreeXmiHelper.saveToString(model3,true))
//    println(KevoreeXmiHelper.saveToString(emtpyModel,true))



  }
  


}
