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

package org.kevoree.framework

import java.io._
class FileHelper(file : File) {
  def write(text : String) : Unit = {
    val fw = new FileWriter(file)
    try{ fw.write(text) }
    finally{ fw.close }
  }
  def write(texts : List[String]) : Unit = {
    val fw = new FileWriter(file)
    try{
      texts.foreach{t=>
        fw.write(t)
        fw.write('\n')
      }
      fw.write('\n')

    }
    finally{ fw.close }
  }

  def foreachLine(proc : String=>Unit) : Unit = {
    val br = new BufferedReader(new FileReader(file))
    try{ while(br.ready) proc(br.readLine) }
    finally{ br.close }
  }
  def deleteAll : Unit = {
    def deleteFile(dfile : File) : Unit = {
      if(dfile.isDirectory){
        val subfiles = dfile.listFiles
        if(subfiles != null)
          subfiles.foreach{ f => deleteFile(f) }
      }
      dfile.delete
    }
    deleteFile(file)
  }
}
object FileHelper{
  implicit def file2helper(file : File) = new FileHelper(file)
}