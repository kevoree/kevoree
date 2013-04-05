/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
package org.kevoree.tools.ui.editor.command

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.ui.editor.{ModelHelper, PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import org.kevoree.tools.aether.framework.{NodeTypeBootstrapHelper, AetherUtil}
import org.kevoree.KevoreeFactory
import java.io._
import java.util.jar.{JarEntry, JarFile}
import java.util

class MergeDefaultLibrary(lib : Int) extends Command {

  val ALL = 0
  val JAVASE = 1
  val WEBSERVER = 2
  val ARDUINO = 3
  val SKY = 4
  val ANDROID = 5
  val DAUM = 6

  var logger = LoggerFactory.getLogger(this.getClass)

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: Object) {
    try {

      val repos = new util.ArrayList[String]()
      repos.add("http://maven.kevoree.org/release")
      repos.add("http://maven.kevoree.org/snapshots")


      val file : File = lib match {
        case ALL =>AetherResolver.resolve("org.kevoree.library.model.all","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
        case JAVASE =>AetherResolver.resolve("org.kevoree.library.model.javase","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
        case WEBSERVER =>AetherResolver.resolve("org.kevoree.library.model.javase.webserver","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
        case ARDUINO =>AetherResolver.resolve("org.kevoree.library.model.arduino","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
        case SKY =>AetherResolver.resolve("org.kevoree.library.model.sky","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
        case ANDROID =>AetherResolver.resolve("org.kevoree.library.model.android","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
        case DAUM =>AetherResolver.resolve("org.kevoree.library.model.daum","org.kevoree.corelibrary.model",ModelHelper.kevoreeFactory.getVersion,repos)
      }
//       val file = AetherUtil.resolveMavenArtifact("org.kevoree.library.model.all","org.kevoree.library.model",ModelHelper.kevoreeFactory.getVersion,List("http://maven.kevoree.org/release","http://maven.kevoree.org/snapshots"))
       val jar = new JarFile(file)
       val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
       val newmodel = KevoreeXmiHelper.instance$.loadStream(jar.getInputStream(entry))
      if (newmodel != null) {
        kernel.getModelHandler.merge(newmodel);

        //CREATE TEMP FILE FROM ACTUAL MODEL
        val tempFile = File.createTempFile("kevoreeEditorTemp", ".kev");
        PositionedEMFHelper.updateModelUIMetaData(kernel);
        KevoreeXmiHelper.instance$.save(tempFile.getAbsolutePath, kernel.getModelHandler.getActualModel);

        //LOAD MODEL
        val loadCmd = new LoadModelCommand();
        loadCmd.setKernel(kernel);
        loadCmd.execute(tempFile.getAbsolutePath);


      } else {
        logger.error("Error while loading model");
      }


    } catch {

      case _@e => logger.error("Could not load default lib ! => "+e.getMessage ); e.printStackTrace()
    }


  }




}
