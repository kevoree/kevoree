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
                if (clazzName.equals(DurationHistoryMetric.class.getName())) {
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

}
