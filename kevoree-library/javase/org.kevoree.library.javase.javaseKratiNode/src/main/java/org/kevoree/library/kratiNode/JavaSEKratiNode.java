/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.kratiNode;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ContextModel;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.framework.context.KevoreeDeployManager;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.JavaSENode;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@NodeType
public class JavaSEKratiNode extends JavaSENode {




    @Override
    public ContextModel getContextModel() {
        return super.getContextModel();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
