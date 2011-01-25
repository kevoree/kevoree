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

import org.kevoree.api.service.core.kompare.ModelKompareService
import org.kevoree.kompare.KevoreeKompareBean
import org.kevoree.kompare.tests.KompareSuite
import org.junit._
import org.kevoreeAdaptation._


class TypeInstallAndRemoveTest extends KompareSuite {

  var component : ModelKompareService = null

  @Before def initialize() {
    component = new KevoreeKompareBean
  }

  @Test def testNoTypeInstall()={
    var kompareModel = component.kompare(emptyModel, model("test_type/noTypeInstall.art2"), "duke")
    kompareModel verifySize 0
  }

  @Test def testOnlyOneDeployUnitInstall_INITNODE()={
    var kompareModel = component.kompare(emptyModel, model("test_type/onlyOneDeployUnitInstall.art2"), "duke")
    kompareModel shouldContainSize(classOf[AddDeployUnit],1)
  }

  @Test def testOnlyOneDeployUnitInstall_UPDATENODE()={
    var kompareModel = component.kompare(model("test_type/noTypeInstall.art2"), model("test_type/onlyOneDeployUnitInstall.art2"), "duke")
    kompareModel shouldContainSize(classOf[AddDeployUnit],1)
    kompareModel shouldContain(classOf[AddType],"ComponentPrimitiveTypeService")
    kompareModel shouldContain(classOf[AddType],"ComponentB")
    kompareModel shouldContain(classOf[AddInstance],"ComponentB--10313997")
    kompareModel shouldContain(classOf[AddInstance],"ComponentPrimitiveTypeService--791402174")
  }

  @Test def testnoTypeInstall_UPDATENODE()={
    var kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"), model("test_type/onlyOneDeployUnitInstall.art2"), "duke")
    kompareModel verifySize 0
  }

  @Test def testnoTypeDeployUnitUninstall_UPDATENODE()={
    var kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"), model("test_type/noTypeDeployUnitUninstall.art2"), "duke")
    kompareModel shouldContainSize(classOf[RemoveDeployUnit],0)
  }


  @Test def testOnlyOneDeployUnitUninstall_UPDATENODE()={
    var kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"), model("test_type/onlyOneDeployUnitUninstall.art2"), "duke")
    kompareModel shouldContainSize(classOf[RemoveDeployUnit],1)
    kompareModel shouldContain(classOf[RemoveType],"ComponentPrimitiveTypeService")
    kompareModel shouldContain(classOf[RemoveType],"ComponentB")
    kompareModel shouldContain(classOf[RemoveInstance],"ComponentB--10313997")
    kompareModel shouldContain(classOf[RemoveInstance],"ComponentPrimitiveTypeService--791402174")
  }

  @Test def testOnlyOneDeployUnitUninstall_STOPNODE()={
    var kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"),emptyModel, "duke")
    kompareModel shouldContainSize(classOf[RemoveDeployUnit],1)
  }

  @Test def testuninstall_STOPNODE()={
    var kompareModel = component.kompare(model("test_type/onlyOneDeployUnitInstall.art2"),emptyModel, "duke")
    kompareModel shouldContainSize(classOf[RemoveDeployUnit],1)
    kompareModel shouldContain(classOf[RemoveType],"ComponentPrimitiveTypeService")
    kompareModel shouldContain(classOf[RemoveType],"ComponentB")
    kompareModel shouldContain(classOf[RemoveInstance],"ComponentB--10313997")
    kompareModel shouldContain(classOf[RemoveInstance],"ComponentPrimitiveTypeService--791402174")
  }

}

