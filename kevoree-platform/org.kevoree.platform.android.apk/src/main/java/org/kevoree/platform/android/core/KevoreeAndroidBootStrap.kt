
package org.kevoree.platform.android.core

import android.app.Activity
import android.content.pm.ActivityInfo
import java.util.jar.JarFile
import org.kevoree.ContainerRoot
import org.kevoree.android.framework.helper.UIServiceHandler
import org.kevoree.api.Bootstraper
import org.kevoree.api.service.core.script.KevScriptEngine
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.core.impl.KevoreeCoreBean
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.log.Log
import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 29/02/12
 * Time: 18:04
 */

class KevoreeAndroidBootStrap {

    /* Bootstrap Model to init default nodeType */
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
            Log.info("Starting Kevoree {}", factory.getVersion())
            bootstraper.setKevoreeLogService(SimpleServiceKevLog());
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

            bootstraper.registerManuallyDeployUnit("jgrapht-jdk1.5", "org.jgrapht", "0.7.3", dummyKCL)
            bootstraper.registerManuallyDeployUnit("kotlin-runtime", "org.jetbrains.kotlin", "*", dummyKCL);
            bootstraper.registerManuallyDeployUnit("kotlin-stdlib", "org.jetbrains.kotlin", "*", dummyKCL);
            bootstraper.registerManuallyDeployUnit("jfilter-library", "fr.inria.jfilter", "1.3", dummyKCL);
//            bootstraper.registerManuallyDeployUnit("org.kevoree.adaptation.model", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.api", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.basechecker", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.core", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.framework", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.kcl", "org.kevoree", "*", dummyKCL)
//            bootstraper.registerManuallyDeployUnit("org.kevoree.kompare", "org.kevoree", "*", dummyKCL)
//            bootstraper.registerManuallyDeployUnit("org.kevoree.merger", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.model", "org.kevoree", "*", dummyKCL)

            bootstraper.registerManuallyDeployUnit("org.kevoree.resolver", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("scala-library", "org.scala-lang", "2.9.2", dummyKCL)

//            bootstraper.registerManuallyDeployUnit("org.kevoree.model.context", "org.kevoree", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.annotation.api", "org.kevoree.tools", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.android.framework", "org.kevoree.tools", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.javase.framework", "org.kevoree.tools", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.marShell", "org.kevoree.tools", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.aether.framework", "org.kevoree.tools", "*", dummyKCL)
            bootstraper.registerManuallyDeployUnit("org.kevoree.tools.aether.framework.android", "org.kevoree.tools", "*", dummyKCL)
            coreBean!!.start()
            coreBean!!.registerModelListener(ProgressDialogModelListener(act))

            var askedVersion = "RELEASE";
            if(factory.getVersion().contains("SNAPSHOT")){
                askedVersion = "LATEST";
            }

            val file = (bootstraper as Bootstraper).resolveKevoreeArtifact( "org.kevoree.library.android.jexxus","org.kevoree.corelibrary.android", askedVersion)
            val jar = JarFile(file)
            val entry = jar.getJarEntry("KEV-INF/lib.kev")
            val bootstrapModel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry!!)!!)
            if (bootstrapModel != null) {
                try {

                    Log.info("Bootstrap step will init " + coreBean!!.getNodeName())
                    Log.debug("Bootstrap step !")
                    val bsh = BootstrapHelper()
                    bsh.initModelInstance(bootstrapModel, "AndroidNode", groupName, nodeName)
                    coreBean!!.updateModel(bootstrapModel)
                } catch(e: Exception) {
                    Log.error("Bootstrap failed", e)
                }
            } else {
                Log.error("Can't bootstrap nodeType")
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
