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
import org.slf4j.LoggerFactory
//import ch.qos.logback.classic.{Level, Logger}

object MergerApp {


  val logger = LoggerFactory.getLogger(this.getClass)
  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    /*val root: Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]

    root.setLevel(Level.DEBUG)*/

    logger.debug("Hello Merger")

    val merger = new RootMerger
    val emtpyModel = KevoreeFactory.eINSTANCE.createContainerRoot

    val model1 = KevoreeXmiHelper.load("/home/edaubert/workspace/daum-library/android/org.daum.library.android.replicatingMap/target/generated-sources/kevoree/KEV-INF/lib.kev")
    val model2 = KevoreeXmiHelper.load("/home/edaubert/workspace/daum-library/javase/org.daum.library.javase.replicatingMap/target/generated-sources/kevoree/KEV-INF/lib.kev")
//    val model3 = KevoreeXmiHelper.load("/home/edaubert/workspace/kevoree/kevoree-corelibrary/android/org.kevoree.library.android.nanohttp/target/generated-sources/kevoree/KEV-INF/lib.kev.debug")


   // val model3 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.javaseNode/target/classes/KEV-INF/lib.kev")
  //  val model4 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.nanohttp/target/classes/KEV-INF/lib.kev")
    /*
    val model1 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.restNode/target/classes/KEV-INF/lib.kev")
    val model2 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.javaseNode/target/classes/KEV-INF/lib.kev")
    val model3 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.fakeDomo/target/generated-sources/kevoree/KEV-INF/lib.kev")
    val model4 = KevoreeXmiHelper.load("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.fakeDomo/target/generated-sources/kevoree/KEV-INF/lib2.kev")
     */

    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 1")
//    merger.merge(emtpyModel, model1)
//    merger.merge(emtpyModel, model2)
//        merger.merge(emtpyModel, model3)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 2")
    //merger.merge(model1, model3)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 3")
    merger.merge(model1, model2)
    println("%%%%%%%%%%%%%%%%%%%%%%%%%% 4")
  //  merger.merge(model1, model4)
    println("!!!!!!!!!!!!!!!!!!!!!!!!!! end")



    println(KevoreeXmiHelper.saveToString(model1,true))
//    println(KevoreeXmiHelper.saveToString(model2,true))
//    println(KevoreeXmiHelper.saveToString(model3,true))
//    println(KevoreeXmiHelper.saveToString(emtpyModel,true))



  }
  


}
