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

package org.kevoree.framework

import org.kevoree.ContainerRoot
import org.kevoree.framework.internal.MethodAnnotationResolver
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService

public class KevoreeGroup(val target: AbstractGroupType, val nodeName: String, val name: String,val modelService : KevoreeModelHandlerService): KInstance {

    val kevoree_internal_logger = LoggerFactory.getLogger(this.javaClass)!!
    var isStarted: Boolean = false
    private val resolver = MethodAnnotationResolver(target.javaClass);

    override fun kInstanceStart(tmodel: ContainerRoot): Boolean {
        if (!isStarted){
            try {

                target.setModelService(ModelHandlerServiceProxy(modelService))
                target.setName(name)
                target.setNodeName(nodeName)

                target.getModelService()!!.registerModelListener(target)
                (target.getModelService() as ModelHandlerServiceProxy).setTempModel(tmodel)
                val met = resolver.resolve(javaClass<org.kevoree.annotation.Start>())
                met?.invoke(target)
                (target.getModelService() as ModelHandlerServiceProxy).unsetTempModel()
                isStarted = true
                return true
            } catch(e: Exception) {
                kevoree_internal_logger.error("Kevoree Group Instance Start Error !", e)
                return false
            }
        } else {
            return false
        }
    }

    override fun kInstanceStop(tmodel: ContainerRoot): Boolean {
        if (isStarted){
            try {
                target.getModelService()!!.unregisterModelListener(target)
                (target.getModelService() as ModelHandlerServiceProxy).setTempModel(tmodel)
                val met = resolver.resolve(javaClass<org.kevoree.annotation.Stop>())
                met?.invoke(target)
                (target.getModelService() as ModelHandlerServiceProxy).unsetTempModel()
                isStarted = false
                return true
            } catch (e: Exception){
                kevoree_internal_logger.error("Kevoree Group Instance Stop Error !", e)
                return false
            }
        } else {
            return false
        }
    }

    public override fun kUpdateDictionary(d: Map<String, Any>, cmodel: ContainerRoot): Map<String, Any>? {
        try {
            val previousDictionary = target.getDictionary()!!.clone()
            for(v in d.keySet()) {
                target.getDictionary()!!.put(v, d.get(v))
            }
            if (isStarted) {
                (target.getModelService() as ModelHandlerServiceProxy).setTempModel(cmodel)
                val met = resolver.resolve(javaClass<org.kevoree.annotation.Update>())
                met?.invoke(target)
                (target.getModelService() as ModelHandlerServiceProxy).unsetTempModel()
            }
            return previousDictionary as Map<String, Any>?
        } catch(e: Exception) {
            kevoree_internal_logger.error("Kevoree Group Instance Update Error !", e)
            return null
        }
    }

}