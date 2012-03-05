/*
package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.javase.ssh.SSHRestConsensusGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

*/
/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/01/12
 * Time: 18:16
 *
 * @author Erwan Daubert
 * @version 1.0
 *//*

@Library(name = "SKY")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "masterNode", optional = false)
})
public class KloudPaaSGroupBak extends SSHRestConsensusGroup {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContainerRoot userModel;

	public void startRestGroup () {
		super.startRestGroup();
	}

	@Override
	public boolean triggerPreUpdate (ContainerRoot currentModel, ContainerRoot futureModel) {
		if (userModel != null) {
			return super.triggerPreUpdate(currentModel, futureModel);
		} else {
			return true;
		}
	}

	@Override
	public void triggerModelUpdate () {
		if (userModel != null) {
			super.triggerModelUpdate();
		} else {
			if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
				// pull the model to the master node
				userModel = pull(selectMasterNode(this.getDictionary().get("masterNode").toString()));
				this.getModelService().atomicUpdateModel(userModel);
			}
		}
	}

	private String selectMasterNode (String masterNodeList) {
		return masterNodeList.split(",")[0];
	}

	@Override
	public void push (ContainerRoot containerRoot, String s) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			KevoreeXmiHelper.saveStream(outStream, containerRoot);
			outStream.flush();
			Option<String> publicURLOption = KevoreePropertyHelper.getStringPropertyForGroup(containerRoot, this.getName(), "publicURL", false, "");

			String publicURL;
			if (publicURLOption.isEmpty() || publicURLOption.get().equals("")) {
				publicURL = "127.0.0.1";
			} else {
				publicURL = publicURLOption.get();
			}
			if (!publicURL.startsWith("http://")) {
				publicURL = "http://" + publicURL;
			}

			URL url = new URL(publicURL);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(outStream.toString());
			wr.flush();
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = rd.readLine();
			while (line != null) {
				line = rd.readLine();
			}
			wr.close();
			rd.close();

		} catch (Exception e) {
			logger.error("Unable to push a model on {}", s, e);

		}
	}

	@Override
	public boolean updateModel (ContainerRoot model, String sender) {
		// looking if this instance is on top of a IaaS node or a PaaS node (PJavaSeNode)
		if (KloudHelper.isIaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of IaaS node then we try to dispatch the received model on the kloud
			if (KloudReasoner.needsNewDeployment(model, userModel)) {
				logger.debug("A new Deployment must be done!");
				if (userModel == null) {
					userModel = KevoreeFactory.createContainerRoot();
				}
				Option<ContainerRoot> cleanedModelOption = KloudReasoner.processDeployment(model, userModel, this.getModelService(), this.getKevScriptEngineFactory(), this.getName());
				if (cleanedModelOption.isDefined()) {
					userModel = model;
					return true;
				}
				return false;
			} else {
				Option<ContainerRoot> cleanModelOption = KloudHelper.cleanUserModel(userModel);
				if (cleanModelOption.isDefined()) {
					logger.debug("An update will be done!");
					// there is no new node so we simply push model on each PaaSNode
					if (KloudReasoner.updateUserConfiguration(this.getName(), cleanModelOption.get(), model, this.getModelService(), getKevScriptEngineFactory())) {
						userModel = model;
						return true;
					}
				}
				return false;
			}
		} else if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of PaaS node then we deploy the model on the node
			this.getModelService().updateModel(model);
			return true;
		} else {
			logger.debug("Unable to manage this kind of node as a Kloud node");
			return false;
		}
	}

	@Override
	public String getModel () {
		if (KloudHelper.isIaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			return KevoreeXmiHelper.saveToString(userModel, false);
		} else if (KloudHelper.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of PaaS node then we deploy the model on the node
			return KevoreeXmiHelper.saveToString(this.getModelService().getLastModel(), false);
		} else {
			logger.debug("Unable to manage this kind of node as a Kloud node");
			return "";
		}
	}
}
*/
