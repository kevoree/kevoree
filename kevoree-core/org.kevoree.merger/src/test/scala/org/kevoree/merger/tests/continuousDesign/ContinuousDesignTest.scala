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
package org.kevoree.merger.tests.continuousDesign

import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.api.service.core.merger.MergerService
import org.kevoree.merger.KevoreeMergerComponent
import org.junit.{Test, Before}
import org.kevoree.KevoreeFactory
import org.kevoree.framework.KevoreeXmiHelper

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/10/11
 * Time: 18:40
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class ContinuousDesignTest extends MergerTestSuiteHelper {
  var component: MergerService = null

  @Before def initialize () {
    component = new KevoreeMergerComponent
  }

  //@Test
  def mergeSetOfVals () {
    val mergedModel = component
      .merge(model("continuousdesign/mergeSetOfVals1.kev"), model("continuousdesign/mergeSetOfVals.kev"))

//    println(KevoreeXmiHelper.saveToString(mergedModel, true))
    
    /*mergedModel.getTypeDefinitions.find(tp => tp.getName == "FakeConsole") match {
      case None => fail("FakeConsole doesn't exist")
      case Some(tp) => {
        tp.
      }
    }*/
  }

  @Test
   def mergeFakeDomo () {
     val mergedModel = component
       .merge(model("continuousdesign/mergeOptionalPort.kev"), model("continuousdesign/mergeOptionalPort1.kev"))

     println(KevoreeXmiHelper.saveToString(mergedModel, true))

     /*mergedModel.getTypeDefinitions.find(tp => tp.getName == "FakeConsole") match {
       case None => fail("FakeConsole doesn't exist")
       case Some(tp) => {
         tp.
       }
     }*/
   }
}