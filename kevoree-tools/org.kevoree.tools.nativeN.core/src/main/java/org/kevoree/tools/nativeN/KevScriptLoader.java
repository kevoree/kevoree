package org.kevoree.tools.nativeN;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.api.service.core.script.KevScriptEngineException;

import org.kevoree.tools.marShell.KevScriptOfflineEngine;
import org.kevoree.tools.modelsync.FakeBootstraperService;
import org.kevoree.tools.nativeN.api.IKevScriptLoader;
import org.kevoree.tools.nativeN.utils.FileManager;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class KevScriptLoader implements IKevScriptLoader {

    /**
     * Loading kevScript in ContainerRoot from file
     *
     * @param path_file
     * @return
     */
    public ContainerRoot loadKevScript(String path_file) throws KevScriptEngineException {

        ContainerRoot basemodel = KevoreeFactory.createContainerRoot();
        byte [] file = FileManager.load(path_file);
        String kevScript = new String(file);
        FakeBootstraperService bootstraper = new FakeBootstraperService();
        KevScriptOfflineEngine kevOfflineEngine = new KevScriptOfflineEngine(basemodel,bootstraper.getBootstrap()) ;
        for( String pro : System.getProperties().stringPropertyNames())
        {
            kevOfflineEngine.addVariable(pro,System.getProperty(pro));
        }
        kevOfflineEngine.addVariable("kevoree.version", KevoreeFactory.getVersion());
        // add MessagePort
        String kev_framework ="merge 'mvn:org.kevoree.corelibrary.android/org.kevoree.library.android.logger/{kevoree.version}'";

        kevOfflineEngine.append("{"+kev_framework+"\n"+kevScript.replace("tblock","")+"}") ;
        ContainerRoot model = kevOfflineEngine.interpret();

        return model;
    }
}
