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
import java.util.Collections
import org.kevoree.context.container.KMFContainer
import org.kevoree.context.container.RemoveFromContainerCommand

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 21:16
 */
class CounterHistoryMetricImpl: CounterHistoryMetric, CounterHistoryMetricInternal {
    override var internal_unsetCmd: RemoveFromContainerCommand? = null
    override var internal_eContainer: KMFContainer? = null
    override var internal_containmentRefName: String? = null
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

        //IMPORTED FROM SUPER :: Kotlin compiler workaround
        if(isReadOnly()){throw Exception("This model is ReadOnly. Elements are not modifiable.")}
        _values_java_cache=null
   //     (v as org.kevoree.context.container.KMFContainerImpl).setEContainer(this,{()->this.removeValues(v)},"values")
        _values.put(v.getTimestamp(),v)
        //END IMPORTED FROM SUPER :: Kotlin compiler workaround

        if (ll.size >= getNumber() && !ll.isEmpty()) {
            _values.remove(ll.getFirst())
            ll.removeFirst()
        }
        ll.addLast(v)
        try {
            val sumE = java.lang.Double.parseDouble(v.getValue())
            setSum(getSum() + sumE)
        } catch (e: Exception) {
        }
    }


    override fun removeValues(value: MetricValue) {
        if (ll.size != 0 && _values.get(value.getTimestamp()) != null) {
            ll.remove(value)
        }

        //IMPORTED FROM SUPER :: Kotlin compiler workaround
        if(isReadOnly()){throw Exception("This model is ReadOnly. Elements are not modifiable.")}
        _values_java_cache=null
        if(_values.size() != 0 && _values.containsKey(value.getTimestamp())) {
            _values.remove(value.getTimestamp())
            (value!! as org.kevoree.context.container.KMFContainerImpl).setEContainer(null,null,null)
        }
        //END IMPORTED FROM SUPER :: Kotlin compiler workaround

    }

    override fun getFirst(): MetricValue? {
        if (ll.isEmpty()) {
            return null
        } else {
            return ll.getFirst()
        }
    }

    override fun setFirst(first: MetricValue?) {
        throw Exception("First attribute is computed on add method")
    }

    override fun getLast(): MetricValue? {
        if (ll.isEmpty()) {
            return null
        } else {
            return ll.getLast()
        }
    }

    override fun setLast(last: MetricValue?) {
        throw Exception("Last attribute is computed on add method")
    }

    override fun getValues(): MutableList<MetricValue> {
        return Collections.unmodifiableList(ll)
    }

}
