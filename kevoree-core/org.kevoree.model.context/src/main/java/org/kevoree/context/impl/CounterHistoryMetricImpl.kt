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
package org.kevoree.context.impl

import org.kevoree.context.MetricValue
import org.kevoree.context.CounterHistoryMetric
import java.util
import org.kevoree.context.ContextContainer
import java.util.Collections

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 21:16
 */
class CounterHistoryMetricImpl: CounterHistoryMetric, CounterHistoryMetricInternal {

    override var internal_eContainer: ContextContainer? = null
    override var internal_unsetCmd: (()->Unit)? = null
    override var internal_readOnlyElem: Boolean = false
    override var internal_recursive_readOnlyElem = false

    override public var _name: String = ""
    override public var _query: String = ""
    override public var _syncConstraints: String = ""
    override public var _sum: Double = 0.0
    override public var _number: Int = 0
    override var _values_java_cache: List<org.kevoree.context.MetricValue>? = null
    override val _values: java.util.HashMap<Any, org.kevoree.context.MetricValue> = java.util.HashMap<Any, org.kevoree.context.MetricValue>()
    override var _min: org.kevoree.context.MetricValue? = null
    override var _max: org.kevoree.context.MetricValue? = null
    override var _first: org.kevoree.context.MetricValue? = null
    override var _last: org.kevoree.context.MetricValue? = null


    private val ll = java.util.LinkedList<MetricValue>()

    override fun addValues(v: MetricValue) {
        if (ll.size >= getNumber() && !ll.isEmpty()) {
            ll.removeFirst()
        }
        ll.addLast(v)
        (v as MetricValueInternal).setEContainer(this, {() -> { this.removeValues(v) } })
        try {
            val sumE = java.lang.Double.parseDouble(v.getValue())
            setSum(getSum() + sumE)
        } catch (e: Exception) {
        }
    }

    override fun removeValues(values: MetricValue) {
        if (ll.size != 0 && ll.contains(values.getTimestamp())) {
            ll.remove(values.getTimestamp())
            (values as MetricValueInternal).setEContainer(null, null)
        }
    }

    override fun getFirst(): MetricValue? {
        if (ll.isEmpty()) {
            return null
        } else {
            return ll.getFirst()
        }
    }

    override fun setFirst(first: MetricValue?) {
        null
    }

    override fun getLast(): MetricValue? {
        println("Last !!")

        if (ll.isEmpty()) {
            return null
        } else {
            return ll.getLast()
        }
    }

    override fun setLast(last: MetricValue?) {
        null
    }

    override fun getValues(): List<MetricValue> {
        return Collections.unmodifiableList(ll)
    }

}
