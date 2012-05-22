package org.kevoree.library.webserver.sample.monitoring;

import com.yammer.metrics.reporting.ConsoleReporter;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/05/12
 * Time: 15:46
 */
@ComponentType
@Library(name = "JavaSE")
public class ConsoleMetricsReporter extends AbstractComponentType {

    @Start
    public void start(){
        ConsoleReporter.enable(3, TimeUnit.SECONDS);
    }

    @Stop
    public void stop(){

    }
}
