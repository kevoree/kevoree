package org.kevoree.library.monitored;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.ConsoleReporter;
import javolution.util.FastMap;
import org.kevoree.Instance;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.NodeType;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.framework.event.MonitorEvent;
import org.kevoree.framework.event.MonitorEventHandler;
import org.kevoree.kompare.JavaSePrimitive;
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

    private Map<String,Meter> gauges = null;

    @Override
    public void startNode() {
        gauges = new FastMap<String,Meter>().shared();
        super.startNode();
    }

    @Override
    public void stopNode() {
        super.stopNode();
        try {
            Metrics.shutdown();
        } catch(Exception ignore){
        }
        gauges = null;
    }

    public PrimitiveCommand getSuperPrimitive(AdaptationPrimitive a){
         return super.getPrimitive(a);
    }

    @Override
    public PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {

        if(adaptationPrimitive.get_primitiveType().getName().equals(JavaSePrimitive.AddInstance())){
               return new MonitoredAddInstance(this, (Instance)adaptationPrimitive.getRef(), getNodeName(), getModelService(), getKevScriptEngineFactory(), getBootStrapperService());
        } else {
            return super.getPrimitive(adaptationPrimitive);
        }
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

