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

  def modelToCode(componentType : ComponentType, fileLocation : URI) = {
    
    //Load CU
    var compilationUnit = compilationUnitLoader(fileLocation)
    
    if(compilationUnit != null) {
    
      //Update Annotations
      var annotationUpdater = new AnnotationUpdater
      annotationUpdater.updateAnnotations(componentType, compilationUnit)
      
      
      //Generate Missing Mandatory Methods
      var codeFiller = new CodeFiller
      codeFiller.fillCode(componentType, compilationUnit)
      
      //Generate Utility Methods
      var utilityGen = new UtilityGenerator
      utilityGen.generateUtilities(componentType, compilationUnit)
      
      //Write CU
      compilationUnitWriter(compilationUnit, fileLocation)
      
      
    }
    
  }
  
  def compilationUnitLoader(fileLocation : URI) = {
    var file = new File(fileLocation)
    if(file.exists) {
      var in = new FileInputStream(file)
      var cu = JavaParser.parse(in)
      cu
    } else {
      null
    }
  }
  
  def compilationUnitWriter(cu : CompilationUnit, fileLocation : URI) = {
        
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
