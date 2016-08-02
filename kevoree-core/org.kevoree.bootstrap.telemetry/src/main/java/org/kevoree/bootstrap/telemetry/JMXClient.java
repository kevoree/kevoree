package org.kevoree.bootstrap.telemetry;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.core.TelemetryEventImpl;

import java.lang.management.*;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gregory.nain on 03/09/2014.
 */
public class JMXClient {

    private MQTTDispatcher dispatcher;
    private String nodeName;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public JMXClient(MQTTDispatcher dispatcher, String nodeName) {
        this.dispatcher = dispatcher;
        this.nodeName = nodeName;
    }

    public void init() {
        registerOsPoller();
        registerRuntimePoller();
        registerMemoryPoller();
        registerThreadsPoller();
    }

    public void close() {
        executor.shutdownNow();
    }


    public void registerOsPoller() {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
                JsonObject osData = new JsonObject();
                osData.add("name", bean.getName());
                osData.add("architecture", bean.getArch());
                osData.add("nbProcessorsAvailable", bean.getAvailableProcessors());
                osData.add("loadAverage", bean.getSystemLoadAverage());
                osData.add("version", bean.getVersion());
                dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.SYSTEM_INFO, osData.toString(), ""), "nodes/" + nodeName + "/infra/os");
            }
        }, 1, TimeUnit.SECONDS);
    }


    public void registerRuntimePoller() {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
                JsonObject osData = new JsonObject();
                osData.add("name", bean.getName());
                osData.add("bootClasspath", bean.getBootClassPath());
                osData.add("classpath", bean.getClassPath());
                osData.add("libpath", bean.getLibraryPath());
                osData.add("mgmtSpecVersion", bean.getManagementSpecVersion());
                osData.add("startTime", bean.getStartTime());
                osData.add("upTime", bean.getUptime());

                JsonObject vm = new JsonObject();
                vm.add("vendor",bean.getVmVendor());
                vm.add("version",bean.getVmVersion());
                vm.add("name",bean.getVmName());
                osData.add("vm",vm);

                JsonObject spec = new JsonObject();
                spec.add("vendor",bean.getSpecVendor());
                spec.add("version",bean.getSpecVersion());
                spec.add("name",bean.getSpecName());
                osData.add("spec",spec);

                JsonArray inputArgs = new JsonArray();
                for (String arg : bean.getInputArguments()) {
                    inputArgs.add(arg);
                }
                osData.add("inputArguments", inputArgs);

                JsonObject systemProperties = new JsonObject();
                for (Map.Entry<String, String> e : bean.getSystemProperties().entrySet()) {
                    systemProperties.add(e.getKey(), e.getValue());
                }
                osData.add("systemProperties", systemProperties);


                dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.SYSTEM_INFO, osData.toString(), ""), "nodes/" + nodeName + "/runtime/properties");
            }
        }, 1, TimeUnit.SECONDS);
    }

    public void registerMemoryPoller() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
                JsonObject osData = new JsonObject();
                osData.add("pendingFinalization",bean.getObjectPendingFinalizationCount());

                JsonObject heapMemory = new JsonObject();
                heapMemory.add("init",bean.getHeapMemoryUsage().getInit());
                heapMemory.add("committed",bean.getHeapMemoryUsage().getCommitted());
                heapMemory.add("max",bean.getHeapMemoryUsage().getMax());
                heapMemory.add("used",bean.getHeapMemoryUsage().getUsed());
                osData.add("heapMemory",heapMemory);

                JsonObject offHeapMemory = new JsonObject();
                offHeapMemory.add("init",bean.getNonHeapMemoryUsage().getInit());
                offHeapMemory.add("committed",bean.getNonHeapMemoryUsage().getCommitted());
                offHeapMemory.add("max",bean.getNonHeapMemoryUsage().getMax());
                offHeapMemory.add("used",bean.getNonHeapMemoryUsage().getUsed());
                osData.add("offHeapMemory",offHeapMemory);

                dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.SYSTEM_INFO, osData.toString(), ""), "nodes/" + nodeName + "/runtime/memory");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }


    public void registerThreadsPoller() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                JsonObject osData = new JsonObject();
                JsonObject currentThread = new JsonObject();
                currentThread.add("cpuTime",bean.getCurrentThreadCpuTime());
                currentThread.add("userTime",bean.getCurrentThreadUserTime());
                osData.add("currentThread",currentThread);

                osData.add("daemonThreadCount",bean.getDaemonThreadCount());
                osData.add("peakThreadCount",bean.getPeakThreadCount());
                osData.add("threadCount",bean.getThreadCount());
                osData.add("totalStartedThreadCount",bean.getTotalStartedThreadCount());

                JsonArray allThreadsInfo = new JsonArray();
                for(ThreadInfo info : bean.getThreadInfo(bean.getAllThreadIds())) {
                    JsonObject thisThread = new JsonObject();
                    thisThread.add("blockedCount", info.getBlockedCount());
                    thisThread.add("blockedTime", info.getBlockedTime());
                    thisThread.add("waitedCount", info.getWaitedCount());
                    thisThread.add("waitedTime", info.getWaitedTime());
                    thisThread.add("threadId", info.getThreadId());
                    thisThread.add("threadName", info.getThreadName());
                    thisThread.add("threadState", info.getThreadState().name());

                    JsonObject lock = new JsonObject();
                    lock.add("name", info.getLockName());
                    lock.add("ownerId", info.getLockOwnerId());
                    lock.add("ownerName", info.getLockOwnerName());
                    thisThread.add("lock", lock);

                    allThreadsInfo.add(thisThread);
                }
                osData.add("threads", allThreadsInfo);


                JsonArray deadlockedThreadsInfo = new JsonArray();
                for(ThreadInfo info : bean.getThreadInfo(bean.findDeadlockedThreads())) {
                    JsonObject thisThread = new JsonObject();
                    thisThread.add("blockedCount", info.getBlockedCount());
                    thisThread.add("blockedTime", info.getBlockedTime());
                    thisThread.add("waitedCount", info.getWaitedCount());
                    thisThread.add("waitedTime", info.getWaitedTime());
                    thisThread.add("threadId", info.getThreadId());
                    thisThread.add("threadName", info.getThreadName());
                    thisThread.add("threadState", info.getThreadState().name());

                    JsonObject lock = new JsonObject();
                    lock.add("name", info.getLockName());
                    lock.add("ownerId", info.getLockOwnerId());
                    lock.add("ownerName", info.getLockOwnerName());
                    thisThread.add("lock", lock);

                    deadlockedThreadsInfo.add(thisThread);
                }
                osData.add("deadlockedThreads", deadlockedThreadsInfo);

                dispatcher.notify(TelemetryEventImpl.build(nodeName, TelemetryEvent.Type.SYSTEM_INFO, osData.toString(), ""), "nodes/" + nodeName + "/runtime/threads");
            }
        }, 2, 5, TimeUnit.SECONDS);
    }

}
