package org.kevoree.library.javase.gossiperNettyAutoDiscovery;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.library.javase.gossiperNetty.group.NettyGossiperGroup;
import org.kevoree.library.jmdnsrest.JmDnsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Erwan Daubert
 */
@Library(name = "JavaSE")
@GroupType
@DictionaryType({
		@DictionaryAttribute(name = "timer", defaultValue = "5000", optional = false, fragmentDependant = true)
})
public class NettyGossiperGroupAutoDiscovery extends NettyGossiperGroup implements Runnable {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ArrayList<JmDnsComponent> jmdns = null;
	private Thread thread = null;
	private boolean alive;
	private int timer;

	@Start
	public void startGossiperGroup () {
		super.startGossiperGroup();
		jmdns = new ArrayList<JmDnsComponent>();
		thread = new Thread(this);
		getTimer();
		alive = true;
		thread.start();
	}

	@Stop
	public void stopGossiperGroup () {
		alive = false;
		thread.interrupt();
		for (JmDnsComponent _jmdns : jmdns) {
			_jmdns.close();
		}
		jmdns = null;
		super.stopGossiperGroup();
	}

	@Update
	public void updateGossiperGroup () {

	}

	public void getTimer () {
		try {
			timer = Integer.parseInt(getDictionary().get("timer").toString());
		} catch (Exception e) {
			timer = 9000;
		}
	}

	@Override
	public void run () {
		ArrayList<InetAddress> ips = getIps();
		ContainerRoot model = getModelService().getLastModel();
		// update local address interfaces
		for (InetAddress ip : ips) {
			KevoreePlatformHelper
					.updateNodeLinkProp(model, this.getNodeName(), this.getNodeName(), org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(), ip.getHostAddress(),
							"LAN" + ip.getHostName(),
							100);
//			KevoreePlatformHelper.updateNodeLinkProp(model, this.getNodeName(), this.getNodeName(), org.kevoree.framework.Constants.KEVOREE_MODEL_PORT(), this.getDictionary().get("port").toString(), "LAN", 100);
		}
		getModelService().updateModel(model);

		for (InetAddress ip : ips) {
			jmdns.add(new JmDnsComponent(this.getNodeName(), this.getName(), Integer.parseInt(this.getDictionary().get("http_port").toString()), this.getModelService(),
					"NettyGossiperGroupAutoDiscovery", ip));
		}
		while (alive) {
			for (JmDnsComponent _jmdns : jmdns) {
				_jmdns.requestUpdateList(timer);
			}
			try {
				Thread.sleep(timer);
			} catch (InterruptedException e) {
				logger.debug("requestUpdateList: " + e.getMessage());
			}
		}
	}

	public ArrayList<InetAddress> getIps () {
		ArrayList<InetAddress> ips = new ArrayList<InetAddress>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface interfaceN = interfaces.nextElement();
				Enumeration<InetAddress> ienum = interfaceN.getInetAddresses();
				while (ienum.hasMoreElements()) {
					InetAddress ia = ienum.nextElement();
					if (ia instanceof Inet4Address && !ia.getHostAddress().startsWith("127")) {
						ips.add(ia);
					}
				}
			}
		} catch (Exception e) {
			logger.error("There is no network interface", e);
		}
		return ips;
	}

	@Override
	public void triggerModelUpdate () {
		super.triggerModelUpdate();
		for (Group g : getModelService().getLastModel().getGroupsForJ()) {
			if (g.getName().equals("sync")) {
				logger.warn("{}", KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), "sync", "port", true, "minicloud"));
				logger.warn("{}", KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), "sync", "port", true, "node0"));
				logger.warn("{}", KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), "sync", "port", true, "node1"));

				logger.warn("{}", KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), "sync", "http_port", true, "minicloud"));
				logger.warn("{}", KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), "sync", "http_port", true, "node0"));
				logger.warn("{}", KevoreePropertyHelper.getIntPropertyForGroup(getModelService().getLastModel(), "sync", "http_port", true, "node1"));
			}
		}
	}
}
