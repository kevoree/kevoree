package org.kevoree.library.mavenCache;

import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.AbstractComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 20/11/12
 * Time: 01:06
 */
@Library(name = "JavaSE")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8090", optional = true)
})
public class MavenCacheServer extends AbstractComponentType implements ModelListener, Runnable {

    NanoHTTPD srv = null;
    ExecutorService pool = null;
    AtomicReference<ContainerRoot> cachedModel = new AtomicReference<ContainerRoot>();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void startCacheSrv() throws IOException {
        pool = Executors.newSingleThreadExecutor();
        File repositoryFile = new File(System.getProperty("user.home").toString() + File.separator + ".m2" + File.separator + "repository");
        new NanoHTTPD(Integer.parseInt(getDictionary().get("port").toString()), repositoryFile);
        getModelService().registerModelListener(this);
    }

    @Stop
    public void stopCacheSrv() {
        getModelService().unregisterModelListener(this);
        pool.shutdownNow();
        srv.stop();
        srv = null;
    }

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        cachedModel.set(proposedModel);
        pool.submit(this);
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public void modelUpdated() {
    }

    @Override
    public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }

    @Override
    public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }

    @Override
    public void run() {
        ContainerRoot model = cachedModel.get();
        if(model != null){
            for(DeployUnit du : model.getDeployUnits()){
                logger.debug("CacheFile for DU : "+du.getUnitName()+":"+du.getGroupName()+":"+du.getVersion());
                File cachedFile = getBootStrapperService().resolveDeployUnit(du);
            }
        }
    }
}
