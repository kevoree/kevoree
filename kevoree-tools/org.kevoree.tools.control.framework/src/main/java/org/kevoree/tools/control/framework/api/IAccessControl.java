package org.kevoree.tools.control.framework.api;


import org.kevoree.ContainerRoot;
import org.kevoree.KControlModel.KControlRoot;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 21/01/13
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public interface IAccessControl
{

    public KControlRoot getControlRoot();
    /**
     * Call Kompare
     * @param nodeName
     * @param model
     * @param target
     * @return  The AdaptationPrimitive refused if is empty the access is approval
     */
    public List<AdaptationPrimitive> approval(String nodeName, ContainerRoot model, SignedModel target_model) throws ControlException;


    /**
     * don't call Kompare
     * @param adaptationModel
     * @param target_model
     * @return
     * @throws ControlException
     */
    public List<AdaptationPrimitive> approval(AdaptationModel adaptationModel,SignedModel target_model) throws ControlException;


}
