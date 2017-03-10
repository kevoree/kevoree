package org.kevoree.api;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.AdaptationCommand;
import org.kevoree.adaptation.KevoreeAdaptationException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 31/12/11
 * Time: 09:50
 */
public interface NodeType {

    List<AdaptationCommand> plan(ContainerRoot actualModel, ContainerRoot targetModel)
            throws KevoreeAdaptationException;
}