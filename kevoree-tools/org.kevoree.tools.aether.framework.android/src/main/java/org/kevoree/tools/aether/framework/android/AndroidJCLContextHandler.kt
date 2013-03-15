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
package org.kevoree.tools.aether.framework.android

import org.kevoree.DeployUnit
import java.io.File
import org.kevoree.kcl.KevoreeJarClassLoader
import org.slf4j.LoggerFactory
import org.kevoree.tools.aether.framework.JCLContextHandler
import java.util.ArrayList

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:50
 */

class AndroidJCLContextHandler(val ctx: android.content.Context, val parent: ClassLoader): JCLContextHandler() {


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
            resolvedFile = org.kevoree.tools.aether.framework.android.AetherUtil.resolveDeployUnit(du)
        }

        if (resolvedFile != null) {
            return installDeployUnitInternals(du, resolvedFile!!)
        } else {
            logger.error("Error while resolving deploy unit " + du.getUnitName())
            return null
        }
    }

    override fun installDeployUnitInternals(du: DeployUnit, file: File): KevoreeJarClassLoader {
        val previousKCL = getKCLInternals(du)
        val res = if (previousKCL != null) {
            logger.debug("Take already installed {}", buildKEY(du))
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

                    logger.debug("UnbreakLink "+du.getUnitName()+"->"+toLinkKCL.getLoadedURLs().get(0))

                }
                failedLinks.remove(buildKEY(du))
                logger.debug("Failed Link {} remain size : {}", du.getUnitName(), failedLinks.size())
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
                    logger.debug("Fail link ! Warning ")
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
