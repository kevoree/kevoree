/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.NodeType;
import org.kevoree.framework.AbstractNodeType;

/**
 *
 * @author ffouquet
 */
@NodeType
public class JavaSENode extends AbstractNodeType {

    @Override
    public void deploy(String string, ContainerRoot cr) {
        System.out.println("Hello world");
    }

}
