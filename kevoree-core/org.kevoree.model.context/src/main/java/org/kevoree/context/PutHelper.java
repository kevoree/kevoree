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
package org.kevoree.context;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 17:03
 */
public class PutHelper {

    public static Metric getMetric(ContextRoot ctx, String path, Class clazzName) {
        String[] paths = path.split("/");
        if (paths.length != 3) {
            ContextModel mod = ctx.findContextByID(paths[0]);
            if (mod == null) {
                mod = ContextFactory.createContextModel();
                mod.setName(paths[0]);
                ctx.addContext(mod);
            }
            MetricType mt = mod.findTypesByID(paths[1]);
            if (mt == null) {
                mt = ContextFactory.createMetricType();
                mt.setName(paths[1]);
                mod.addTypes(mt);
            }
            Metric metric = mt.findMetricsByID(paths[2]);
            if (metric == null) {
                if (clazzName.equals(DurationHistoryMetric.class)) {
                    metric = ContextFactory.createDurationHistoryMetric();
                } else {
                    metric = ContextFactory.createCounterHistoryMetric();
                }
            }
            return metric;
        } else {
            return null;
        }
    }

    public static void main(String[] args){

    }


}
