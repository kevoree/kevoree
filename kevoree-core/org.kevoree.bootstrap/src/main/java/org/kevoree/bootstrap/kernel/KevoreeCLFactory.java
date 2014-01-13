package org.kevoree.bootstrap.kernel;

import org.kevoree.DeployUnit;
import org.kevoree.kcl.api.FlexyClassLoader;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 23:33
 */
public interface KevoreeCLFactory {

    public FlexyClassLoader createClassLoader(DeployUnit du, File file);

}
