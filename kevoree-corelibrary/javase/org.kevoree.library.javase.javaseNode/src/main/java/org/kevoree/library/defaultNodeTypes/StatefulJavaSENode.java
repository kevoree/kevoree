package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 16/10/12
 * Time: 15:30
 */
@Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "storageLocation", defaultValue = "", optional = true)
})
@NodeType
public class StatefulJavaSENode extends JavaSENode {

    private StatefulModelListener listener = null;
    private final String property = "java.io.tmpdir";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JavaSENode.class);
    private States state = States.bootstrap;
    private enum States {
        bootstrap,networkLink,ready,loading
    }


    class StatefulModelListener implements ModelListener {
        public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            return true;
        }

        public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            return true;
        }

        public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
           return true;
        }

        public void modelUpdated() {
            logger.debug("ModelUpdated("+StatefulJavaSENode.this.toString()+")");
            switch (state) {
                case bootstrap: {
                    state=States.networkLink;
                    logger.debug("State Bootstrap -> Links");
                }break;
                case networkLink: {
                    File inputModel  = getLastPersistedModel();
                    if(inputModel.exists()){
                        logger.info("Stateful node ready. Loading last config from " + inputModel.getAbsolutePath());
                        logger.debug("State NetworkLink => Loading");
                        state=States.loading;
                        getModelService().updateModel(KevoreeXmiHelper.load(inputModel.getAbsolutePath()));
                    } else {
                        state=States.ready;
                        logger.debug("State NetworkLink => Ready");
                        logger.info("Stateful node ready. No stored model found at " + inputModel.getAbsolutePath());
                    }
                }break;
                case loading: {
                    state=States.ready;
                    logger.debug("State Loading => Ready");
                }break;
                case ready: {
                    try{
                        File lastSaved = getLastPersistedModel();
                        if(lastSaved.exists()) {
                            KevoreeXmiHelper.save(lastSaved.getAbsolutePath() + "." +System.currentTimeMillis()+".kev" ,KevoreeXmiHelper.load(lastSaved.getAbsolutePath()));
                        }
                       // logger.debug("Stateful node started storage of new model at " + lastSaved.getAbsolutePath());
                        KevoreeXmiHelper.save(lastSaved.getAbsolutePath(),getModelService().getLastModel());
                        logger.info("Stateful node stored new model at " + lastSaved.getAbsolutePath());
                    } catch(Exception e) {
                        logger.error("Error while saving state",e);
                    }
                }break;
            }
        }

        public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            logger.debug("PreRollback");
        }

        public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            logger.debug("POSTRollback");
        }
    }

    @Start
    @Override
    public void startNode() {
        super.startNode();

        listener = new StatefulModelListener();
        getModelService().registerModelListener(listener);

    }

    private File getLastPersistedModel() {
        String baseLocation = "";
        if(getDictionary().get("storageLocation") != null) {
            baseLocation = (String) getDictionary().get("storageLocation");
        } else {
            baseLocation = System.getProperty(property);
        }
        if(!baseLocation.endsWith(File.separator)) {
            baseLocation += File.separator;
        }
        return new File(baseLocation + getNodeName()+".kev");

    }

    @Stop
    @Override
    public void stopNode() {
        getModelService().unregisterModelListener(listener);
        listener = null;
        logger.info("Stateful node stopped");
        super.stopNode();
    }

    @Update
    @Override
    public void updateNode() {
        super.updateNode();
    }


}
