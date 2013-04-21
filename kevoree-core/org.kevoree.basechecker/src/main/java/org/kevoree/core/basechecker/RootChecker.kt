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
package org.kevoree.core.basechecker

import java.util.ArrayList
import org.kevoree.ContainerRoot
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.core.basechecker.bindingchecker.BindingChecker
import org.kevoree.core.basechecker.channelchecker.BoundsChecker
import org.kevoree.core.basechecker.cyclechecker.ComponentCycleChecker
import org.kevoree.core.basechecker.dictionaryChecker.DictionaryNetworkPortChecker
import org.kevoree.core.basechecker.dictionaryChecker.DictionaryOptionalChecker
import org.kevoree.core.basechecker.namechecker.NameChecker
import org.kevoree.core.basechecker.nodechecker.NodeChecker
import org.kevoree.core.basechecker.nodechecker.NodeContainerChecker
import org.kevoree.core.basechecker.portchecker.PortChecker
import org.kevoree.log.Log

class RootChecker: CheckerService {


    private val subcheckers = ArrayList<CheckerService>();

    {
        /*subcheckers.add(KevoreeVersionChecker())*/
        subcheckers.add(ComponentCycleChecker())
        /*subcheckers.add(NodeCycleChecker())*/
        subcheckers.add(NameChecker())
        subcheckers.add(PortChecker())
        subcheckers.add(NodeChecker())
        subcheckers.add(BindingChecker())
        subcheckers.add(BoundsChecker())
        /*subcheckers.add(IdChecker())*/
        subcheckers.add(DictionaryOptionalChecker())
        subcheckers.add(NodeContainerChecker())
        subcheckers.add(DictionaryNetworkPortChecker())
    }
    override fun check (model: ContainerRoot?): MutableList<CheckerViolation> {
        val result = ArrayList<CheckerViolation>()
        val beginTime = System.currentTimeMillis()
        if (model != null) {
            for (checker in subcheckers) {
                try {
                    result.addAll(checker.check(model)!!)
                } catch (e: Exception) {
                    Log.error("Exception during checking", e)
                    val violation: CheckerViolation = CheckerViolation()
                    violation.setMessage("Checker fatal exception " + checker.getClass().getSimpleName() + "-" + e.getMessage())
                    violation.setTargetObjects(ArrayList())
                    result.add(violation)
                }

            }
        } else {
            val violation: CheckerViolation = CheckerViolation()
            violation.setMessage("Model which must be checked is null")
            violation.setTargetObjects(ArrayList())
            result.add(violation)
        }
        if(Log.DEBUG){
            Log.debug("Model checked in {} " , (System.currentTimeMillis() - beginTime).toString())
        }
        return result
    }

}
