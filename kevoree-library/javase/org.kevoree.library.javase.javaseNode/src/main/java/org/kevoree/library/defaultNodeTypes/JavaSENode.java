/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.CommandMapper;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@NodeType
@DictionaryType({
        @DictionaryAttribute(name = "logLevel", defaultValue = "INFO", optional = true, vals = {"INFO","WARN","DEBUG","ERROR"})
})
@PrimitiveCommands(
        values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty","RemoveThirdParty"}, value = {})
public class JavaSENode extends AbstractNodeType {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JavaSENode.class);

    private KevoreeKompareBean kompareBean = null;
    private CommandMapper mapper = null;

	private boolean isRunning;

    @Start
    @Override
    public void startNode() {
		isRunning = true;
        kompareBean = new KevoreeKompareBean();
        mapper = new CommandMapper();
        mapper.setNodeType(this);

        updateNode();

		new Thread() {
			@Override
			public void run () {
				catchShutdown();
			}
		}.start();
    }


    @Stop
    @Override
    public void stopNode() {
        kompareBean = null;
        mapper = null;
		isRunning = false;
        //Cleanup the local runtime
        KevoreeDeployManager.clearAll(this);
    }

    @Update
    @Override
    public void updateNode(){
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if("DEBUG".equals(getDictionary().get("logLevel"))){root.setLevel(Level.DEBUG);}
        if("WARN".equals(getDictionary().get("logLevel"))){root.setLevel(Level.WARN);}
        if("INFO".equals(getDictionary().get("logLevel"))){root.setLevel(Level.INFO);}
        if("ERROR".equals(getDictionary().get("logLevel"))){root.setLevel(Level.ERROR);}
    }


    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current, target, this.getNodeName());
    }

    @Override
    public org.kevoree.api.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return mapper.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
    }

	private void catchShutdown () {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		boolean shutdown = false;
		try {
			while (isRunning) {
				line = reader.readLine();
                if(line == null){
                    shutdown = true;
                }
				if (line.equalsIgnoreCase("shutdown")) {
					isRunning = false;
					shutdown = true;
				} else if (line.equalsIgnoreCase("kcl")) {
					System.out.println(this.getBootStrapperService().getKevoreeClassLoaderHandler().getKCLDump());
				} else if (line.equalsIgnoreCase("help")) {
					System.out.println("commands:\n\tshutdown: allow to shutdown the node\n\tkcl: allow to list all the KCLClassLoader and their relationships");
				}
			}
			reader.close();
			if (shutdown) {
				// start the shutdown of the platform
				System.exit(0);
			}
		} catch (IOException e) {
		}
	}

}
