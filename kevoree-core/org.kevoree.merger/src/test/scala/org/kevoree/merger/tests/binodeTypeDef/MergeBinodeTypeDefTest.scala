package org.kevoree.merger.tests.binodeTypeDef

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

import org.junit._
import org.kevoree.merger.KevoreeMergerComponent
import org.kevoree.merger.tests.MergerTestSuiteHelper
import org.kevoree.ChannelType
import org.kevoree.ComponentType
import org.kevoree.api.service.core.merger.MergerService
import scala.collection.JavaConversions._

class MergeBinodeTypeDefTest extends MergerTestSuiteHelper  {

  var component : MergerService = null

  @Before def initialize() {
    component = new KevoreeMergerComponent
  }

  @Test def verifyBinodeMergeTypeDef() {
    val mergedModel = component.merge(model("binodeTypeDef/arduinoSerialCT.kev"), model("binodeTypeDef/javaseSerialCT.kev"))
    mergedModel testSave ("binodeTypeDef","binodeTypeDefMerged.kev")
      /*
    mergedModel.getNodes.find(node => node.getName=="node") match {
      case Some(node) => {
          assert(node.getComponents.size == 3)
          assert( ! node.getComponents.exists(cmpInst => cmpInst.getName=="ComponentA--215276142"))
        }
      case None => error("No node named 'node' in the model")
    } */

  }

}
