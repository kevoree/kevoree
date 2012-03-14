package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/01/12
 * Time: 10:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentType
@Provides({
        @ProvidedPort(name = "deploy", type = PortType.MESSAGE), // TODO define a message type
        @ProvidedPort(name = "release", type = PortType.MESSAGE) // TODO define a message type
})
public class PaaSKloudResourceManager extends AbstractComponentType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Port(name = "deploy")
    public void deploy(Object message) {
        if (message instanceof StdKevoreeMessage) {
            StdKevoreeMessage stdMessage = (StdKevoreeMessage) message;
            if (stdMessage.getValue("login").isDefined() && stdMessage.getValue("model").isDefined()) {
                final String login = (String) stdMessage.getValue("login").get();
                final ContainerRoot model = KevoreeXmiHelper.loadString((String) stdMessage.getValue("model").get());
                final String sshKey = (String) stdMessage.getValue("sshKey").get();
                // check if a previous deploy has already done for this login
                if (!KloudHelper.lookForAGroup(login, this.getModelService().getLastModel())) {
                    processNew(model, login, sshKey);
                }
            }
        }
    }

    @Port(name = "release")
    public void release(Object message) {
        if (message instanceof StdKevoreeMessage) {
            StdKevoreeMessage stdMessage = (StdKevoreeMessage) message;
            if (stdMessage.getValue("login").isDefined()) {
                String login = (String) stdMessage.getValue("login").get();
                KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
                KloudReasoner.appendScriptToCleanupIaaSModelFromUser(kengine, login, getModelService().getLastModel());
                for (int i = 0; i < 5; i++) {
                    try {
                        if (kengine.atomicInterpretDeploy()) {
                            return;
                        }
                    } catch (Exception e) {
                        logger.warn("Error while cleanup user, try number " + i);
                    }
                }
            }
        }
    }

    private void processNew(ContainerRoot userModel, String login, String sshKey) {
        logger.debug("starting processNew");
        KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
        KloudReasoner.appendCreateGroupScript(getModelService().getLastModel(), login, this.getNodeName(), kengine, sshKey);
        for (int i = 0; i < 5; i++) {
            try {
                if (kengine.atomicInterpretDeploy()) {
                    break;
                }
            } catch (Exception e) {
                logger.warn("Error while adding user master group, try number " + i);
            }
        }
        //ADD GROUP
        Option<ContainerRoot> userModelUpdated = KloudReasoner.updateUserConfiguration(login, userModel, getModelService().getLastModel(), getKevScriptEngineFactory());

        if (userModelUpdated.isDefined()) {
            /* Send blindly the model to the core , PaaS Group are in charge to trigger this request , reply false and forward to Master interested node  */
            getModelService().updateModel(userModel);
        } else {
            //TODO CALL RELEASE
        }


    }
}