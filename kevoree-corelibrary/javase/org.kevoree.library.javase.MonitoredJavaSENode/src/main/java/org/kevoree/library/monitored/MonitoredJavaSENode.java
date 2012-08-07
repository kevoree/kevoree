package org.kevoree.library.monitored;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.ConsoleReporter;
import javolution.util.FastMap;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.framework.event.MonitorEvent;
import org.kevoree.framework.event.MonitorEventHandler;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoreeAdaptation.AdaptationPrimitive;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/05/12
 * Time: 16:29
 */

@NodeType
@Library(name = "JavaSE")
public class MonitoredJavaSENode extends JavaSENode implements MonitorEventHandler {

    private CommandMapper mapper = null;
    private Map<String,Meter> gauges = null;

    @Override
    public void startNode() {
        mapper = new CommandMapper(this,this);
        gauges = new FastMap<String,Meter>().shared();
        super.startNode();
        //ConsoleReporter.enable(5, TimeUnit.SECONDS);
       // GangliaReporter.enable(30, TimeUnit.SECONDS, "10.0.0.2", 8649);
    }

    @Override
    public void stopNode() {
        super.stopNode();
        try {
            Metrics.shutdown();
        } catch(Exception ignore){
        }
        mapper = null;
        gauges = null;
    }

    public PrimitiveCommand getSuperPrimitive(AdaptationPrimitive a){
         return super.getPrimitive(a);
    }

    @Override
    public PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return mapper.buildPrimitiveCommand(adaptationPrimitive);
    }

    @Override
    public void triggerEvent(MonitorEvent event) {
        String key = event.getClass().getName()+event.getOriginID();
        Meter m = gauges.get(key);
        if(m == null){
            m = Metrics.newMeter(event.getClass(), key, "requests", TimeUnit.SECONDS);
            gauges.put(key,m);
        }
        m.mark();
    }

}

