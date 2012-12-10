package org.kevoree.library.monitored;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.context.CounterHistoryMetric;
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
public class BasicProbes extends AbstractComponentType implements Runnable {

    private ScheduledExecutorService t = null;
    private MBeanServerConnection mbsc = null;
    private OperatingSystemMXBean osBean = null;
    private MemoryMXBean mBean = null;
    private MBeanServer mbserv = null;

    @Start
    public void start() throws IOException {
        mbserv = ManagementFactory.getPlatformMBeanServer();
        mbsc = ManagementFactory.getPlatformMBeanServer();
        osBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
        mBean = ManagementFactory.getMemoryMXBean();
        t = Executors.newScheduledThreadPool(1);
        t.scheduleAtFixedRate(this,0,10000, TimeUnit.MILLISECONDS);
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

        double cpuUsage = osBean.getSystemLoadAverage() / osBean.getAvailableProcessors();
        System.out.println("cpu.usage="+cpuUsage);

        org.kevoree.context.PutHelper.getMetric(getModelService().getContextModel(),"perf/cpu/{"+getNodeName()+"}", CounterHistoryMetric.class); 

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BasicProbes p = new BasicProbes();
        p.start();


        for(int i=0;i < 10000000000d;i++){
            Thread.sleep(1);
        }




    }

}
