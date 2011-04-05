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

    var merger = new RootMerger
  //  var model1 = KevoreeFactory.eINSTANCE.createContainerRoot//KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-core/org.kevoree.merger/src/test/resources/library/deflib.kev")
   
   // var model2 = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-core/org.kevoree.merger/src/test/resources/assemblies/base-assembly.art2")
   // var model1 = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-core/org.kevoree.merger/src/test/resources/assemblies/PlusInstance.art2")

  
    //var model1 = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-core/org.kevoree.merger/src/test/resources/nodeType/defchannels1.1.0.kev")
    var model1 = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-library/org.kevoree.library.arduino.nodeType/src/test/resources/models/TempSensorAlone.kev")
    var model2 = KevoreeXmiHelper.load("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-library/org.kevoree.library.arduino.channels/target/generated-sources/kevoree/KEV-INF/lib.kev")
    
    merger.merge(model1, model2)

    KevoreeXmiHelper.save("/Users/ffouquet/Documents/DEV/dukeboard_github/kevoree/kevoree-core/org.kevoree.merger/src/test/resources/deployUnit/merged.kev", model1)

  }
  


}
