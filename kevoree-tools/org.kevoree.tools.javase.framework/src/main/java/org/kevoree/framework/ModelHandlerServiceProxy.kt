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
package org.kevoree.framework

import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.handler.*
import org.kevoree.context.ContextRoot
import org.kevoree.log.Log

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 02/01/12
 * Time: 16:14
 */

class ModelHandlerServiceProxy(val proxy: KevoreeModelHandlerService): KevoreeModelHandlerService {
    public override fun updateModel(model: ContainerRoot?, callback: ((ModelUpdateCallBackReturn?) -> Unit)?) {
        throw UnsupportedOperationException()
    }
    public override fun compareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?, callback: ((ModelUpdateCallBackReturn?) -> Unit)?) {
        throw UnsupportedOperationException()
    }
    var proxyModel: AtomicReference<ContainerRoot> = AtomicReference<ContainerRoot>()

    fun setTempModel(tempModel: ContainerRoot) {
        proxyModel.set(tempModel)
    }

    fun unsetTempModel() {
        proxyModel.set(null)
    }

    override fun getLastModel(): ContainerRoot? {
        val model = proxyModel.get();
        if (model != null) {
            return model
        } else {
            return proxy.getLastModel()
        }
    }

    override fun getLastUUIDModel(): UUIDModel? {
        val model = proxyModel.get();
        if (model != null) {
            return object : UUIDModel {
                override fun getUUID(): UUID {
                    return UUID.randomUUID()
                }
                override fun getModel(): ContainerRoot? {
                    return model
                }
            }
        } else {
            return proxy.getLastUUIDModel()
        }
    }

    override fun getLastModification(): Date? {
        return if (proxyModel.get() != null) {
            Log.error("Last Modification not available during update")
            null
        } else {
            proxy.getLastModification()
        }
    }

    override fun updateModel(model: ContainerRoot?) {
        proxy.updateModel(model)
    }

    override fun atomicUpdateModel(model: ContainerRoot?): Date? {
        return if (proxyModel.get() != null) {
            Log.error("atomicUpdateModel not available during update")
            null
        } else {
            proxy.atomicUpdateModel(model)
        }
    }

    override fun compareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?) {
        if (proxyModel.get() != null) {
            Log.error("compareAndSwapModel not available during update")
        } else {
            proxy.compareAndSwapModel(previousModel, targetModel)
        }
    }

    override fun atomicCompareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?): Date? {
        return if (proxyModel.get() != null) {
            Log.error("atomicUpdateModel not available during update")
            null
        } else {
            proxy.atomicCompareAndSwapModel(previousModel, targetModel)
        }
    }

    override fun getPreviousModel(): MutableList<ContainerRoot>? {
        return if (proxyModel.get() != null) {
            Log.error("getPreviousModel not available during update")
            null
        } else {
            proxy.getPreviousModel()
        }
    }

    override fun getNodeName(): String? {
        return proxy.getNodeName()
    }

    override fun registerModelListener(listener: ModelListener?) {
        proxy.registerModelListener(listener)
    }

    override fun unregisterModelListener(listener: ModelListener?) {
        proxy.unregisterModelListener(listener)
    }

    override fun getContextModel(): ContextRoot? {
        return proxy.getContextModel()
    }

    override fun acquireLock(callBack: ModelHandlerLockCallBack?, timeout: Long?) {
        proxy.acquireLock(callBack, timeout)
    }

    override fun releaseLock(uuid: UUID?) {
        proxy.releaseLock(uuid)
    }

    override fun checkModel(targetModel: ContainerRoot?): Boolean {
        return proxy.checkModel(targetModel)
    }

    override fun updateModel(model: ContainerRoot?, callback: ModelUpdateCallback?) {
        proxy.updateModel(model, callback)
    }

    override fun compareAndSwapModel(previousModel: UUIDModel?, targetModel: ContainerRoot?, callback: ModelUpdateCallback?) {
        if (proxyModel.get() != null) {
            Log.error("compareAndSwapModel not available during update")
        } else {
            proxy.compareAndSwapModel(previousModel, targetModel, callback)
        }
    }

}