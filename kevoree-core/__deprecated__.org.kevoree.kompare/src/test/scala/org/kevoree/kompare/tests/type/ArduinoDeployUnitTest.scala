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
package org.kevoree.kompare.tests.`type`

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

import org.kevoree.kompare.KevoreeKompareBean
import org.junit._
import org.kevoree.kompare.tests.KompareSuite

class ArduinoDeployUnitTest extends KompareSuite {

  var component : KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def testNoTypeInstall()={
   // val kompareModel = component.kompare(emptyModel, model("test_deployUnit/testArduino.kev"), "duke")
    /*
    kompareModel shouldContainSize(classOf[UpdateDeployUnit],1)
    kompareModel shouldContain(classOf[UpdateType],"ComponentPrimitiveTypeService")
    kompareModel shouldContain(classOf[UpdateType],"ComponentA")

    kompareModel shouldContain(classOf[UpdateInstance],"ComponentPrimitiveTypeService-193784848")
    kompareModel shouldContain(classOf[UpdateInstance],"ComponentA-1649555745")
    kompareModel shouldContainSize(classOf[UpdateBinding],2)
    kompareModel shouldContainSize(classOf[RemoveBinding],1)

    kompareModel.verifySize(8)   */

  //  kompareModel.print

  }

}
