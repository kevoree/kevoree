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
package org.kevoree.core.basechecker

import bindingchecker.BindingChecker
import channelchecker.BoundsChecker
import cyclechecker.{ComponentCycleChecker, NodeCycleChecker}
import dictionaryChecker.DictionaryOptionalChecker
import namechecker.{IdChecker, NameChecker}
import nodechecker.NodeChecker
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import java.util.ArrayList
import portchecker.PortChecker
import org.slf4j.LoggerFactory


class RootChecker extends CheckerService {

  private var logger = LoggerFactory.getLogger(this.getClass)
  
  var subcheckers: List[CheckerService] = List(new ComponentCycleChecker, new NodeCycleChecker, new NameChecker,
                                                new PortChecker, new NodeChecker, new BindingChecker, new BoundsChecker, new IdChecker, new DictionaryOptionalChecker)

  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    val result: java.util.List[CheckerViolation] = new ArrayList()
    val beginTime = System.currentTimeMillis()
    subcheckers.foreach({
      sub =>
        result.addAll(sub.check(model))
    })
    logger.debug("Model checked in "+(System.currentTimeMillis()-beginTime))
    result
  }

}