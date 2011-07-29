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
package org.kevoree.tools.model2code

import java.net.URI
import japa.parser.ast.CompilationUnit
import japa.parser.JavaParser
import java.io.{PrintWriter, FileOutputStream, FileInputStream, File}

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 29/07/11
 * Time: 17:40
 */

trait CompilationUnitHelpers {

   def compilationUnitLoader(fileLocation: URI) : CompilationUnit = {
    val file = new File(fileLocation)
    if (!file.exists) {
      new CompilationUnit
    } else {
      val in = new FileInputStream(file)
      val cu = JavaParser.parse(in)
      cu
    }
  }

  def compilationUnitWriter(cu: CompilationUnit, fileLocation: URI) {

    val file = new File(fileLocation)
    if (!file.exists) {
      val folders = new File(new URI(fileLocation.toString.substring(0, fileLocation.toString.lastIndexOf("/"))))
      folders.mkdirs
      file.createNewFile
    }

    val out = new FileOutputStream(file)
    val br = new PrintWriter(out)

    br.print(cu.toString)
    br.flush
    br.close
    out.close
  }

}