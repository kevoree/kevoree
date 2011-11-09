package org.kevoree.library.javase.pipeChannel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 08/11/11
 * Time: 11:49
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ChannelTypeFragment
public class PipeChannel extends AbstractChannelFragment {
	private Logger logger = LoggerFactory.getLogger(PipeChannel.class);

	private Map<String, ObjectOutputStream> outputStreams;
	private PipeReader reader;

	@Start
	public void startPipeChannel () throws IOException {
		if (isWindows()) {
			initializeOnWindows();
		} else {
			initializeOnLinux();
		}
		reader = new PipeReader(this);

	}

	@Stop
	public void stopPipeChannel () {
		for (ObjectOutputStream stream : outputStreams.values()) {
			try {
				stream.close();
			} catch (IOException e) {

			}
		}
		reader.stop();
	}

	@Override
	public Object dispatch (Message msg) {

		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			forward(p, msg);
		}
		for (KevoreeChannelFragment cf : getOtherFragments()) {
			if (isWindows()) {
				dispatchOnWindows(msg);
			} else {
				try {
					dispatchOnUnix(msg, cf.getNodeName());
				} catch (IOException e) {
					logger.warn("Unable to use the pipe for " + cf.getNodeName() + " from " + this.getNodeName(), e);
				}
			}
		}

		return null;
	}

	@Override
	public ChannelFragmentSender createSender (String remoteNodeName, String remoteChannelName) {
		return null;
	}

	void forward (Message msg) {
		for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
			forward(p, msg);
		}
	}

	private void initializeOnWindows () {

	}

	private void initializeOnLinux () throws IOException {
		Runtime.getRuntime()
				.exec("mkfifo -m 777 " + System.getProperty("java.io.tmpdir") + File.separator + this
						.getName() + "_" + this.getNodeName());
	}

	private void dispatchOnWindows (Message msg) {

	}

	private void dispatchOnUnix (Message msg, String nodeName) throws IOException {
		if (outputStreams.get(nodeName) == null) {
			outputStreams.put(nodeName,
					new ObjectOutputStream(
							new FileOutputStream(new File(System.getProperty("java.io.tmpdir") + File.separator + this
									.getName() + "_" + nodeName))));
		}
		outputStreams.get(nodeName).writeObject(msg);
	}

	public static boolean isWindows () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}
}
