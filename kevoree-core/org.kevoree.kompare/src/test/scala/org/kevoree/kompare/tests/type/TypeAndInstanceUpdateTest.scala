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

package org.kevoree.kompare.tests.`type`

import org.kevoree.kompare.tests.KompareSuite
import org.junit._
import org.kevoreeadaptation._
import org.kevoree.kompare.{JavaSePrimitive, KevoreeKompareBean}

class TypeAndInstanceUpdateTest extends KompareSuite {

  var component: KevoreeKompareBean = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def testNoTypeInstall() {
    val kompareModel = component.kompare(model("test_type/updateBaseModel.art2")/*.setLowerHashCode*/, model("test_type/update2ComponentOneBindingRemove.art2"), "duke")

    kompareModel.print

//    println(kompareModel.getAdaptations.size)

   kompareModel shouldContainSize (JavaSePrimitive.instance$.getUpdateDeployUnit, 1)
//    kompareModel shouldContainSize (JavaSePrimitive.RemoveDeployUnit, 1)
    //kompareModel shouldContainSize (JavaSePrimitive.AddDeployUnit, 1)
//    kompareModel shouldContain (JavaSePrimitive.UpdateType, "ComponentPrimitiveTypeService")
    kompareModel shouldContain (JavaSePrimitive.instance$.getRemoveType, "ComponentPrimitiveTypeService")
    kompareModel shouldContain (JavaSePrimitive.instance$.getAddType, "ComponentPrimitiveTypeService")
//    kompareModel shouldContain (JavaSePrimitive.UpdateType, "ComponentA")
    kompareModel shouldContain (JavaSePrimitive.instance$.getRemoveType, "ComponentA")
    kompareModel shouldContain (JavaSePrimitive.instance$.getAddType, "ComponentA")

//    kompareModel shouldContain (JavaSePrimitive.UpdateInstance, "ComponentPrimitiveTypeService-193784848")
    kompareModel shouldContain (JavaSePrimitive.instance$.getStopInstance, "ComponentPrimitiveTypeService-193784848")
    kompareModel shouldContain (JavaSePrimitive.instance$.getRemoveInstance, "ComponentPrimitiveTypeService-193784848")
    kompareModel shouldContain (JavaSePrimitive.instance$.getAddInstance, "ComponentPrimitiveTypeService-193784848")
    kompareModel shouldContain (JavaSePrimitive.instance$.getUpdateDictionaryInstance, "ComponentPrimitiveTypeService-193784848")
    kompareModel shouldContain (JavaSePrimitive.instance$.getAddInstance, "ComponentPrimitiveTypeService-193784848")
//    kompareModel shouldContain (JavaSePrimitive.UpdateInstance, "ComponentA-1649555745")
    kompareModel shouldContain (JavaSePrimitive.instance$.getStopInstance, "ComponentA-1649555745")
    kompareModel shouldContain (JavaSePrimitive.instance$.getRemoveInstance, "ComponentA-1649555745")
    kompareModel shouldContain (JavaSePrimitive.instance$.getAddInstance, "ComponentA-1649555745")
    kompareModel shouldContain (JavaSePrimitive.instance$.getUpdateDictionaryInstance, "ComponentA-1649555745")
    kompareModel shouldContain (JavaSePrimitive.instance$.getAddInstance, "ComponentA-1649555745")

    kompareModel shouldContainSize (JavaSePrimitive.instance$.getRemoveBinding, 3)
    kompareModel shouldContainSize (JavaSePrimitive.instance$.getAddBinding, 2)

    //kompareModel.verifySize(8)

  }

}
