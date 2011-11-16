package org.kevoree.library.javase.pipeChannel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePort;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
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

	private Map<String, RandomAccessFile> outputStreams;
	private PipeReader reader;

	@Start
	public void startPipeChannel () throws IOException, InterruptedException {
		outputStreams = new HashMap<String, RandomAccessFile>(this.getOtherFragments().size());
		if (isWindows()) {
			initializeOnWindows();
		} else {
			initializeOnLinux();
		}
		reader = new PipeReader(this);
	}

	@Stop
	public void stopPipeChannel () {
		for (RandomAccessFile stream : outputStreams.values()) {
			try {
				stream.close();
			} catch (IOException e) {

			}
		}
		reader.stop();
	}

	@Override
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
	}

	void forward (Message msg) {
		for (KevoreePort p : getBindedPorts()) {
			forward(p, msg);
		}
	}

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

	private void dispatchOnWindows (Message msg) {
		// TODO
	}

	private void dispatchOnUnix (Message msg, String nodeName) throws IOException {
		if (outputStreams.get(nodeName) == null) {
			outputStreams.put(nodeName,
					new RandomAccessFile(new File(System.getProperty("java.io.tmpdir") + File.separator + this
							.getName() + "_" + nodeName), "rw"));
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(stream);
		objectStream.writeObject(msg);
		outputStreams.get(nodeName).writeInt(stream.size());
		outputStreams.get(nodeName).write(stream.toByteArray());
	}

	public static boolean isWindows () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}
}
