package org.kevoree.library.javase.webserver.webbit;

import org.kevoree.annotation.*;
import org.kevoree.context.CounterHistoryMetric;
import org.kevoree.context.Metric;
import org.kevoree.context.PutHelper;
import org.kevoree.framework.AbstractComponentType;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 11/12/12
 * Time: 11:28
 */
@Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8080", optional = true),
        @DictionaryAttribute(name = "period_check", defaultValue = "5000", optional = true)
})
@ComponentType
public class WebbitServer extends AbstractComponentType {

    private WebServer server = null;
    private ScheduledExecutorService t = null;
    private Metric latencyMetric = null;
    private SelfMonitor monitor = null;

    @Start
    public void start() throws ExecutionException, InterruptedException, MalformedURLException {
        server = WebServers.createWebServer(Integer.parseInt(getDictionary().get("port").toString())).add(new HelloWorldHttpHandler()).start().get();
        latencyMetric = PutHelper.getMetric(getModelService().getContextModel(), "perf/latency/{"+getModelElement().path()+"}", PutHelper.getParam().setMetricTypeClazzName(CounterHistoryMetric.class.getName()).setNumber(1000));
        monitor = new SelfMonitor(new URL("http://localhost:"+getDictionary().get("port")),latencyMetric);
        t = Executors.newScheduledThreadPool(1);
        t.scheduleAtFixedRate(monitor,100,Integer.parseInt(getDictionary().get("period_check").toString()), TimeUnit.MILLISECONDS);
    }

    @Stop
    public void stop(){
        t.shutdownNow();
        t = null;
        server = null;
        latencyMetric = null;
        monitor = null;
    }

    @Update
    public void update() throws InterruptedException, ExecutionException, MalformedURLException {
        stop();
        start();
    }


}
