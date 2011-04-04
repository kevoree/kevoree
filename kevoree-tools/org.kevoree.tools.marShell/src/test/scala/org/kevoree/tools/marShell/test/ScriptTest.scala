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

package org.kevoree.tools.marShell.test

import org.junit._
import org.junit.Assert._
import org.kevoree.KevoreeFactory
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.parser.ParserUtil


class ScriptTest {

  @Test
  def primaryTest() {

    val newModel = KevoreeFactory.eINSTANCE.createContainerRoot

    val parser =new KevsParser();

    val modelPath = "/scripts/t1.kevs"
    val oscript =  parser.parseScript(ParserUtil.loadFile(getClass().getResource(modelPath).getPath))

    assertTrue("Error "+parser.lastNoSuccess, oscript.isDefined)
    val context = KevsInterpreterContext(newModel)
    assertNotNull(context)
    assertTrue("Interpreter Result was false for model: " + modelPath, oscript.get.interpret(context))
    
    
    ParserUtil.save(getClass().getResource("/scripts").getPath + "/modified.kev", newModel)
  }

  @Test
  def addServicePortTest() {
    val textScript = "tblock {addPortType sp => ServicePort}"
    val newModel = KevoreeFactory.eINSTANCE.createContainerRoot
    val parser = new KevsParser();
    val oscript =  parser.parseScript(textScript)
    oscript.get.interpret(KevsInterpreterContext(newModel))
    
  }


}

