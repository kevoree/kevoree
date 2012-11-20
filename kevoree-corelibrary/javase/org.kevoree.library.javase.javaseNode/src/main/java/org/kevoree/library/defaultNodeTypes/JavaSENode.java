/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.framework.ModelHandlerServiceProxy;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.CommandMapper;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
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
        @DictionaryAttribute(name = "logLevel", defaultValue = "INFO", optional = true, vals = {"INFO", "WARN", "DEBUG", "ERROR", "FINE"}),
        @DictionaryAttribute(name = "coreLogLevel", defaultValue = "WARN", optional = true, vals = {"INFO", "WARN", "DEBUG", "ERROR", "FINE"})
})
@PrimitiveCommands(
        values = {"UpdateType", "AddType", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty", "RemoveThirdParty"},
        value = {@PrimitiveCommand(name="AddDeployUnit",maxTime = 120000),@PrimitiveCommand(name="UpdateDeployUnit",maxTime = 120000)})
public class JavaSENode extends AbstractNodeType implements ModelListener {
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(JavaSENode.class);

    private KevoreeKompareBean kompareBean = null;
    private CommandMapper mapper = null;

    private boolean isRunning;
    private Thread shutdownThread;


    protected boolean isDaemon() {
        return false;
    }

    @Start
    @Override
    public void startNode() {
        mapper = new CommandMapper();
        preTime = System.currentTimeMillis();
        getModelService().registerModelListener(this);
        isRunning = true;
        kompareBean = new KevoreeKompareBean();
        mapper.setNodeType(this);
        updateNode();
        if (!isDaemon()) {
            shutdownThread = new Thread() {
                @Override
                public void run() {
                    catchShutdown();
                }
            };
            shutdownThread.start();
        }
        KevoreeDeployManager.startPool();
    }


    @Stop
    @Override
    public void stopNode() {
        getModelService().unregisterModelListener(this);
        kompareBean = null;
        mapper = null;
        isRunning = false;
        if (shutdownThread != null) {
            shutdownThread.stop();
        }
        //Cleanup the local runtime
        KevoreeDeployManager.clearAll(this);
        KevoreeDeployManager.stopPool();
    }

    @Update
    @Override
    public void updateNode() {
        if (getBootStrapperService().getKevoreeLogService() != null) {
            KevoreeLogLevel logLevel = KevoreeLogLevel.WARN;
            KevoreeLogLevel corelogLevel = KevoreeLogLevel.WARN;

            String logLevelVal = (String)getDictionary().get("logLevel");
            if ("DEBUG".equals(logLevelVal)) {
                logLevel = KevoreeLogLevel.DEBUG;
            } else if ("WARN".equals(logLevelVal)) {
                logLevel = KevoreeLogLevel.WARN;
            } else if ("INFO".equals(logLevelVal)) {
                logLevel = KevoreeLogLevel.INFO;
            } else if ("ERROR".equals(logLevelVal)) {
                logLevel = KevoreeLogLevel.ERROR;
            } else if ("FINE".equals(logLevelVal)) {
                logLevel = KevoreeLogLevel.FINE;
            }

            String coreLogLevelVal = (String)getDictionary().get("coreLogLevel");
            if ("DEBUG".equals(coreLogLevelVal)) {
                corelogLevel = KevoreeLogLevel.DEBUG;
            } else if ("WARN".equals(coreLogLevelVal)) {
                corelogLevel = KevoreeLogLevel.WARN;
            } else if ("INFO".equals(coreLogLevelVal)) {
                corelogLevel = KevoreeLogLevel.INFO;
            } else if ("ERROR".equals(coreLogLevelVal)) {
                corelogLevel = KevoreeLogLevel.ERROR;
            } else if ("FINE".equals(coreLogLevelVal)) {
                corelogLevel = KevoreeLogLevel.FINE;
            }

            getBootStrapperService().getKevoreeLogService().setUserLogLevel(logLevel);
            getBootStrapperService().getKevoreeLogService().setCoreLogLevel(corelogLevel);
        }
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current, target, this.getNodeName());
    }

    @Override
    public org.kevoree.api.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return mapper.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
    }

    private void catchShutdown() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String line = reader.readLine();
            boolean shutdown = false;
            while (isRunning && !shutdown) {
                if ("shutdown".equalsIgnoreCase(line)) {
                    shutdown = true;
                } else if ("kcl".equalsIgnoreCase(line)) {
                    System.out.println(this.getBootStrapperService().getKevoreeClassLoaderHandler().getKCLDump());
                } else if ("help".equalsIgnoreCase(line)) {
                    System.out.println("commands:\n\tshutdown: shutdown the node\n\tkcl: lists all KCLClassLoaders and their relationships\n\thelp: displays this list");
                } else if (line == null) {
                    isRunning = false;
                }
                line = reader.readLine();
            }
            if (shutdown) {
                // start the shutdown of the platform
                System.exit(0);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Long preTime = 0l;

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        preTime = System.currentTimeMillis();
        logger.info("JavaSENode received a new Model to apply...");
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        mapper.doEnd();
        logger.info("JavaSENode Update completed in {} ms",(System.currentTimeMillis() - preTime));
        return true;
    }

    @Override
    public void modelUpdated() {
    }

	@Override
	public void preRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		logger.warn("JavaSENode is aborting last update...");
	}

	@Override
	public void postRollback (ContainerRoot containerRoot, ContainerRoot containerRoot1) {
		logger.warn("JavaSENode update aborted in {} ms",(System.currentTimeMillis() - preTime));
	}
}
