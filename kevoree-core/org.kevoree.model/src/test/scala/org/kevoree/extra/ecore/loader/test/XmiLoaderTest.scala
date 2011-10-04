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
package org.kevoree.extra.ecore.loader.test

import java.io.File
import org.junit.Assert._
import org.kevoree.loader.ContainerRootLoader
import org.junit.{Test, BeforeClass}
import org.kevoree.serializer.ModelSerializer
import org.kevoree.{NodeType, ContainerRoot}

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 24/09/11
 * Time: 13:39
 */

object XmiLoaderTest {
  var model: ContainerRoot = null

  @BeforeClass
  def loadXmi() {

    val localModel = ContainerRootLoader.loadModel(new File(getClass.getResource("/defaultlibs.kev").toURI));
    localModel match {
      case Some(m) => {
        model = m
      }
      case None => fail("Model not loaded!")
    }
  }
}

class XmiLoaderTest {
  @Test
  def testSave() {
    val serializer = new ModelSerializer

    println(serializer.serialize(XmiLoaderTest.model))
  }

  @Test
  def loadBootstrapModel() {
     val localModel = ContainerRootLoader.loadModel(new File(getClass.getResource("/bootstrapModel0.kev").toURI));
    localModel match {
      case Some(m) =>
      case None => fail("Model not loaded!")
    }
  }


  @Test
  def deepCheck() {
      XmiLoaderTest.model.getTypeDefinitions.find(td=>td.getName.equals("ArduinoNode")) match {
        case Some(typeDef) => {

          XmiLoaderTest.model.getDeployUnits.find(du => du.getGroupName.equals("org.kevoree.library.arduino")) match {
            case Some(du) => {
              assertTrue("TypeDefinition does not contain its deploy unit.", typeDef.getDeployUnits.contains(du))
            }
            case None => fail("DeployUnit org.kevoree.library.arduino not found")
          }

          typeDef.asInstanceOf[NodeType].getDictionaryType match {
            case Some(dico) =>{
              dico.getAttributes.find{att => att.getName.equals("boardTypeName")} match {
                case Some(att) => {
                  assertTrue(att.getOptional)
                  assertTrue(att.getDatatype.equals("enum=uno,atmega328,mega2560"))
                  dico.getDefaultValues.find(defVal => defVal.getAttribute.equals(att))match{
                    case Some(default) => {
                      assertTrue(default.getValue.equals("uno"))
                    }
                    case None => fail("No default value for att:" + att.getName)
                  }
                }
                case None => fail("No attribute named boardTypeName found in ArduinoNode type dictionary")
              }
            }
            case None => fail("No dictionaryType loaded for ArduinoNode")
          }
        }
        case None => fail("Arduino Node Type not found !")
      }
  }

  //@Test
  def checkRepositories() {
    val repList = XmiLoaderTest.model.getRepositories
    assertTrue("Wrong number of repositories in model." + repList.size, repList.size == 6);
    repList.foreach {
      elem =>
        assertNotNull("eContainer not set for Repository:" + elem.getName, elem.eContainer)
    }

  }

  /*
  @Test
  def checkLibraries() {
    val libList = XmiLoaderTest.model.getLibraries
    assertTrue("Wrong number of libraries in model:" + libList.size, libList.size == 9);
    libList.foreach {
      lib =>
        System.out.println("Lib[name:" + lib.getName + ", sub:" + lib.getSubTypes.mkString("[", ", ", "]") + "]")
      // assertFalse("Lib has no name.",lib.getName.equals(""))
      // assertNotNull("eContainer not set for Library:" + lib.getName,lib.eContainer)
    }

  }

  @Test
  def checkDeployUnits() {
    val duList = XmiLoaderTest.model.getDeployUnits
    assertTrue("Wrong number of DeployUnits in model." + duList.size, duList.size == 20);
    duList.foreach {
      du =>
        assertNotNull("DeployUnit name is null", du.getName)
        assertNotNull("eContainer not set for DeployUnit:" + du.getName, du.eContainer)
        System.out.println("DeployUnit[name:" + du.getName
          + ", groupName:" + du.getGroupName
          + ", unitName:" + du.getUnitName
          + ", version:" + du.getVersion
          + ", url:" + du.getUrl
          + "]") /*
         du.getName match {
           case "" => {
             assertFalse("DeployUnit.getName:"+du.getName+" du.groupName:" +du.getGroupName,du.getGroupName.equals(""))
             assertFalse("DeployUnit.getName:"+du.getName+" du.unitName:"+du.getUnitName,du.getUnitName.equals(""))
             assertFalse("DeployUnit.getName:"+du.getName+" du.version:"+du.getVersion,du.getVersion.equals(""))
           }
           case _ => {
              assertFalse("DeployUnit.getName:"+du.getName+" du.url:"+du.getUrl,du.getUrl.equals(""))
           }
         }
         */
    }
  }


  @Test
  def checkTypeDefinitions() {
    val tdList = XmiLoaderTest.model.getTypeDefinitions
    assertTrue("Wrong number of TypeDefinitions in model." + tdList.size, tdList.size == 33);

    tdList.foreach {
      typeDef =>
        assertNotNull("eContainer not set for TypeDef:" + typeDef.getName, typeDef.eContainer)
        typeDef match {
          case ct: ComponentType => {
            val provPortList = ct.getProvided

            provPortList.foreach(port => assertNotNull("eContainer not set for Port:" + port.getName + " in " + typeDef.getName, port.eContainer))


            if (ct.getName.equals("DigitalLight")) {
              assertTrue("No provided port in " + ct.getName, ct.getProvided != null)
              assertTrue("Not enough providedPorts in " + ct.getName + " : " + ct.getProvided.size, ct.getProvided.size == 3)
            }
          }
          case _ => //No test for other types for the moment//TODO: ADD TESTS
        }

    }
  }    */

}