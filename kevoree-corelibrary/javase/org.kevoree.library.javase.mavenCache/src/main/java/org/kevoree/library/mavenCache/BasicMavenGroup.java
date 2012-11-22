package org.kevoree.library.mavenCache;

import org.kevoree.*;
import org.kevoree.annotation.*;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.api.service.core.classloading.DeployUnitResolver;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.library.BasicGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/11/12
 * Time: 09:32
 */

@DictionaryType({
        @DictionaryAttribute(name = "repo_port", defaultValue = "8090", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "server", defaultValue = "false", optional = true, fragmentDependant = true)
})
@GroupType
@Library(name = "JavaSE", names = "Android")
public class BasicMavenGroup extends BasicGroup implements Runnable, DeployUnitResolver {

    NanoHTTPD srv = null;
    ExecutorService pool = null;
    AtomicReference<ContainerRoot> cachedModel = new AtomicReference<ContainerRoot>();
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private Boolean server = false;
    private AtomicReference<List<String>> remoteURLS = null;

    @Start
    public void startBasicMavenGroup() throws IOException {
        server = Boolean.parseBoolean(this.getDictionary().get("server").toString());
        if (server) {
            pool = Executors.newSingleThreadExecutor();
            File repositoryFile = new File(System.getProperty("user.home").toString() + File.separator + ".m2" + File.separator + "repository");
            srv = new NanoHTTPD(Integer.parseInt(getDictionary().get("repo_port").toString()), repositoryFile);
        } else {
            remoteURLS = new AtomicReference<List<String>>();
            remoteURLS.set(new ArrayList<String>());
            getBootStrapperService().getKevoreeClassLoaderHandler().registerDeployUnitResolver(this);
        }
    }

    @Stop
    public void stopBasicMavenGroup() {
        if (server) {
            pool.shutdownNow();
            srv.stop();
            srv = null;
        } else {
            getBootStrapperService().getKevoreeClassLoaderHandler().unregisterDeployUnitResolver(this);
            remoteURLS.set(null);
            remoteURLS = null;
        }
    }

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        cachedModel.set(proposedModel);
        if (server) {
            pool.submit(this);
        } else {
            List<String> urls = new ArrayList<String>();
            ContainerRoot model = getModelService().getLastModel();
            for (Group group : model.getGroupsForJ()) {
                if (group.getTypeDefinition().getName().equals(BasicMavenGroup.class.getSimpleName())) {
                    for (ContainerNode child : group.getSubNodesForJ()) {
                        Object server = KevoreePropertyHelper.getProperty(model, group, null, "server", true, child.getName());
                        if (server != null) {
                            logger.info("Cache Found on node " + child.getName());
                            List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(model, child.getName(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
                            Object port = KevoreePropertyHelper.getProperty(model, child, null, "repo_port", false, null);
                            for (String remoteIP : ips) {
                                String url = "http://" + remoteIP + ":" + port;
                                logger.info("Add URL " + url);
                                urls.add(url);
                            }
                        }
                    }
                }
            }
            remoteURLS.set(urls);
        }
        return true;
    }

    @Override
    public void run() {
        ContainerRoot model = cachedModel.get();
        if (model != null) {
            for (DeployUnit du : model.getDeployUnitsForJ()) {
                logger.debug("CacheFile for DU : " + du.getUnitName() + ":" + du.getGroupName() + ":" + du.getVersion());
                File cachedFile = getBootStrapperService().resolveDeployUnit(du);
            }
        }
    }

    @Override
    public File resolve(DeployUnit du) {
        File resolved = getBootStrapperService().resolveArtifact(du.getUnitName(), du.getGroupName(), du.getVersion(), remoteURLS.get());
        logger.info("DU " + du.getUnitName() + " from cache resolution " + (resolved != null));
        return null;
    }
}
