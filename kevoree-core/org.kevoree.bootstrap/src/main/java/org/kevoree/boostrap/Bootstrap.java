package org.kevoree.boostrap;

import org.kevoree.boostrap.kernel.KevoreeCLKernel;
import org.kevoree.core.impl.KevoreeCoreBean;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 23:47
 */
public class Bootstrap {

    private KevoreeCoreBean core = new KevoreeCoreBean();

    private KevoreeCLKernel kernel = new KevoreeCLKernel();

    public Bootstrap(String nodeName) {
        core.setNodeName(nodeName);

    }

}
