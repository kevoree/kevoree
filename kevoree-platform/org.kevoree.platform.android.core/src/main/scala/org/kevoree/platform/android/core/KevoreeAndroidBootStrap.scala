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
package org.kevoree.platform.android.core

import log.KevoreeLogbackService
import org.slf4j.LoggerFactory
import org.kevoree.core.impl.{KevoreeCoreBean, KevoreeConfigServiceBean}
import android.content.Context
import org.kevoree.api.service.core.script.{KevScriptEngine, KevScriptEngineFactory}
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.{KevoreeFactory, ContainerRoot}
import java.util.jar.JarFile
import org.kevoree.framework.KevoreeXmiHelper
import android.app.Activity
import android.widget.TabHost
import org.kevoree.api.Bootstraper
import android.util.Log
import org.kevoree.api.service.core.logging.KevoreeLogService

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:04
 */

class KevoreeAndroidBootStrap {

  /* Bootstrap Model to init default nodeType */
  val logger = LoggerFactory.getLogger(this.getClass)
  var started = false
  var coreBean: KevoreeCoreBean = null;

  def start(act: Activity, ctx: android.content.Context, clusterCL: ClassLoader, kui: org.kevoree.platform.android.ui.KevoreeAndroidUIScreen, nodeName: String) {
    if (started) {
      return;
    }

    //Build UI PROXY
    val uiService = new KevoreeActivityAndroidService(act, kui)
    org.kevoree.android.framework.helper.UIServiceHandler.setUIService(uiService)

    try {
      val configBean = new KevoreeConfigServiceBean
      coreBean = new KevoreeCoreBean
      val clazz = clusterCL.loadClass("org.kevoree.tools.aether.framework.android.NodeTypeBootstrapHelper")
      val bootstraper = clazz.getConstructor(classOf[Context], classOf[ClassLoader]).newInstance(ctx, clusterCL).asInstanceOf[org.kevoree.api.Bootstraper]
      val logbackService = new KevoreeLogbackService()
      bootstraper.setKevoreeLogService(logbackService)

     // clazz.getMethod("setKevoreeLogService", classOf[KevoreeLogService]).invoke(bootstraper,logbackService);



      coreBean.setBootstraper(bootstraper.asInstanceOf[Bootstraper])
      coreBean.setConfigService(configBean);
      coreBean.setKevsEngineFactory(new KevScriptEngineFactory() {
        override def createKevScriptEngine(): KevScriptEngine = {
          try {
            return new org.kevoree.tools.marShell.KevScriptCoreEngine(coreBean)
          } catch {
            case _@e => e.printStackTrace();
          }
          null
        }

        override def createKevScriptEngine(srcModel: ContainerRoot): KevScriptEngine = {
          try {
            return new org.kevoree.tools.marShell.KevScriptOfflineEngine(srcModel)
          } catch {
            case _@e => e.printStackTrace()
          }
          null
        }
      });

      val dummyKCL = new KevoreeJarClassLoader{

        override def loadClass(className: String) = {
          //Log.i("u","try to load from stream "+className)
          val clazz = clusterCL.loadClass(className)
          //Log.i("resolved","r"+clazz)
          clazz
        }
      }
      dummyKCL.lockLinks();

      val selfRegisteredClazz = bootstraper.asInstanceOf[Bootstraper].getClass
      selfRegisteredClazz.getMethods.find(m => m.getName == "registerManuallyDeployUnit").map {
        m =>
          m.invoke(bootstraper, "org.kevoree.adaptation.model", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.api", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.basechecker", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.core", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.framework", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.kcl", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.kompare", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.merger", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.model", "org.kevoree", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.tools.annotation.api", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.tools.android.framework", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.tools.javase.framework", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);
          m.invoke(bootstraper, "org.kevoree.tools.aether.framework.android", "org.kevoree.tools", KevoreeFactory.getVersion, dummyKCL);

      }


      // selfRegisteredClazz.get


      /*
    for (Method m: selfRegisteredClazz.getMethods()) {
    if (m.getName().equals("registerManuallyDeployUnit")) {
      logger.debug("Manual Init Aether KCL");
      // m.invoke(bootstraper, "org.kevoree.tools.aether.framework", "org.kevoree.tools", KevoreeFactory.getVersion(), jcl);

      // logger.debug("Manual Init MarShell KCL");
      // m.invoke(bootstraper, "org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion(), scriptEngineKCL);

      logger.debug("Manual Init AdaptationModel");
      m.invoke(bootstraper, "org.kevoree.adaptation.model", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.api", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.basechecker", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.core", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.framework", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.kcl", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.kompare", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.merger", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.model", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);

      m.invoke(bootstraper, "org.kevoree.tools.annotation.api", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);
      m.invoke(bootstraper, "org.kevoree.tools.android.framework", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);

    }
  }    */

      /*
  bootstraper.registerManuallyDeployUnit("org.kevoree.tools.aether.framework", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.tools.aether.framework.android", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.tools.marShell", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);

  bootstraper.registerManuallyDeployUnit("org.kevoree.adaptation.model", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.api", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.basechecker", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.core", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.framework", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.kcl", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.kompare", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.merger", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.model", "org.kevoree", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.tools.annotation.api", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);
  bootstraper.registerManuallyDeployUnit("org.kevoree.tools.android.framework", "org.kevoree.tools", KevoreeFactory.getVersion(), dummyKCL);
      */
      coreBean.start();


      val file = bootstraper.asInstanceOf[Bootstraper].resolveKevoreeArtifact("org.kevoree.library.model.bootstrap.android", "org.kevoree.library.model", KevoreeFactory.getVersion)
      val jar = new JarFile(file)
      val entry = jar.getJarEntry("KEV-INF/lib.kev")
      val bootstrapModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry))
      if (bootstrapModel != null) {
        try {
          logger.debug("Bootstrap step !");
          val bsh = new BootstrapHelper
          bsh.initModelInstance(bootstrapModel, "AndroidNode", "NanoRestGroup", nodeName);
          coreBean.updateModel(bootstrapModel);
        } catch {
          case _@e => logger.error("Bootstrap failed", e);
        }
      } else {
        logger.error("Can't bootstrap nodeType");
      }
      started = true;
    } catch {
      case _@e => e.printStackTrace();
    }
  }

  def stop() {
    if (!started) {
      return;
    }
    coreBean.stop();
    started = false;
  }

}
