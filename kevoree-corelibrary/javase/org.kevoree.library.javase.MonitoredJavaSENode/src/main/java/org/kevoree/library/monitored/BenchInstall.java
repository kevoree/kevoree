package org.kevoree.library.monitored;

import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.api.service.core.script.KevScriptEngineException;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.impl.DefaultKevoreeFactory;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 31/05/12
 * Time: 01:26
 */
@Library(name = "JavaSE")
@ComponentType
public class BenchInstall extends AbstractComponentType implements Runnable {

    Thread t = null;


    @Start
    public void start() {
        t = new Thread(this);
        t.start();
    }

    @Stop
    public void stop() {
        t.interrupt();
        t = null;
    }

    private Random r = new Random();

    @Override
    public void run() {

        KevScriptEngine engine = getKevScriptEngineFactory().createKevScriptEngine();
        engine.append("merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.fakeDomo/" + new DefaultKevoreeFactory().getVersion() + "'");
        String name = "benchConsole" + Math.abs(r.nextInt());
        engine.addVariable("benchName", name);
        engine.addVariable("nodeName", getNodeName());
        engine.append("addComponent {benchName}@{nodeName} : FakeConsole");
        try {
            engine.atomicInterpretDeploy();
        } catch (KevScriptEngineException e) {
            e.printStackTrace();
        }

        System.out.println("After install Console");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        engine.clearScript();
        engine.append("removeComponent {benchName}@{nodeName}");
        try {
            engine.atomicInterpretDeploy();
        } catch (KevScriptEngineException e) {
            e.printStackTrace();
        }
    }
}
