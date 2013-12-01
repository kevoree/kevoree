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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}
import org.slf4j.LoggerFactory
import java.io._
import java.util.jar.{JarEntry, JarFile}
import java.util
import org.kevoree.loader.JSONModelLoader
import org.kevoree.ContainerRoot
import org.kevoree.serializer.JSONModelSerializer

class MergeDefaultLibrary(groupID: String, arteID: String, version: String) extends Command {

  var logger = LoggerFactory.getLogger(this.getClass)
  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: Object) {
    try {

      val repos = new util.ArrayList[String]()
      repos.add("http://oss.sonatype.org/content/groups/public")

      val file: File = null// AetherResolver.resolve(arteID, groupID, version, repos)
      val jar = new JarFile(file)
      val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")

      val loader = new JSONModelLoader();
      val saver = new JSONModelSerializer();

      val newmodel = loader.loadModelFromStream(jar.getInputStream(entry)).get(0).asInstanceOf[ContainerRoot]
      if (newmodel != null) {
        kernel.getModelHandler.merge(newmodel);

        //CREATE TEMP FILE FROM ACTUAL MODEL
        val tempFile = File.createTempFile("kevoreeEditorTemp", ".kev")
        PositionedEMFHelper.updateModelUIMetaData(kernel);
        val fop = new FileOutputStream(tempFile)
        saver.serializeToStream(kernel.getModelHandler.getActualModel,fop)
        fop.flush()
        fop.close()
        //LOAD MODEL
        val loadCmd = new LoadModelCommand();
        loadCmd.setKernel(kernel);
        loadCmd.execute(tempFile.getAbsolutePath);


      } else {
        logger.error("Error while loading model");
      }


    } catch {

      case _@e => logger.error("Could not load default lib ! => " + e.getMessage); e.printStackTrace()
    }


  }


}
