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

package org.kevoree.tools.modelsync

import org.kevoree.framework.AbstractGroupType
import org.kevoree.ContainerRoot

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 09:20
 */

class ModelSyncBean {

  var bootstraper = new FakeBootstraperService

  @throws(classOf[Exception])
  def pushTo (model: ContainerRoot, destNodeName: String, viaGroupName: String) {

    bootstraper.getBootstrap.clear
    bootstraper.getBootstrap.bootstrapGroupType(model, viaGroupName, ModelHandlerServiceNoKernel(model)) match {
      case groupTypeInstance : AbstractGroupType => {
        groupTypeInstance.push(model, destNodeName)
      }
      case null => {
        org.kevoree.log.Log.error("Error while bootstraping group type ")
        throw new Exception("Error while bootstraping group type")
      }
    }
  }

  @throws(classOf[Exception])
  def pullTo (model: ContainerRoot, destNodeName: String, viaGroupName: String) : ContainerRoot = {
    bootstraper.getBootstrap.clear
    bootstraper.getBootstrap.bootstrapGroupType(model, viaGroupName, ModelHandlerServiceNoKernel(model)) match {
      case groupTypeInstance:AbstractGroupType => {
        groupTypeInstance.pull(destNodeName)
      }
      case null => {
        org.kevoree.log.Log.error("Error while bootstraping group type")
        throw new Exception("Error while bootstraping group type")
      }
    }
  }

  def clear () {
    bootstraper.getBootstrap.clear
  }

}
