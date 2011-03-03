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

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import java.net.URL
import org.kevoree.framework.KevoreeXmiHelper
import java.io.{File, BufferedReader, InputStreamReader, OutputStreamWriter}
import org.eclipse.emf.common.util.URI

class MergeDefaultLibrary extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: Object) {

    try {
      val url = new URL("http://dist.kevoree.org/KevoreeLibrary.php");
      val conn = url.openConnection();
      conn.setConnectTimeout(2000);
      conn.setDoOutput(true);

      //var wr = new OutputStreamWriter(conn.getOutputStream())
      // wr.flush();

      // Get the response
      val rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      val responseURL = new StringBuilder
      var line: String = rd.readLine;
      while (line != null) {
        responseURL.append(line)
        line = rd.readLine
      }
      rd.close();

      val libURL = responseURL.toString.trim

      val urlLib = new URL(libURL);
      val connLib = urlLib.openConnection();
      connLib.setConnectTimeout(2000);
      connLib.setDoOutput(true);

      // Get the response
      val newmodel = KevoreeXmiHelper.loadStream(connLib.getInputStream())

      if (newmodel != null) {
        kernel.getModelHandler().merge(newmodel);

        //CREATE TEMP FILE FROM ACTUAL MODEL
        var tempFile = File.createTempFile("kevoreeEditorTemp", ".kev");
        KevoreeXmiHelper.save(URI.createFileURI(tempFile.getAbsolutePath()).toString(), kernel.getModelHandler().getActualModel);

        //LOAD MODEL
        var loadCmd = new LoadModelCommand();
        loadCmd.setKernel(kernel);
        loadCmd.execute(URI.createFileURI(tempFile.getAbsolutePath()).toString());


      } else {
        System.out.println("Error while loading model");
      }


    } catch {

      case _@e => println("Could not load default lib !")
    }


  }


}
