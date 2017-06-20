package org.kevoree.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kevoree.KevoreeCoreException;
import org.kevoree.MavenRuntimeService;
import org.kevoree.annotation.KevoreeInject;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.reflect.Injector;
import org.kevoree.resolver.MavenResolverException;
import org.kevoree.service.ContextAwareModelServiceAdapter;
import org.kevoree.service.KevScriptService;
import org.kevoree.service.RuntimeService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * Created by leiko on 6/7/17.
 */
public class TestKevoreeCore {

    private KevoreeCoreImpl core;
    private ContextAwareModelServiceAdapter adapter;

    @Before
    public void setUp() throws KevoreeCoreException, MavenResolverException {
        this.core = new KevoreeCoreImpl();

        Injector injector = new Injector(KevoreeInject.class);
        injector.register(RuntimeService.class, new MavenRuntimeService(core, injector));
        injector.register(KevScriptService.class, new KevScriptEngine("https://new-registry.kevoree.org"));

        injector.inject(this.core);
        this.adapter = new ContextAwareModelServiceAdapter(core, "/nodes[node0]");
        this.core.start();
    }

    @Test
    @Ignore
    public void testKevsService() throws TimeoutException, ExecutionException, InterruptedException {
        Future<Exception> task = adapter.submitScript("add node0: JavaNode/LATEST/LATEST");
        Exception ex = task.get(2000, TimeUnit.MILLISECONDS);
        Assert.assertNull("submitScript exception", ex);
        Assert.assertNotNull("JavaNode \"node0\" should be added to model", core.getCurrentModel().findNodesByID("node0"));
    }
}
