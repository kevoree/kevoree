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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 17:03
 */
public class PutHelper {

       private static Logger logger = LoggerFactory.getLogger(PutHelper.class);

    public static class GetParams{
        public String unitParam = null;
        public Double duration = null;
        public String durationUnit = null;
        public Integer number = null;
        public String metricTypeClazzName = null;

        public GetParams setMetricTypeClazzName(String p){
            metricTypeClazzName = p;
            return this;
        }
        public GetParams setNumber(Integer p){
            number = p;
            return this;
        }
        public GetParams setdurationUnit(String p){
            durationUnit = p;
            return this;
        }
        public GetParams setDuration(Double p){
            duration = p;
            return this;
        }
        public GetParams setMetricTypeUnit(String p){
            unitParam = p;
            return this;
        }
    }

    public static GetParams getParam(){
        return new GetParams();
    }

    private static String cleanName(String name2){
        String name = name2.trim();
        if(name.startsWith("{") && name.endsWith("}") ){
          name = name.substring(1,name.length()-1);
        }
        return name;
    }

    public static Metric getMetric(ContextRoot ctx, String path, GetParams params) {
        String[] paths = path.split("/");
        if (paths.length == 3) {
            ContextModel mod = ctx.findContextByID(cleanName(paths[0]));
            if (mod == null) {
                mod = ContextFactory.createContextModel();
                mod.setName(cleanName(paths[0]));
                ctx.addContext(mod);
            }
            MetricType mt = mod.findTypesByID(cleanName(paths[1]));
            if (mt == null) {
                mt = ContextFactory.createMetricType();
                mt.setName(cleanName(paths[1]));
                mod.addTypes(mt);
            }
            Metric metric = mt.findMetricsByID(cleanName(paths[2]));
            if (metric == null) {
                if (DurationHistoryMetric.class.getName().equals(params.metricTypeClazzName)) {
                    metric = ContextFactory.createDurationHistoryMetric();
                    if(params.duration != null){
                        ((DurationHistoryMetric)metric).setDuration(params.duration);
                    }
                    if(params.durationUnit != null){
                        ((DurationHistoryMetric)metric).setDurationUnit(params.durationUnit);
                    }
                } else {
                    metric = ContextFactory.createCounterHistoryMetric();
                    if(params.number != null){
                        ((CounterHistoryMetric)metric).setNumber(params.number);
                    }
                }
                metric.setName(cleanName(paths[2]));
                mt.addMetrics(metric);
            }
            return metric;
        } else {
            logger.error("Can't parse parameter path "+paths.length+"- path length "+paths+" must be 3");
            return null;
        }
    }

    public static void addValue(Metric m, String value){
        MetricValue valueMod = ContextFactory.createMetricValue();
        valueMod.setValue(value);
        valueMod.setTimestamp(System.nanoTime()+"");
        m.addValues(valueMod);
    }


}
