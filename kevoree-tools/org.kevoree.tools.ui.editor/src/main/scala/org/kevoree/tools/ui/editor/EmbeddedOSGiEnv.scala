package org.kevoree.tools.ui.editor

import actors.DaemonActor
import org.apache.felix.framework.Felix
import org.osgi.framework.Constants


object EmbeddedOSGiEnv extends DaemonActor {


  var fwk: Felix = null
  var configProps = new java.util.HashMap[String, Object]()
  /* if (cacheDir != null) {
  configProps.put(Constants.FRAMEWORK_STORAGE, cacheDir.getAbsolutePath());
}  */

  configProps.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, ",sun.misc");
  configProps.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
  // configProps.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, EmbeddedActivators.getActivators());

  Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {

    def run() {
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

  start()

  def act() {

  }


}