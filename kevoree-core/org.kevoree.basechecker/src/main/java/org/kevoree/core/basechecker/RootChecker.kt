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
import org.kevoree.core.basechecker.dictionaryChecker.DictionaryNetworkPortChecker
import org.kevoree.core.basechecker.dictionaryChecker.DictionaryOptionalChecker
import org.kevoree.core.basechecker.namechecker.NameChecker
import org.kevoree.core.basechecker.abstractchecker.AbstractChecker
import org.kevoree.core.basechecker.portchecker.PortChecker
import org.kevoree.log.Log
import org.kevoree.modeling.api.util.ModelVisitor
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.api.service.core.checker.CheckerContext

class RootChecker : CheckerService {
    private val subcheckers = ArrayList<CheckerService>();

    {
        subcheckers.add(NameChecker())
        subcheckers.add(PortChecker())
        subcheckers.add(BindingChecker())
        subcheckers.add(DictionaryOptionalChecker())
        subcheckers.add(DictionaryNetworkPortChecker())

        subcheckers.add(AbstractChecker())

    }
    fun check(element: KMFContainer?): MutableList<CheckerViolation> {
        return check(element, CheckerContextImpl())
    }

    override fun check(element: KMFContainer?, context : CheckerContext?): MutableList<CheckerViolation> {
        val result = ArrayList<CheckerViolation>()
        val beginTime = System.currentTimeMillis()
        if (element != null) {
            element.visit(object : ModelVisitor() {
                override public fun visit(elem: org.kevoree.modeling.api.KMFContainer, refNameInParent: String, parent: org.kevoree.modeling.api.KMFContainer) {
                    for (checker in subcheckers) {
                        val violations = checker.check(elem, context)
                        if (violations != null && violations.size > 0) {
                            result.addAll(violations)
                        }
                    }
                }
            }, true, true, true)
        } else {
            val violation: CheckerViolation = CheckerViolation()
            violation.setMessage("Model which must be checked is null")
            violation.setTargetObjects(ArrayList())
            result.add(violation)
        }
        if (Log.DEBUG) {
            Log.debug("Model checked in {} ", (System.currentTimeMillis() - beginTime))
        }
        return result
    }

}
