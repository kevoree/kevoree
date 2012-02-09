package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.framework.*;
import org.kevoree.library.javase.ssh.SSHRestGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

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
		@DictionaryAttribute(name = "publicURL", optional = false)
})
public class KloudResourceManagerGroup extends SSHRestGroup {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContainerRoot userModel;

	public void startRestGroup () {
		super.startRestGroup();
		userModel = KevoreeFactory.createContainerRoot();
	}

	@Override
	public void triggerModelUpdate () {
		// do nothing
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
	public ContainerRoot pull (String s) {
		return super.pull(s);
	}

	@Override
	public void updateModel (ContainerRoot model) {
		// looking if this instance is on top of a IaaS node or a PaaS node (PJavaSeNode)
		if (KloudDeploymentManager.isIaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of IaaS node then we try to dispatch the received model on the kloud
			if (KloudDeploymentManager.needsNewDeployment(model, userModel)) {
				Option<ContainerRoot> cleanedModelOption = KloudDeploymentManager.processDeployment(model, userModel, this.getModelService(), this.getKevScriptEngineFactory(), this.getName());
				if (cleanedModelOption.isDefined()) {
					userModel = cleanedModelOption.get();
				}
			} else {
				Option<ContainerRoot> cleanModelOption = KloudDeploymentManager.cleanUserModel(userModel);
				if (cleanModelOption.isDefined()) {
					// there is no new node so we simply push model on each PaaSNode
					if (KloudDeploymentManager.updateUserConfiguration(this.getName(), cleanModelOption.get(), model, this.getModelService(), getKevScriptEngineFactory())) {
						userModel = model;
					}
				}
			}
		} else if (KloudDeploymentManager.isPaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			// if this instance is on top of PaaS node then we deploy the model on the node
			this.getModelService().updateModel(model);
		} else {
			logger.debug("Unable to manage this kind of node as a Kloud node");
		}
	}

	@Override
	public String getModel () {
		if (KloudDeploymentManager
				.isIaaSNode(this.getModelService().getLastModel(), this.getName(), this.getNodeName())) {
			return KevoreeXmiHelper.saveToString(userModel, false);
		} else if (KloudDeploymentManager.isPaaSNode(this.getModelService().getLastModel(), this.getName(),
				this.getNodeName())) {
			// if this instance is on top of PaaS node then we deploy the model on the node
			return KevoreeXmiHelper.saveToString(this.getModelService().getLastModel(), false);
		} else {
			logger.debug("Unable to manage this kind of node as a Kloud node");
			return "";
		}
	}
}
