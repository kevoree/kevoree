package org.kevoree.library.sky.provider;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.FileNIOHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.nanohttp.NanoHTTPD;
import org.kevoree.library.nanohttp.NanoRestGroup;
import org.kevoree.library.sky.api.helper.KloudModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/01/12
 * Time: 18:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "SKY")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "SSH_Public_Key", optional = true)
})
public class KloudPaaSNanoGroup extends NanoRestGroup {//AbstractGroupType implements PaaSGroup{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
//	ContainerRoot userModel = KevoreeFactory.createContainerRoot();

	@Start
	public void startRestGroup () throws IOException {
		super.startRestGroup();
		if (KloudModelHelper.isPaaSNode(getModelService().getLastModel()/*, getName()*/, getNodeName())) {
			// configure ssh authorized keys for user nodes
			Object sshKeyObject = this.getDictionary().get("SSH_Public_Key");
			if (sshKeyObject != null) {
				// build directory if necessary
				File f = new File(System.getProperty("user.home") + File.separator + ".ssh");
				if ((f.exists() && f.isDirectory()) || (!f.exists() && f.mkdirs())) {
					// copy key
					FileNIOHelper.addStringToFile(sshKeyObject.toString(), new File(System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "authorized_keys"));
				}
			}
		}
	}

	@Stop
	public void stopRestGroup () {

	}

	@Override
	public boolean triggerPreUpdate (ContainerRoot currentModel, ContainerRoot proposedModel) {
		logger.debug("Trigger pre update");

		if (KloudModelHelper.isIaaSNode(currentModel, getNodeName()) && KloudModelHelper.isPaaSModel(proposedModel, getName(), getNodeName())) {
			logger.debug("A new user model is received (sent by the core and coming from the IaaS), notify all the PaaS nodes");
			// send to all the nodes except the one which is an IaaS node
			Group group = getModelElement();
			for (ContainerNode subNode : group.getSubNodesForJ()) {
				if (!subNode.getName().equals(this.getNodeName()) && !KloudModelHelper.isIaaSNode(currentModel, subNode.getName())) {
					try {
						internalPush(getModelService().getLastModel(), subNode.getName(), this.getNodeName());
					} catch (Exception e) {
						logger.warn("Unable to notify other members of {} group", group.getName());
					}
				}
			}
			// abort the update because the model is not for the IaaS but for the PaaS
			return false;
		} else {
			logger.debug("nothing specific, update can be done");
			return true;
		}
	}

	@Override
	public NanoHTTPD.Response processOnModelRequested (String uri) {
		if (!KloudModelHelper.isIaaSNode(getModelService().getLastModel(), getNodeName())) {
			if (uri.endsWith("/model/current")) {
				String msg = KevoreeXmiHelper.saveToString(getModelService().getLastModel(), false);
				return server.new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, msg);
			} else if (uri.endsWith("/model/current/zip")) {
				ByteArrayOutputStream st = new ByteArrayOutputStream();
				KevoreeXmiHelper.saveCompressedStream(st, getModelService().getLastModel());
				ByteArrayInputStream resultStream = new ByteArrayInputStream(st.toByteArray());
				return server.new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, resultStream);
			} else {
				return server.new Response(NanoHTTPD.HTTP_BADREQUEST, null, "");
			}
		} else {
			return server.new Response(NanoHTTPD.HTTP_BADREQUEST, null, "");
		}
	}

	@Override
	public void triggerModelUpdate () {
		/*if (KloudModelHelper.isPaaSNode(getModelService().getLastModel(), getNodeName()) && !KloudModelHelper.isPaaSModel(getModelService().getLastModel())) {
			// the current model is not the model of the PaaS so we need to look for it
			UUIDModel uuidModel = getModelService().getLastUUIDModel();
			for (ContainerNode subNode : getModelElement().getSubNodesForJ()) {
				try {
					ContainerRoot model = pull(subNode.getName());
					if (KloudModelHelper.isPaaSModel(getModelService().getLastModel())) {
						getModelService().atomicCompareAndSwapModel(uuidModel, model);
					}
				} catch (Exception ignored) {
				}
			}
		} else*/
		if (KloudModelHelper.isPaaSNode(getModelService().getLastModel(), getNodeName())) {
			// send to all the nodes
			Group group = getModelElement();
			for (ContainerNode subNode : group.getSubNodesForJ()) {
				if (!subNode.getName().equals(this.getNodeName())) {
					try {
						internalPush(getModelService().getLastModel(), subNode.getName(), this.getNodeName());
					} catch (Exception e) {
						logger.warn("Unable to notify other members of {} group", group.getName());
					}
				}
			}
		}
		// FIXME maybe when a node start he doesn't have the good model so we need to ask this model to someone
	}
}