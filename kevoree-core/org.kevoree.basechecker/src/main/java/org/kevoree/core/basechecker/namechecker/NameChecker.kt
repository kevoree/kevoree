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

package org.kevoree.core.basechecker.namechecker

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.kevoree.NamedElement
import org.kevoree.api.service.core.checker.CheckerService
import org.kevoree.api.service.core.checker.CheckerViolation
import org.kevoree.modeling.api.KMFContainer
import org.kevoree.Instance
import org.kevoree.api.service.core.checker.CheckerContext

class NameChecker: CheckerService {

    private val acceptedRegex = "[A-Za-z0-9_]*"
    private var message = "The name doesn't fit the defined format.\nA name only contains lower or upper letters, numbers and \"_\"."

    override fun check(element: KMFContainer?, context : CheckerContext?): MutableList<CheckerViolation> {
        var violations = ArrayList<CheckerViolation>()
        if (element != null && element is Instance) {
            val violation = check(element)
            if (violation != null) {
                violations.add(violation)
            }
        }
        // TODO Do we need to check port name, dictionary attribute name, ... ?
        return violations;
    }

    private fun check(name: String): Boolean {
        if (name.equals("")) {
            return false
        }
        val p: Pattern = Pattern.compile(acceptedRegex)
        val m: Matcher = p.matcher(name)
        return m.matches()
    }

    private fun check(obj: NamedElement): CheckerViolation? {
        if (check(obj.name!!) == false) {
            val violation = CheckerViolation()
            violation.setMessage(message)
            val targetObjects = ArrayList<String>()
            targetObjects.add(obj.path()!!)
            violation.setTargetObjects(targetObjects)
            return violation
        } else {
            return null
        }
    }
}
