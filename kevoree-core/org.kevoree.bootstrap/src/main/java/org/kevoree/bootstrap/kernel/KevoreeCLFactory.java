package org.kevoree.bootstrap.kernel;

import org.kevoree.DeployUnit;
import org.kevoree.kcl.KevoreeJarClassLoader;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 23:33
 */
public interface KevoreeCLFactory {

    public KevoreeJarClassLoader createClassLoader(DeployUnit du, File file);

}
