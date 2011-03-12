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

package org.kevoree.tools.model2code

import japa.parser.JavaParser
import japa.parser.ast.CompilationUnit
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import java.net.URI
import org.kevoree.ComponentType

class Model2Code {

  def modelToCode(componentType : ComponentType, srcRoot : URI) = {
    
    
    var fileLocation = srcRoot.toString + componentType.getBean.replace(".", "/").concat(".java")
    var fileLocationUri = new URI(fileLocation)
    
    //Load CU
    var compilationUnit = compilationUnitLoader(fileLocationUri)
    
    if(compilationUnit != null) {
    
      var ctw = new ComponentTypeWorker(componentType, compilationUnit)
      ctw.synchronize
      
      //Save CU
      compilationUnitWriter(compilationUnit, fileLocationUri)
      
    }
    
  }
  
  private def compilationUnitLoader(fileLocation : URI) = {
    var file = new File(fileLocation)
    if(!file.exists) {
      var folders = new File(new URI(fileLocation.toString.substring(0, fileLocation.toString.lastIndexOf("/"))))
      folders.mkdirs
      file.createNewFile
    }
    var in = new FileInputStream(file)
    var cu = JavaParser.parse(in)
    cu
  }
  
  private def compilationUnitWriter(cu : CompilationUnit, fileLocation : URI) = {
        
    var file = new File(fileLocation)
    if( ! file.exists) {
      file.createNewFile
    }
            
    var out = new FileOutputStream(file)
    var br = new PrintWriter(out)
            
    br.print(cu.toString)
    br.flush
    br.close
    out.close
  }
  
}
