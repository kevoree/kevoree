package org.kevoree.library.javase.vlc.channel;

import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.javase.vlc.MediaPlayerHelper;
import org.kevoree.library.javase.vlc.VLCReaderActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.*;
import java.net.Socket;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 08/11/11
 * Time: 13:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "JavaSE")
@MessageTypes({
		@MessageType(name = "bytes", elems = {@MsgElem(name = "bytes", className = byte[].class),
				@MsgElem(name = "width", className = Integer.class),
				@MsgElem(name = "height", className = Integer.class),
				@MsgElem(name = "chroma", className = String.class),
				@MsgElem(name = "fps", className = Integer.class)}
		)
})
@Requires({
		@RequiredPort(name = "image_bytes", type = PortType.MESSAGE, optional = true, messageType = "bytes")
})
@DictionaryType({
		@DictionaryAttribute(name = "PROTOCOL", defaultValue = "RTP", vals = {"RTP", "RTSP", "HTTP"}),
		@DictionaryAttribute(name = "PORT", defaultValue = "8080", fragmentDependant = true)
})
@org.kevoree.annotation.ChannelTypeFragment
// TODO bounds for sender
public class VLCChannel extends AbstractChannelFragment {
	private Logger logger = LoggerFactory.getLogger(VLCChannel.class);

	private String media;
	private OutputStream stream;

	private EmbeddedMediaPlayer mediaPlayer;
	private VLCReaderActor readerActor;

	@Start
	public void startVLCChannel () throws IOException {
		if (isWindows()) {
			initializeOnWindows();
		} else {
			initializeOnLinux();
		}

		mediaPlayer = buildPlayer(this.getNodeName());

		readerActor = new VLCReaderActor(this);

	}

	@Stop
	public void stopVLCChannel () {
		mediaPlayer.stop();
		readerActor.stop();
		mediaPlayer.release();
		MediaPlayerHelper.getInstance().releaseKey(this.getName() + "_" + this.getNodeName());
	}

	public EmbeddedMediaPlayer buildPlayer (String nodeName) throws IOException {
		// launch VLC instance to stream
		mediaPlayer = MediaPlayerHelper.getInstance()
				.getFactory(this.getName() + "_" + this.getNodeName()).newEmbeddedMediaPlayer();

		String[] options = null;
		if (this.getDictionary().get("PROTOCOL").equals("HTTP")) {
			options = formatHttpStream("localhost", parsePortNumber(nodeName));
		} else if (this.getDictionary().get("PROTOCOL").equals("RTP")) {
			options = formatRtpStream("localhost", parsePortNumber(nodeName));
		} else if (this.getDictionary().get("PROTOCOL").equals("RTSP")) {
			options = formatRtspStream("localhost", parsePortNumber(nodeName),
					this.getName() + "_" + this.getNodeName());
		}
		mediaPlayer.playMedia(media, options);


		return mediaPlayer;
	}

	@Override
	public Object dispatch (Message msg) {
		if (msg.getContent() instanceof StdKevoreeMessage) {
			StdKevoreeMessage message = (StdKevoreeMessage) msg.getContent();
			if (!message.getValue("bytes").isEmpty() && !message.getValue("width").isEmpty()
					&& !message.getValue("height").isEmpty() && !message.getValue("chroma").isEmpty()
					&& !message.getValue("fps").isEmpty()) {
				try {
					for (KevoreePort p : getBindedPorts()) {
						forward(p, msg);
					}
					for (KevoreeChannelFragment fragment : this.getOtherFragments()) {
						Socket s = new Socket(getAddress(fragment.getNodeName()),
								parsePortNumber(fragment.getNodeName()));
						if (s.isConnected()) {
							ObjectOutputStream stream = new ObjectOutputStream(s.getOutputStream());
							stream.writeObject(message);
							stream.flush();
							stream.close();
							s.close();
						}
						if (isWindows()) {
							// TODO
						} else {
							try {
								dispatchOnUnix(message);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				} catch (IOException e) {
					logger.warn("Invalid parameter value. Please check!");
				}
			} else {
				logger.warn(
						"Incompatible type of message.\nMessage content: <bytes>, <height>, <width>, <chroma>, <fps>");
			}

		} else {
			logger.warn(
					"Unknown message type, must be StdKevoreeMessage with <bytes>, <height>, <width>, <chroma>, <fps>");
		}


		return null;
	}

	@Override
	public ChannelFragmentSender createSender (String remoteNodeName, String remoteChannelName) {
		return null;
	}

	public void forward (Message msg) {
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
		stream = new FileOutputStream(new File(System.getProperty("java.io.tmpdir") + File.separator + this
				.getName() + "_" + this.getNodeName()));

		media = System.getProperty("java.io.tmpdir") + File.separator + this.getName() + "_" + this.getNodeName();

	}

	private void dispatchOnWindows (Message msg) {

	}

	private void dispatchOnUnix (StdKevoreeMessage msg) throws IOException {
		stream.write((byte[]) msg.getValue("bytes").get());
	}

	public static boolean isWindows () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}

	private static String[] formatHttpStream (String serverAddress, int serverPort) {
		StringBuilder sb = new StringBuilder(60);
		sb.append(":sout=#duplicate{dst=std{access=http,mux=ts,");
		sb.append("dst=");
		sb.append(serverAddress);
		sb.append(':');
		sb.append(serverPort);
		sb.append("}}");
		return new String[]{sb.toString()};
	}

	private static String[] formatRtpStream (String serverAddress, int serverPort) {
		StringBuilder sb = new StringBuilder(60);
		sb.append(":sout=#rtp{dst=");
		sb.append(serverAddress);
		sb.append(",port=");
		sb.append(serverPort);
		sb.append(",mux=ts}");
		return new String[]{sb.toString(), ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep"};
	}

	private static String[] formatRtspStream (String serverAddress, int serverPort, String id) {
		StringBuilder sb = new StringBuilder(60);
		sb.append(":sout=#rtp{sdp=rtsp://@");
		sb.append(serverAddress);
		sb.append(':');
		sb.append(serverPort);
		sb.append('/');
		sb.append(id);
		sb.append("}");
		return new String[]{sb
				.toString(), ":no-sout-rtp-sap", ":no-sout-standard-sap", ":sout-all", ":sout-keep"};
	}

	public String getAddress (String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	public int parsePortNumber (String nodeName) throws IOException {
		try {
			//logger.debug("look for port on " + nodeName);
			return KevoreeFragmentPropertyHelper
					.getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(),
							"port",
							nodeName);
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}
}
