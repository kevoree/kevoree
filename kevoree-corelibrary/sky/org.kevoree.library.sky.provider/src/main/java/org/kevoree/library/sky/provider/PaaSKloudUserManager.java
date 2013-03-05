package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.sky.api.helper.KloudModelHelper;
import org.kevoree.library.sky.provider.api.PaaSManagerService;
import org.kevoree.library.sky.provider.api.PaaSService;
import org.kevoree.library.sky.provider.api.SubmissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/11/12
 * Time: 15:01
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@Provides({
        @ProvidedPort(name = "submit", type = PortType.SERVICE, className = PaaSManagerService.class)
})
@Requires({
        @RequiredPort(name = "delegate", type = PortType.SERVICE, className = PaaSService.class, optional = true)
})
@ComponentType
public class PaaSKloudUserManager extends AbstractComponentType implements PaaSManagerService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    public void start() throws Exception {
    }

    @Stop
    public void stop() {
    }


    @Override
    @Port(name = "submit", method = "initialize")
    public void initialize(String id, ContainerRoot model) throws SubmissionException {
        // fails if the id already exist for a group on the IaaS model

        Group nodeOption = getModelService().getLastModel().findByPath("groups[" + id + "]", Group.class);
        /*for (Group g : getModelService().getLastModel().getGroupsForJ()) {
			if (id.equals(g.getName())) {
				throw new SubmissionException("Platform already exist");
			}
		}*/
        if (nodeOption != null) {
            throw new SubmissionException("Platform already exist");
        }

        // select an IaaS that will be the entry point for the PaaS Group
        String nodeName = PaaSKloudReasoner.selectIaaSNodeAsMaster(getModelService().getLastModel());
        // create the Group with the id as name
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        PaaSKloudReasoner.appendCreatePaaSManagerScript(getModelService().getLastModel(), id, nodeName, getName(), getNodeName(), "delegate", kengine);
        PaaSKloudReasoner.appendCreateGroupScript(getModelService().getLastModel(), id, nodeName, model, kengine);
        Boolean created = false;
        for (int i = 0; i < 5; i++) {
            try {
                kengine.atomicInterpretDeploy();
                created = true;
                break;
            } catch (Exception e) {
                logger.warn("Error while try to add the group for the new PaaS {}, try number {}", id, i);
            }
        }
        // call merge operation if the group has been created
        if (created) {
            merge(id, model);
        }
    }

    @Override
    @Port(name = "submit", method = "add")
    public void add(String id, ContainerRoot model) throws SubmissionException {
        // check if the model contains a PaaS Group
        List<Group> groups = KloudModelHelper.getPaaSKloudGroups(model);
        Boolean paasExists = false;
        entireloop:
        for (Group group : groups) {
            for (Group g : getModelService().getLastModel().getGroups()) {
                if (g.getName().equals(group.getName())) {
                    paasExists = true;
                    break entireloop;
                }
            }
        }
        if (paasExists) {
            getPortByName("submit", PaaSService.class).add(id, model);
        }
    }

    @Override
    @Port(name = "submit", method = "remove")
    public void remove(String id, ContainerRoot model) throws SubmissionException {
        // check if the model contains a PaaS Group
        List<Group> groups = KloudModelHelper.getPaaSKloudGroups(model);
        Boolean paasExists = false;
        entireloop:
        for (Group group : groups) {
            for (Group g : getModelService().getLastModel().getGroups()) {
                if (g.getName().equals(group.getName())) {
                    paasExists = true;
                    break entireloop;
                }
            }
        }
        if (paasExists) {
            getPortByName("submit", PaaSService.class).remove(id, model);
        }
    }

    @Override
    @Port(name = "submit", method = "merge")
    public void merge(String id, ContainerRoot model) throws SubmissionException {
        // check if the model contains a PaaS Group
        List<Group> groups = KloudModelHelper.getPaaSKloudGroups(model);
        Boolean paasExists = false;
        entireloop:
        for (Group group : groups) {
            for (Group g : getModelService().getLastModel().getGroups()) {
                if (g.getName().equals(group.getName())) {
                    paasExists = true;
                    break entireloop;
                }
            }
        }
        if (paasExists) {
            getPortByName("submit", PaaSService.class).merge(id, model);
        }
    }

    @Override
    @Port(name = "submit", method = "release")
    public void release(String id) throws SubmissionException {
        //FIXME (remove PaaSManager)
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        PaaSKloudReasoner.releasePlatform(id, getModelService().getLastModel(), kengine);
        Boolean releaseDone = false;
        for (int i = 0; i < 5; i++) {
            try {
                kengine.atomicInterpretDeploy();
                releaseDone = true;
            } catch (Exception e) {
                logger.warn("Error while releasing platform {}, try number {}", id, i);
            }
        }
        if (!releaseDone) {
            throw new SubmissionException("Unable to release the resources of the PaaS " + id);
        }
    }

    @Override
    @Port(name = "submit", method = "getModel")
    public ContainerRoot getModel(String id) throws SubmissionException {
        // check if the model contains a PaaS Group
        List<Group> groups = KloudModelHelper.getPaaSKloudGroups(getModelService().getLastModel());
        Boolean paasExists = false;
        for (Group group : groups) {
            if (id.equals(group.getName())) {
                paasExists = true;
                break;
            }
        }
        if (paasExists) {
            return getPortByName("submit", PaaSService.class).getModel(id);
        }
        return null;
    }
}
