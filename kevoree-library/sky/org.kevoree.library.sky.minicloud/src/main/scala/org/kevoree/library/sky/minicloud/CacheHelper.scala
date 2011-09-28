package org.kevoree.library.sky.minicloud

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
import java.io.File

object CacheHelper {

  def cleanupCacheDirectories() {
    val baseDirectory = new File(".")
    baseDirectory.listFiles.foreach {
      subFile =>
        if (subFile.getName.contains("felixCache") && subFile.isDirectory) {
            cleanFolder(subFile)
        }
    }

  }


  def cleanFolder(repName: String) {
//    println("clean=" + repName)
    val f: java.io.File = new java.io.File(repName)
    cleanFolder(f)
  }

  def cleanFolder(f: java.io.File) {
    if (f.exists()) {
      val children = f.list
      for (i <- 0 until children.length) {
        val subF = new java.io.File(f + java.io.File.separator + children(i))
        if (subF.isDirectory) {
          cleanFolder(subF)
        } else {
          subF.delete
        }
      }
      f.delete()
    } else {
      //  log.debug("Cleaning : folder : {} ,not exist",f.getName())
    }
  }


}