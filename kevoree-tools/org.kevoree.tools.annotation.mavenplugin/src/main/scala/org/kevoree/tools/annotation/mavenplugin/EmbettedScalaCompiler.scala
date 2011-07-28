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
package org.kevoree.tools.annotation.mavenplugin

import java.io.File

object EmbettedScalaCompiler {
	
  def compile(srcPATH : String, outputPATH : String, jars : List[String]) : Int = {

    var startTime = System.currentTimeMillis
    var compilationResult = 0

    /* Src files collect step */
    var listSrcFiles = InternalCompilerHelper.listFile(new File(srcPATH))

    if(listSrcFiles.size > 0){

    /* Build class path */
		
    println("Scala compilation step begin on "+listSrcFiles.size+" files")
		
    var classpath : StringBuilder = new StringBuilder("."+File.pathSeparator)
    for(path <- jars) {
      classpath.append(path+File.pathSeparator)
    }
		
    val compilParams = List("-nowarn","-encoding","UTF8","-g:none","-optimise","-d",outputPATH,"-classpath",classpath.toString) ++ listSrcFiles

    //println("hi"+compilParams)
    /* Compilation step */
   // if(fsc){
   //   try scala.tools.nsc.CompileClient.main0(compilParams.toArray) catch { case e : Exception => compilationResult = 1 }
   // } else {



      _root_.scala.tools.nsc.Main.process(compilParams.toArray)
      compilationResult = if (scala.tools.nsc.Main.reporter.hasErrors) 1 else 0
   // }
		
    var endTime= System.currentTimeMillis() - startTime
    println("Scala compilation step complete in "+(endTime)+" millisecondes ")
    }
    return compilationResult
  }
	

}
