
package org.kevoree.tools.aether.framework.android

import org.kevoree.DeployUnit
import java.io.File
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.tools.aether.framework.JCLContextHandler
import java.util.ArrayList
import org.kevoree.tools.aether.framework.AetherUtil
import org.kevoree.log.Log

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:50
 */

class AndroidJCLContextHandler(val ctx: android.content.Context, val parent: ClassLoader): JCLContextHandler() {


    public override fun unregisterDeployUnitResolver(dur: ((DeployUnit?) -> File?)?) {
        super<JCLContextHandler>.unregisterDeployUnitResolver(dur)
    }
    public override fun registerDeployUnitResolver(dur: ((DeployUnit?) -> File?)?) {
        super<JCLContextHandler>.registerDeployUnitResolver(dur)
    }
    override fun installDeployUnitNoFileInternals(du: DeployUnit): KevoreeJarClassLoader? {
        var resolvedFile: File? = null
        resolvers.any{
            res ->
            try {
                resolvedFile = res.resolve(du)
                true
            } catch(e: Exception){
                false
            }
        }
        if (resolvedFile == null) {
            resolvedFile = AetherUtil.resolveDeployUnit(du)
        }

        if (resolvedFile != null) {
            return installDeployUnitInternals(du, resolvedFile!!)
        } else {
            Log.error("Error while resolving deploy unit " + du.getUnitName())
            return null
        }
    }

    override fun installDeployUnitInternals(du: DeployUnit, file: File): KevoreeJarClassLoader {
        val previousKCL = getKCLInternals(du)
        val res = if (previousKCL != null) {
            Log.debug("Take already installed {}", buildKEY(du))
            previousKCL
        } else {
            val cleankey = buildKEY(du).replace(File.separator, "_")
            val newcl = AndroidKevoreeJarClassLoader(cleankey, ctx, parent)
            newcl.setLazyLoad(false)
            newcl.add(file.getAbsolutePath())
            kcl_cache.put(buildKEY(du), newcl)
            kcl_cache_file.put(buildKEY(du), file)
            //TRY TO RECOVER FAILED LINK
            //TRY TO RECOVER FAILED LINK
            if (failedLinks.containsKey(buildKEY(du))) {
                for(toLinkKCL in failedLinks.get(buildKEY(du))!!){
                    toLinkKCL.addSubClassLoader(newcl)
                    newcl.addWeakClassLoader(toLinkKCL)

                    Log.debug("UnbreakLink "+du.getUnitName()+"->"+toLinkKCL.getLoadedURLs().get(0))

                }
                failedLinks.remove(buildKEY(du))
                Log.debug("Failed Link {} remain size : {}", du.getUnitName(), failedLinks.size())
            }
            for(rLib in du.getRequiredLibs()) {
                val kcl = getKCLInternals(rLib)
                if (kcl != null) {
                    newcl.addSubClassLoader(kcl)
                    kcl.addWeakClassLoader(newcl)

                    for(rLibIn in du.getRequiredLibs()){
                        if(rLibIn != rLib){
                            val kcl2 = getKCLInternals(rLibIn)
                            if (kcl2 != null) {
                                kcl.addWeakClassLoader(kcl2)
                            }
                        }
                    }
                } else {
                    Log.debug("Fail link ! Warning ")
                    var pendings = failedLinks.get(buildKEY(rLib))
                    if(pendings == null){
                        pendings = ArrayList<KevoreeJarClassLoader>()
                        failedLinks.put(buildKEY(rLib), pendings!!)
                    }
                    pendings!!.add(newcl)
                }
            }
            newcl
        }
        return res!!
    }

}
