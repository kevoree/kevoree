/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.NodeType;
import org.kevoree.framework.AbstractNodeType;
import org.kevoreeAdaptation.AdaptationModel;

/**
 *
 * @author ffouquet
 */
@NodeType
public class JavaSENode extends AbstractNodeType {


    @Override
    public void push(String targetNodeName, ContainerRoot root) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean deploy(AdaptationModel model, String nodeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
