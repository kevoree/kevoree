package org.kevoree.api;

import org.kevoree.ContainerRoot;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 09:21
 */
public interface KevScriptService {

    void execute(String script, ContainerRoot model) throws Exception;

    void execute(String script, ContainerRoot model, HashMap<String, String> ctxVars) throws Exception;

    void executeFromStream(InputStream script, ContainerRoot model) throws Exception;

    void executeFromStream(InputStream script, ContainerRoot model, HashMap<String, String> ctxVars) throws Exception;
}
