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

object InternalCompilerHelper {
	var ext = "scala"
  /* INPUT : SRC DIRECTORY
   * OUTPUT : LIST of FILE path
   *
   * search and build a list of all Scala files from a directory
   *
   */
	def listFile(f : File) : List[String] = {
		var result = List[String]()
		if (f.isDirectory()) {
			for(subf <- f.listFiles){ 
				result = result ++ listFile(subf)
			}
		} else {
			if (f.getName().toLowerCase().endsWith("." + ext)){
				result = result ++ List(f.getAbsolutePath)
			}
		}
		return result
	}

	def deleteDirRecursive(f : File) : Unit = {
		if (f.isDirectory()) {
			for(subf <- f.listFiles){
				deleteDirRecursive(subf)
			}
		} else {
			f.delete
		}
	}
	
	
  
	
	
	
}
