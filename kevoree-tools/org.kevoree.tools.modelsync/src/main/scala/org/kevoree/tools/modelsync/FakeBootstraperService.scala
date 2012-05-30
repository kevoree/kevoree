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
package org.kevoree.tools.modelsync

import org.kevoree.tools.aether.framework.NodeTypeBootstrapHelper
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.KevoreeFactory
import org.kevoree.api.Bootstraper

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 09:23
 */

class FakeBootstraperService {

  val bootstrap = new NodeTypeBootstrapHelper
  val dummyKCL = new KevoreeJarClassLoader();

  /* Manually register */
  bootstrap.registerManuallyDeployUnit( "scala-library", "org.scala-lang", "2.9.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.aether.framework", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "cglib-nodep", "cglib", "2.2.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "slf4j-api", "org.slf4j", "1.6.4", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "slf4j-api", "org.slf4j", "1.6.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "objenesis", "org.objenesis", "1.2", dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.adaptation.model", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.api", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.basechecker", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.core", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.framework", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.kcl", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.kompare", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.merger", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.model", "org.kevoree", KevoreeFactory.getVersion, dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.annotation.api", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.tools.javase.framework", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
  bootstrap.registerManuallyDeployUnit(  "org.kevoree.extra.kserial", "org.kevoree.extra", "1.2", dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "jna", "net.java.dev.jna", "3.3.0", dummyKCL)
  bootstrap.registerManuallyDeployUnit(  "jgrapht-jdk1.5", "org.jgrapht", "0.7.3", dummyKCL)


  def getBootstrap : NodeTypeBootstrapHelper = {
    bootstrap
  }

}
