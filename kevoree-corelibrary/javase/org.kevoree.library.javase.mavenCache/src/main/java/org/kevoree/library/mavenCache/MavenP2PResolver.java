package org.kevoree.library.mavenCache;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.service.core.classloading.DeployUnitResolver;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 20/11/12
 * Time: 01:29
 */
@Library(name = "JavaSE")
@ComponentType
public class MavenP2PResolver extends AbstractComponentType implements DeployUnitResolver, ModelListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void startResolver(){
        remoteURLS = new AtomicReference<List<String>>();
        remoteURLS.set(new ArrayList<String>());
        getBootStrapperService().getKevoreeClassLoaderHandler().registerDeployUnitResolver(this);
        getModelService().registerModelListener(this);
    }

    @Stop
    public void stopResolver(){
        getModelService().unregisterModelListener(this);
        getBootStrapperService().getKevoreeClassLoaderHandler().unregisterDeployUnitResolver(this);
        remoteURLS.set(null);
        remoteURLS = null;
    }

    private AtomicReference<List<String>> remoteURLS = null;

    @Override
    public File resolve(DeployUnit du) {
        File resolved = getBootStrapperService().resolveArtifact(du.getUnitName(), du.getGroupName(), du.getVersion(), remoteURLS.get());
        logger.info("DU "+du.getUnitName()+" from cache resolution "+(resolved!= null));
        return null;
    }

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
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
        List<String> urls = new ArrayList<String>();
        ContainerRoot model = getModelService().getLastModel();
        for(ContainerNode node : model.getNodesForJ()){
            for(ComponentInstance inst : node.getComponentsForJ()){
                if(inst.getTypeDefinition().getName().equals("MavenCacheServer")){
                    List<String> ips = KevoreePropertyHelper.getNetworkProperties(model, node.getName(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
                    logger.info("Cache Found on node "+node.getName());
                    Object port = KevoreePropertyHelper.getProperty(inst,"port",false,null);
                    for(String remoteIP : ips){
                       String url = "http://"+remoteIP+":"+port;
                        logger.info("Add URL "+url);
                       urls.add(url);
                    }

                }
            }
        }
        remoteURLS.set(urls);
    }

    @Override
    public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }

    @Override
    public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }
}
