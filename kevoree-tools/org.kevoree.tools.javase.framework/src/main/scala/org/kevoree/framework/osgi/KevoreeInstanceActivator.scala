package org.kevoree.framework.osgi

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.api.service.core.script.KevScriptEngineFactory

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
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/12/11
 * Time: 10:24
 */

trait KevoreeInstanceActivator {

  def setNodeName(n : String)

  def setInstanceName(in : String)

  var modelHandlerService : KevoreeModelHandlerService = _
  def setModelHandlerService(mhandler : KevoreeModelHandlerService){
    modelHandlerService = mhandler
  }
  var kevScriptEngine : KevScriptEngineFactory = _
  def setKevScriptEngineFactory(kef : KevScriptEngineFactory){
    kevScriptEngine = kef
  }
/*
  var bundleContext : BundleContext = _
  def setBundleContext(bc : BundleContext){
    bundleContext = bc
  }*/

  def start()
  def stop()

}