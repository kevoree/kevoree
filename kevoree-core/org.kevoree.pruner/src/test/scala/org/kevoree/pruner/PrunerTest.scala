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

package org.kevoree.pruner

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import org.junit._
import org.junit.Assert._
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper

class PrunerTest {

  @Test
  def pruneLibraryTest() {
    var pruner = new KevoreePrunerComponent()

    var modelPath = this.getClass.getClassLoader.getResource("complete/merged.kev").getPath
    var model = KevoreeXmiHelper.load(modelPath)

    val result = pruner.prune(model, "Kevoree-Components")

    safeSave(this.getClass.getClassLoader.getResource("complete").getPath+"/pruned.kev", result)
    
  }

  def safeSave(location : String, model : ContainerRoot) {

    KevoreeXmiHelper.save(location, model)
    hasNoRelativeReference(location)

  }

  def hasNoRelativeReference(path:String) = {
    var bufferedReader = new BufferedReader(new FileReader(new File(path)))

    var stringBuffer = new StringBuffer
    var line : String = bufferedReader.readLine
    while( line != null) {
      stringBuffer.append(line)
      line = bufferedReader.readLine
    }

    assertFalse("Model: " + path + " contains relative references.", stringBuffer.toString.contains("#"))

  }


}
