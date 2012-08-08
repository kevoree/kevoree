package org.kevoree.library.defaultNodeTypes.jcl.deploy.context

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.slf4j.LoggerFactory
import org.kevoree.DeployUnit
import org.kevoree.framework.AbstractNodeType
import java.util.concurrent.{Callable, ThreadFactory, Executors}

object KevoreeDeployManager {

  val logger = LoggerFactory.getLogger(this.getClass)
  private var private_bundleMapping: List[KevoreeMapping] = List[KevoreeMapping]()

  private var pool : java.util.concurrent.ExecutorService = null

  def startPool() {
    if(pool != null){
       logger.error("Error JavaSE Node can't run as duplicated instance on the same JVM")
       return
    }
    pool = Executors.newSingleThreadExecutor(new ThreadFactory() {
      val s = System.getSecurityManager
      val group = if (s != null) {
        s.getThreadGroup
      } else {
        Thread.currentThread().getThreadGroup
      }

      def newThread(p1: Runnable) = {
        val t = new Thread(group, p1, "Kevoree_JavaSENode_DeployManager_" + hashCode())
        if (t.isDaemon) {
          t.setDaemon(false)
        }
        if (t.getPriority != Thread.NORM_PRIORITY) {
          t.setPriority(Thread.NORM_PRIORITY)
        }
        t
      }
    })
  }

  def stopPool(){
    pool.shutdownNow()
    pool=null
  }


  def clearAll(nodeType: AbstractNodeType) {
    KevoreeDeployManager.bundleMapping.filter(bm => bm.ref.isInstanceOf[DeployUnit]).foreach(mapping => {
      val old_du = mapping.ref.asInstanceOf[DeployUnit]
      //CLEANUP KCL CONTEXT
      if (nodeType.getBootStrapperService.getKevoreeClassLoaderHandler.getKevoreeClassLoader(old_du) != null) {
        logger.debug("Force cleanup unitName {}", old_du.getUnitName)
      }
    })
    private_bundleMapping = List[KevoreeMapping]()
    logger.debug("Deploy manager cache size after HaraKiri" + KevoreeDeployManager.bundleMapping.size)
  }


  def bundleMapping: List[KevoreeMapping] = {
    pool.submit(GET_MAPPINGS()).get()
  }

  case class GET_MAPPINGS() extends Callable[List[KevoreeMapping]] {
    def call() = {
      private_bundleMapping
    }
  }

  def addMapping(newMap: KevoreeMapping) {
    pool.submit(ADD_MAPPING(newMap)).get()
  }

  case class ADD_MAPPING(newMap: KevoreeMapping) extends Runnable {
    def run() {
      private_bundleMapping = private_bundleMapping ++ List(newMap)
    }
  }

  def removeMapping(newMap: KevoreeMapping) {
    pool.submit(REMOVE_MAPPING(newMap)).get()
  }

  case class REMOVE_MAPPING(oldMap: KevoreeMapping) extends Runnable {
    def run() {
      private_bundleMapping = private_bundleMapping.filter(p => p != oldMap)
    }
  }

}


