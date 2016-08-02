package org.kevoree.api;

import org.kevoree.ContainerRoot;
import org.kevoree.api.adaptation.AdaptationModel;
import org.kevoree.api.adaptation.AdaptationPrimitive;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 31/12/11
 * Time: 09:50
 */
public interface NodeType {

    AdaptationModel plan(ContainerRoot actualModel, ContainerRoot targetModel);

    PrimitiveCommand getPrimitive(AdaptationPrimitive primitive);
}