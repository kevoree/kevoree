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

import org.kevoree.context.impl.DefaultContextFactory;
import java.util.ArrayList;
import java.util.List;
import static org.kevoree.log.Log.*;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 17:03
 */
public class PutHelper {

    public static class GetParams {
        public String unitParam = null;
        public Double duration = null;
        public String durationUnit = null;
        public Integer number = null;
        public String metricTypeClazzName = null;

        public GetParams setMetricTypeClazzName(String p) {
            metricTypeClazzName = p;
            return this;
        }

        public GetParams setNumber(Integer p) {
            number = p;
            return this;
        }

        public GetParams setdurationUnit(String p) {
            durationUnit = p;
            return this;
        }

        public GetParams setDuration(Double p) {
            duration = p;
            return this;
        }

        public GetParams setMetricTypeUnit(String p) {
            unitParam = p;
            return this;
        }
    }

    public static GetParams getParam() {
        return new GetParams();
    }

    private static String cleanName(String name2) {
        String name = name2.trim();
        if (name.startsWith("{") && name.endsWith("}")) {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }

    public static Metric getMetric(ContextRoot ctx, String path, GetParams params) {

        List<String> paths = new ArrayList<String>();
        String pathR = path;
        while (pathR.indexOf('/') != -1) {
            if (pathR.indexOf("{") == 0) {
                paths.add(pathR.substring(1, pathR.indexOf("}")));
                pathR = pathR.substring(pathR.indexOf("}") + 1);
                pathR = pathR.substring(pathR.indexOf("/") + 1);
            } else {
                paths.add(pathR.substring(0, pathR.indexOf('/')));
                pathR = pathR.substring(pathR.indexOf('/') + 1);
            }
        }
        if (!pathR.equals("")) {
            if (pathR.indexOf("{") == 0) {
                paths.add(pathR.substring(1, pathR.indexOf("}")));
            } else {
                paths.add(pathR);
            }
        }


        //String[] paths = path.split("/");
        if (paths.size() == 3) {
            ContextFactory factory = new DefaultContextFactory();
            ContextModel mod = ctx.findContextByID(cleanName(paths.get(0)));
            if (mod == null) {
                mod = factory.createContextModel();
                mod.setName(cleanName(paths.get(0)));
                ctx.addContext(mod);
            }
            MetricType mt = mod.findTypesByID(cleanName(paths.get(1)));
            if (mt == null) {
                mt = factory.createMetricType();
                mt.setName(cleanName(paths.get(1)));
                mod.addTypes(mt);
            }
            Metric metric = mt.findMetricsByID(cleanName(paths.get(2)));
            if (metric == null) {
                if (DurationHistoryMetric.class.getName().equals(params.metricTypeClazzName)) {
                    metric = factory.createDurationHistoryMetric();
                    if (params.duration != null) {
                        ((DurationHistoryMetric) metric).setDuration(params.duration);
                    }
                    if (params.durationUnit != null) {
                        ((DurationHistoryMetric) metric).setDurationUnit(params.durationUnit);
                    }
                } else {
                    metric = factory.createCounterHistoryMetric();
                    if (params.number != null) {
                        ((CounterHistoryMetric) metric).setNumber(params.number);
                    }
                }
                metric.setName(cleanName(paths.get(2)));
                mt.addMetrics(metric);
            }
            return metric;
        } else {
            error("Can't parse parameter path {} - path length {} must be 3 ",paths.size()+"", paths.toString());
            return null;
        }
    }

    public static void addValue(Metric m, String value) {
        ContextFactory factory = new DefaultContextFactory();
        MetricValue valueMod = factory.createMetricValue();
        valueMod.setValue(value);
        valueMod.setTimestamp(System.nanoTime() + "");
        m.addValues(valueMod);
    }

    public static void addValue(ContextRoot ctx, String path, String value) {
        Metric m = (Metric) ctx.findByPath(path);
        ContextFactory factory = new DefaultContextFactory();
        MetricValue valueMod = factory.createMetricValue();
        valueMod.setValue(value);
        valueMod.setTimestamp(System.nanoTime() + "");
        m.addValues(valueMod);

    }


}
