package org.kevoree.library.javase.pipe;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/11/11
 * Time: 19:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@GroupType
public class PipeGroup extends AbstractGroupType implements PipeInstance {
	private Logger logger = LoggerFactory.getLogger(PipeGroup.class);

	private Map<String, RandomAccessFile> outputStreams;
	private PipeReader reader;

	@Start
	public void startPipeGroup () throws IOException, InterruptedException {
		outputStreams = new HashMap<String, RandomAccessFile>();
		if (isWindows()) {
			initializeOnWindows();
		} else {
			initializeOnLinux();
		}
		reader = new PipeReader(this);
	}

	@Stop
	public void stopPipeGroup () {
		for (RandomAccessFile stream : outputStreams.values()) {
			try {
				stream.close();
			} catch (IOException e) {

			}
		}
		reader.stop();
	}

	/*@Override
	   public Object dispatch (Message msg) {

		   for (KevoreePort p : getBindedPorts()) {
			   forward(p, msg);
		   }
		   for (KevoreeChannelFragment cf : getOtherFragments()) {
			   forward(cf, msg);
		   }

		   return null;
	   }

	   @Override
	   public ChannelFragmentSender createSender (final String remoteNodeName, final String remoteChannelName) {
		   return new ChannelFragmentSender() {
			   @Override
			   public Object sendMessageToRemote (Message message) {
				   if (isWindows()) {
					   dispatchOnWindows(message);
				   } else {
					   try {
						   dispatchOnUnix(message, remoteNodeName);
					   } catch (IOException e) {
						   logger.warn(
								   "Unable to use the pipe for " + remoteChannelName + " from " + getNodeName() + " to "
										   + remoteNodeName, e);
					   }
				   }
				   return null;
			   }
		   };
	   }*/

	/*void forward (Message msg) {
		   for (KevoreePort p : getBindedPorts()) {
			   forward(p, msg);
		   }
	   }*/

	private void initializeOnWindows () {
		// TODO
	}

	private void initializeOnLinux () throws IOException, InterruptedException {
		final Process p = Runtime.getRuntime()
				.exec("mkfifo -m 777 " + System.getProperty("java.io.tmpdir") + File.separator + this
						.getName() + "_" + this.getNodeName());

		new Thread() {
			public void run () {
				InputStream inStream = p.getInputStream();
				InputStream errStream = p.getErrorStream();

				byte[] bytes = new byte[1024];
				try {
					int length = inStream.read(bytes);
					errStream.read(bytes);
					while (length != -1) {
						length = inStream.read(bytes);
//						System.out.write(bytes, 0, length);
						errStream.read(bytes);
					}
				} catch (IOException e) {
					//					e.printStackTrace();
				}


			}
		}.start();
		p.waitFor();
	}

	private void dispatchOnWindows (ContainerRoot msg) {
		// TODO
	}

	private void dispatchOnUnix (ContainerRoot msg, String nodeName) throws IOException {
		if (outputStreams.get(nodeName) == null) {
			outputStreams.put(nodeName,
					new RandomAccessFile(new File(System.getProperty("java.io.tmpdir") + File.separator + this
							.getName() + "_" + nodeName), "rw"));
		}


		ByteArrayOutputStream out = new ByteArrayOutputStream();
		KevoreeXmiHelper.saveStream(out, msg);
		outputStreams.get(nodeName).writeInt(out.size());
		outputStreams.get(nodeName).write(out.toByteArray());
		out.close();
	}

	public static boolean isWindows () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}

	@Override
	public void triggerModelUpdate () {
		for (Group g : this.getModelService().getLastModel().getGroupsForJ()) {
			if (g.getName().equals(this.getName())) {
				for (ContainerNode node : g.getSubNodesForJ()) {
					if (!node.getName().equals(this.getNodeName())) {
						if (isWindows()) {
							dispatchOnWindows(this.getModelService().getLastModel());
						} else {
							try {
								dispatchOnUnix(this.getModelService().getLastModel(), node.getName());
							} catch (IOException e) {
								logger.warn(
										"Unable to use the pipe for " + this.getName() + " from " + getNodeName()
												+ " to "
												+ node.getName(), e);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void push (ContainerRoot model, String targetNodeName) {
	}

	@Override
	public ContainerRoot pull (String targetNodeName) {
		return null;
	}

	@Override
	public void localForward (byte[] data, int length) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(data, 0, length);
			ContainerRoot model = KevoreeXmiHelper.loadStream(stream);
			this.getModelService().updateModel(model);
		} catch (Exception e) {
			logger.warn("Unable to convert data received as a model", e);
		}
	}
}
