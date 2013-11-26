package org.kevoree.api;

import org.kevoree.ContainerRoot;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 26/11/2013
 * Time: 09:21
 */
public interface KevScriptService {

    public void execute(String script, ContainerRoot model);

    public void executeFromStream(InputStream script, ContainerRoot model);

}
