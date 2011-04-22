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

package org.kevoree.tools.ui.editor.command

import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.URL
import javax.swing.JTextField
import org.kevoree.framework.Constants
import org.kevoree.framework.KevoreePlatformHelper
import org.kevoree.framework.KevoreeXmiHelper
import scala.reflect.BeanProperty
import scala.collection.JavaConversions._
import org.kevoree.tools.ui.editor.{PositionedEMFHelper, KevoreeUIKernel}

class SynchNodeIPCommand extends Command {

  //@BeanProperty
  //var field : JTextField = null

  @BeanProperty
  var kernel : KevoreeUIKernel = null

  @BeanProperty
  var destNodeName : String = null
  //   client.start

  def execute(p :Object) {
    try {
      val outStream = new ByteArrayOutputStream
      PositionedEMFHelper.updateModelUIMetaData(kernel);
      KevoreeXmiHelper.saveStream(outStream, kernel.getModelHandler.getActualModel)
      outStream.flush
      // var msg = outStream.toString

      var IP = KevoreePlatformHelper.getProperty(kernel.getModelHandler.getActualModel, destNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP);
      if(IP == "") {IP = "127.0.0.1"}
      var PORT = KevoreePlatformHelper.getProperty(kernel.getModelHandler.getActualModel, destNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT);
      if(PORT == "") {PORT = "8000" }

      println("IP="+IP+",PORT="+PORT)
      new InetSocketAddress(IP,Integer.parseInt(PORT))

    
      // Construct data
      // Send data
      var url = new URL("http://"+IP+":"+PORT+"/model/current");

      var conn = url.openConnection();
      conn.setConnectTimeout(2000);
      conn.setDoOutput(true);
      var wr = new OutputStreamWriter(conn.getOutputStream())
      wr.write(outStream.toString);
      wr.flush();

      // Get the response
      var rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      var line : String = rd.readLine;
      while (line != null) {
        println(line)
        line = rd.readLine
      }
      wr.close();
      rd.close();
    } catch  {

      case _ @ e => e.printStackTrace
    }




  }

}
