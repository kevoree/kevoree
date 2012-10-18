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

    class StatefulModelListener implements ModelListener {
        public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            return true;
        }

        public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            return true;
        }

        public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            try{
                File lastSaved = getLastPersistedModel();
                if(lastSaved.exists()) {
                    KevoreeXmiHelper.save(lastSaved.getAbsolutePath() + ".bk",KevoreeXmiHelper.load(lastSaved.getAbsolutePath()));
                }
                logger.debug("Stateful node started storage of new model at " + lastSaved.getAbsolutePath());
                KevoreeXmiHelper.save(lastSaved.getAbsolutePath(),containerRoot1);
                logger.info("Stateful node stored new model at " + lastSaved.getAbsolutePath());
                return true;
            } catch(Exception e) {
                logger.error("Error while saving state",e);
                return false;
            }

        }

        public void modelUpdated() {

        }

        public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {

        }

        public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {

        }
    }

    @Start
    @Override
    public void startNode() {
        super.startNode();

        listener = new StatefulModelListener();
        getModelService().registerModelListener(listener);

        File inputModel  = getLastPersistedModel();
        if(inputModel.exists()){
            getModelService().updateModel(KevoreeXmiHelper.load(inputModel.getAbsolutePath()));
            logger.info("Stateful node started with stored model: " + inputModel.getAbsolutePath());
        } else {
            logger.info("Stateful node ready. No stored model found at " + inputModel.getAbsolutePath());
        }
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
