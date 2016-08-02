package org.kevoree.bootstrap.telemetry;

/*
import com.eclipsesource.json.JsonObject;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.kevoree.core.TelemetryEventImpl;
import org.kevoree.log.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
*/
/**
 * Created by gregory.nain on 04/09/2014.
 */
public class SigarClient {
    public void init() {}
    public void stop() {}

    private String nodeName;
    private MQTTDispatcher dispatcher;
    public SigarClient(MQTTDispatcher dispatcher, String nodeName) {
        this.dispatcher = dispatcher;
        this.nodeName = nodeName;
    }
/*
    private Sigar sigar;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();



    public void init() {
        sigar = new Sigar();
        registerMemoryPoller();
        registerCPUPoller();
    }


    public void stop() {
        executor.shutdownNow();
    }


    private void registerMemoryPoller() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                JsonObject osData = new JsonObject();
                try {
                    Mem mem = sigar.getMem();
                    osData.add("actualFree", mem.getActualFree());
                    osData.add("actualUsed", mem.getActualUsed());
                    osData.add("free", mem.getFree());
                    osData.add("freePercent", mem.getFreePercent());
                    osData.add("ram", mem.getRam());
                    osData.add("total", mem.getTotal());
                    osData.add("used", mem.getUsed());
                    osData.add("usedPercent", mem.getUsedPercent());
                } catch (SigarException e) {
                    Log.error("Error while monitoring memory in Sigar", e);
                }
                dispatcher.notify(TelemetryEventImpl.build(nodeName, "SigarMemInfo", osData.toString(), ""), "nodes/" + nodeName + "/infra/memory");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void registerCPUPoller() {
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                JsonObject osData = new JsonObject();
                try {
                    Cpu cpu = sigar.getCpu();
                    osData.add("total", cpu.getTotal());
                    osData.add("idle", cpu.getIdle());
                    osData.add("irq", cpu.getIrq());
                    osData.add("nice", cpu.getNice());
                    osData.add("softIrq", cpu.getSoftIrq());
                    osData.add("stolen", cpu.getStolen());
                    osData.add("sys", cpu.getSys());
                    osData.add("user", cpu.getUser());
                    osData.add("wait", cpu.getWait());
                } catch (SigarException e) {
                    Log.error("Error while monitoring CPU in Sigar", e);
                }
                dispatcher.notify(TelemetryEventImpl.build(nodeName, "SigarCpuInfo", osData.toString(), ""), "nodes/" + nodeName + "/infra/cpu");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
*/
}
