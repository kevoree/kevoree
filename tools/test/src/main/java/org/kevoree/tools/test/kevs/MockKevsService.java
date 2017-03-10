package org.kevoree.tools.test.kevs;

import org.kevoree.ContainerRoot;
import org.kevoree.KevScriptException;
import org.kevoree.api.KevScriptService;

import java.io.InputStream;
import java.util.HashMap;

/**
 *
 * Created by leiko on 1/16/17.
 */
public class MockKevsService implements KevScriptService {

    private MockKevsService() {}

    @Override
    public void execute(String script, ContainerRoot model)
            throws KevScriptException {

    }

    @Override
    public void execute(String script, ContainerRoot model, HashMap<String, String> ctxVars)
            throws KevScriptException {

    }

    @Override
    public void executeFromStream(InputStream script, ContainerRoot model)
            throws KevScriptException {

    }

    @Override
    public void executeFromStream(InputStream script, ContainerRoot model, HashMap<String, String> ctxVars)
            throws KevScriptException {

    }

    public static class Builder {
        private MockKevsService service = new MockKevsService();

        public MockKevsService build() {
            return service;
        }
    }
}
