package org.kevoree.library.mavenCache;

import org.kevoree.DeployUnit;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.api.service.core.classloading.DeployUnitResolver;
import org.kevoree.framework.AbstractComponentType;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 20/11/12
 * Time: 01:29
 */
@Library(name = "JavaSE")
@ComponentType
public class MavenP2PResolver extends AbstractComponentType implements DeployUnitResolver {

    @Start
    public void startResolver(){
        getBootStrapperService().getKevoreeClassLoaderHandler().registerDeployUnitResolver(this);
    }

    @Stop
    public void stopResolver(){
        getBootStrapperService().getKevoreeClassLoaderHandler().unregisterDeployUnitResolver(this);
    }

    @Override
    public File resolve(DeployUnit du) {



        System.out.println("I'm called !");
        return null;
    }
}
