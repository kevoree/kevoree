package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.KevoreeXmiHelper;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 16/10/12
 * Time: 15:30
 */
@Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "state_path", defaultValue = "", optional = true)
})
@NodeType
public class StatefulJavaSENode extends JavaSENode {

    class StatefullModelListener implements ModelListener {


        @Override
        public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            return true;
        }

        @Override
        public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            return true;
        }

        @Override
        public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
            try{
                String tempDir = System.getProperty(property);
                File inputModel  = new File(tempDir+File.separator+getNodeName()+".kev");
                KevoreeXmiHelper.save(inputModel.getAbsolutePath(),containerRoot1);
                return true;
            } catch(Exception e) {
                logger.error("Error while saving state",e);
                return false;
            }

        }

        @Override
        public void modelUpdated() {

        }

        @Override
        public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {

        }

        @Override
        public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {

        }
    }

    private StatefullModelListener listener = null;
    private final String property = "java.io.tmpdir";


    @Start
    @Override
    public void startNode() {
        listener = new StatefullModelListener();
        getModelService().registerModelListener(listener);
        String tempDir = System.getProperty(property);
        File inputModel  = new File(tempDir+File.separator+getNodeName()+".kev");
        if(inputModel.exists()){
            getModelService().updateModel(KevoreeXmiHelper.load(inputModel.getAbsolutePath()));
        }
    }

    @Stop
    @Override
    public void stopNode() {
        getModelService().unregisterModelListener(listener);
        listener = null;
    }

    @Update
    @Override
    public void updateNode() {

    }


}
