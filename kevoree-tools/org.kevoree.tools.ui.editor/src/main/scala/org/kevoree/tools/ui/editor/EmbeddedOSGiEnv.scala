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
package org.kevoree.tools.ui.editor

import actors.DaemonActor
import org.apache.felix.framework.Felix
import java.util.Arrays
import org.apache.felix.framework.util.FelixConstants
import generated.SysPackageConstants
import org.osgi.framework.{FrameworkUtil, BundleActivator, Constants}


object EmbeddedOSGiEnv {

  def getFwk = fwk

  var fwk: Felix = null
  var configProps = new java.util.HashMap[String, Object]()
  /* if (cacheDir != null) {
  configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir.getAbsolutePath());
}  */

  configProps.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, SysPackageConstants.getProperty() + ",sun.misc");
  configProps.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

  var activators = Arrays.asList()//(new org.ops4j.pax.url.mvn.internal.Activator).asInstanceOf[BundleActivator])


  configProps.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);


  Runtime.getRuntime.addShutdownHook(new Thread("Felix Shutdown Hook") {

    override def run() {
      try {
        System.err.println("Stopping OSGi Embedded Framework");
        if (fwk != null) {
          fwk.stop();
          fwk.waitForStop(0);
        }
      } catch {
        case _@ex => System.err.println("Error stopping framework: " + ex);
      }
    }
  });

  try {
    fwk = new Felix(configProps);
    fwk.init();
    // (10) Start the framework.
    fwk.start();

    System.out.println("Felix Embedded started");


    // (11) Wait for framework to stop to exit the VM.
    //m_fwk.waitForStop(0);
    //System.exit(0)
  } catch {
    case _@ex => {
      System.err.println("Could not create framework: " + ex);
      ex.printStackTrace();
      System.exit(0);
    }
  }
  /*
  start()

  override def act() {

  }

      */
}