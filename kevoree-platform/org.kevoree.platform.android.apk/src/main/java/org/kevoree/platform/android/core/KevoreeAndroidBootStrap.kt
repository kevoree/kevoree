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
package org.kevoree.platform.android.core

import android.app.Activity
import android.content.pm.ActivityInfo
import java.util.jar.JarFile
import org.kevoree.ContainerRoot
import org.kevoree.android.framework.helper.UIServiceHandler
import org.kevoree.api.Bootstraper
import org.kevoree.api.service.core.logging.KevoreeLogService
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.core.impl.KevoreeCoreBean
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.kcl.KevoreeJarClassLoader
import org.slf4j.LoggerFactory
import org.slf4j.impl.StaticLoggerBinder
import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:04
 */

class KevoreeAndroidBootStrap {

    /* Bootstrap Model to init default nodeType */
    val logger = LoggerFactory.getLogger(this.javaClass)!!
    var started = false
    var coreBean: KevoreeCoreBean? = null
    val factory = DefaultKevoreeFactory()


    fun start(act: Activity, ctx: android.content.Context, clusterCL: ClassLoader, kui: org.kevoree.platform.android.ui.KevoreeAndroidUIScreen, nodeName: String, groupName: String) {
        if (started) {
            return
        }

        act.runOnUiThread(object : Runnable {
            override fun run() {
                if(  UIServiceHandler.getUIService() != null && UIServiceHandler.getUIService()!!.getRootActivity() != null){
                    UIServiceHandler.getUIService()!!.getRootActivity()!!.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        })


        //Build UI PROXY
        val uiService = KevoreeActivityAndroidService(act, kui)
        org.kevoree.android.framework.helper.UIServiceHandler.setUIService(uiService)

        try {
            coreBean = KevoreeCoreBean()
            coreBean!!.setNodeName(nodeName)
            val bootstraper = org.kevoree.tools.aether.framework.android.NodeTypeBootstrapHelper(ctx, clusterCL)
            logger.info("Starting Kevoree {}", factory.getVersion())
            bootstraper.setKevoreeLogService(StaticLoggerBinder.getSingleton()!!.getLoggerFactory() as KevoreeLogService)
            coreBean!!.setBootstraper(bootstraper as Bootstraper)
            coreBean!!.setKevsEngineFactory(object : KevScriptEngineFactory {
                override fun createKevScriptEngine(): KevScriptEngine {
                    try {
                        return org.kevoree.tools.marShell.KevScriptCoreEngine(coreBean, bootstraper)
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                    null
                }

                override fun createKevScriptEngine(srcModel: ContainerRoot?): KevScriptEngine? {
                    try {
                        return org.kevoree.tools.marShell.KevScriptOfflineEngine(srcModel, bootstraper)
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                    return null
                }
            })

            val dummyKCL = object : KevoreeJarClassLoader() {

                override fun loadClass(className: String?): Class<out Any?>? {
                    val clazz = clusterCL.loadClass(className)
                    return clazz
                }
            }
            dummyKCL.lockLinks()


            bootstraper.registerManuallyDeployUnit("cglib-nodep", "cglib", "2.2.2", dummyKCL)
            bootstraper.registerManuallyDeployUnit("slf4j-api", "org.slf4j", "1.6.4", dummyKCL)
            bootstraper.registerManuallyDeployUnit("slf4j-api", "org.slf4j", "1.6.2", dummyKCL)
            bootstraper.registerManuallyDeployUnit("slf4j-api", "org.slf4j", "1.7.2", dummyKCL)
            bootstraper.registerManuallyDeployUnit("objenesis", "org.objenesis", "1.2", dummyKCL)
            bootstraper.registerManuallyDeployUnit("jgrapht-jdk1.5", "org.jgrapht", "0.7.3", dummyKCL)
            bootstraper.registerManuallyDeployUnit("kotlin-runtime", "org.jetbrains.kotlin", "0.5.162", dummyKCL);
            bootstraper.registerManuallyDeployUnit("kotlin-stdlib", "org.jetbrains.kotlin", "0.5.162", dummyKCL);
            bootstraper.registerManuallyDeployUnit("jfilter-library", "fr.inria.jfilter", "1.3", dummyKCL);

            bootstraper.registerManuallyDeployUnit("org.kevoree.adaptation.model", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.api", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.basechecker", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.core", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.framework", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.kcl", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.kompare", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.merger", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.model", "org.kevoree", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.model.context", "org.kevoree", factory.getVersion(), dummyKCL)

            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.annotation.api", "org.kevoree.tools", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.android.framework", "org.kevoree.tools", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.javase.framework", "org.kevoree.tools", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.marShell", "org.kevoree.tools", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.aether.framework", "org.kevoree.tools", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.aether.framework.android", "org.kevoree.tools", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.library.android.nodeType", "org.kevoree.corelibrary.android", factory.getVersion(), dummyKCL)

            bootstraper.registerManuallyDeployUnit("org.kevoree.library.android.jexxus", "org.kevoree.corelibrary.android", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.library.android.basicGossiper", "org.kevoree.corelibrary.android", factory.getVersion(), dummyKCL)
            bootstraper.registerManuallyDeployUnit("protobuf-java", "com.google.protobuf", "2.4.1", dummyKCL)


            coreBean!!.start()

            coreBean!!.registerModelListener(ProgressDialogModelListener(act))

            val file = (bootstraper as Bootstraper).resolveKevoreeArtifact("org.kevoree.library.model.bootstrap.android", "org.kevoree.corelibrary.model", factory.getVersion())
            val jar = JarFile(file)
            val entry = jar.getJarEntry("KEV-INF/lib.kev")
            val bootstrapModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry!!)!!)
            if (bootstrapModel != null) {
                try {

                    logger.info("Bootstrap step will init " + coreBean!!.getNodeName())
                    logger.debug("Bootstrap step !")
                    val bsh = BootstrapHelper()
                    bsh.initModelInstance(bootstrapModel, "AndroidNode", groupName, nodeName)
                    coreBean!!.updateModel(bootstrapModel)
                } catch(e: Exception) {
                    logger.error("Bootstrap failed", e)
                }
            } else {
                logger.error("Can't bootstrap nodeType")
            }
            started = true
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        if (!started) {
            return
        }
        coreBean!!.stop()
        started = false
    }

}
