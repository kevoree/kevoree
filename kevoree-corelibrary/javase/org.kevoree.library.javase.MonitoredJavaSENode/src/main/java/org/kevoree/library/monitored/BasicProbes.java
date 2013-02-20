package org.kevoree.library.monitored;

import org.kevoree.annotation.*;
import org.kevoree.context.CounterHistoryMetric;
import org.kevoree.context.Metric;
import org.kevoree.context.PutHelper;
import org.kevoree.framework.AbstractComponentType;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/12/12
 * Time: 12:14
 */
@Library(name = "JavaSE")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "period", defaultValue = "5000", optional = true)
})
public class BasicProbes extends AbstractComponentType implements Runnable {

    private ScheduledExecutorService t = null;
    private MBeanServerConnection mbsc = null;
    private OperatingSystemMXBean osBean = null;
    private MemoryMXBean mBean = null;
    private MBeanServer mbserv = null;
    private Metric cpuMetric = null;

    @Start
    public void start() throws IOException {
        mbserv = ManagementFactory.getPlatformMBeanServer();
        mbsc = ManagementFactory.getPlatformMBeanServer();
        osBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
        mBean = ManagementFactory.getMemoryMXBean();
        t = Executors.newScheduledThreadPool(1);
        cpuMetric = PutHelper.getMetric(getModelService().getContextModel(), "perf/cpu/{nodes[" + getNodeName() + "]}", PutHelper.getParam().setMetricTypeClazzName(CounterHistoryMetric.class.getName()).setNumber(100));
        t.scheduleAtFixedRate(this,0,Integer.parseInt(getDictionary().get("period").toString()), TimeUnit.MILLISECONDS);
    }

    @Stop
    public void stop() {
        t.shutdownNow();
        t = null;
        mBean = null;
        mbsc = null;
        osBean = null;
        mbserv = null;
    }

    @Override
    public void run() {
        try {
            double cpuUsage = osBean.getSystemLoadAverage() / osBean.getAvailableProcessors();
            PutHelper.addValue(cpuMetric,cpuUsage+"");
            Metric m = (Metric) getModelService().getContextModel().findByPath("perf/cpu/{nodes[" + getNodeName() + "]}");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BasicProbes p = new BasicProbes();
        p.start();
    }

}
