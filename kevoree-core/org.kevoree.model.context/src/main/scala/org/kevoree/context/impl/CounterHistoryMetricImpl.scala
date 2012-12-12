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

import org.kevoree.context.{MetricValue, CounterHistoryMetric}
import java.util
import util.Collections

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 21:16
 */
class CounterHistoryMetricImpl() extends CounterHistoryMetric {

  private val ll = new util.LinkedList[MetricValue]

  override def addValues(v: MetricValue) {
    if (ll.size >= getNumber && !ll.isEmpty) {
      ll.removeFirst()
    }
    ll.addLast(v)
    v.setEContainer(this, Some(() => { this.removeValues(v) }))
    try {
      val sumE = java.lang.Double.parseDouble(v.getValue)
      setSum(getSum + sumE)
    } catch {
      case ignored: Throwable =>
    }
  }

  override def removeValues(values: MetricValue) {
    if (ll.size != 0 && ll.contains(values.getTimestamp)) {
      ll.remove(values.getTimestamp)
      values.setEContainer(null, None)
    }
  }

  override def getFirst = if (ll.isEmpty) {
    None
  } else {
    Some(ll.getFirst)
  }

  override def setFirst(first: Option[MetricValue]) {
    null
  }

  override def getLast = if (ll.isEmpty) {
    None
  } else {
    Some(ll.getLast)
  }

  override def setLast(last: Option[MetricValue]) {
    null
  }

  override def getValues = {
    import scala.collection.JavaConversions._
    Collections.unmodifiableList(ll).toList
  }

  override def getValuesForJ = {
    Collections.unmodifiableList(ll)
  }
}
