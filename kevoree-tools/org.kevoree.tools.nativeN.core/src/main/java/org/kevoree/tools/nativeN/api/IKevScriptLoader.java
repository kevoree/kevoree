package org.kevoree.tools.nativeN.api;

import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.script.KevScriptEngineException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public interface IKevScriptLoader {
    public ContainerRoot loadKevScript(String path_file) throws KevScriptEngineException;
}
