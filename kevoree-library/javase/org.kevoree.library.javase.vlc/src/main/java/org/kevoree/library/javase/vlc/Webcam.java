package org.kevoree.library.javase.vlc;

/*import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.kevoree.annotation.*;
import org.kevoree.extra.vlcj.VLCNativeLibraryLoader;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

import java.awt.*;
import java.awt.image.BufferedImage;*/

import com.sun.jna.Memory;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/10/11
 * Time: 07:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@MessageTypes({
		@MessageType(name = "BufferedImage", elems = {@MsgElem(name = "image", className = BufferedImage.class)}),
		@MessageType(name = "bytes", elems =
				{@MsgElem(name = "bytes", className = byte[].class),
						@MsgElem(name = "width", className = Integer.class),
						@MsgElem(name = "height", className = Integer.class),
						@MsgElem(name = "chroma", className = String.class),
						@MsgElem(name = "fps", className = Integer.class)}
		)
})
@Requires({
		@RequiredPort(name = "image", type = PortType.MESSAGE, optional = true, messageType = "BufferedImage"),
		@RequiredPort(name = "image_bytes", type = PortType.MESSAGE, optional = true, messageType = "bytes")
})
@DictionaryType({
		@DictionaryAttribute(name = "DEVICE", defaultValue = "v4l2:///dev/video0",
				vals = {"v4l2:///dev/video0", "qtcapture://", "dshow://"}),
		@DictionaryAttribute(name = "LOG", defaultValue = "NONE", vals = {"NONE", "DEBUG"}, optional = false),
		@DictionaryAttribute(name = "FORMAT", defaultValue = "800x600",
				vals = {"1280x1024", "1024x768", "800x600", "640x480", "400x300", "200x150"})
})
@Library(name = "JavaSE")
@ComponentType
public class Webcam extends AbstractComponentType {
	private DirectMediaPlayer mediaPlayer;

	@Start
	public void start () throws Exception {
		System.setProperty("vlcj.log", (String) this.getDictionary().get("LOG"));
		String device = (String) this.getDictionary().get("DEVICE");
		mediaPlayer = MediaPlayerHelper.getInstance().getFactory(this.getName())
				.newDirectMediaPlayer(getWidth(), getHeight(), new OwnRenderCallback(getWidth(), getHeight()));
		mediaPlayer.playMedia(device, ":qtcapcture-caching=1:v4l2-caching=1");
	}

	@Stop
	public void stop () {
		mediaPlayer.stop();
		MediaPlayerHelper.getInstance().releaseKey(this.getName());
	}

	@Update
	public void update () throws Exception {
		stop();
		start();
	}

	private int getHeight () {
		try {
			String format = (String) this.getDictionary().get("FORMAT");
			String[] values = format.split("x");
			return Integer.parseInt(values[1]);
		} catch (Exception e) {
			return 600;
		}
	}

	private int getWidth () {
		try {
			String format = (String) this.getDictionary().get("FORMAT");
			String[] values = format.split("x");
			return Integer.parseInt(values[0]);
		} catch (Exception e) {
			return 800;
		}
	}

	private final class OwnRenderCallback implements RenderCallback {

		private final BufferedImage image;
		private int width;
		private int height;
		byte[] bytes;
		int[] ints;

		public OwnRenderCallback (int width, int height) {
//			super(new int[width * height]);
			image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
					.createCompatibleImage(width, height);
			this.width = width;
			this.height = height;
			bytes = new byte[width * height * 4];
			ints = new int[width * height];
		}

		/*@Override
		public void onDisplay (int[] data) {
			// The image data could be manipulated here...
			if (isPortBinded("image")) {
				image.setRGB(0, 0, width, height, data, 0, width);
				getPortByName("image", MessagePort.class).process(image);
			}

			if (isPortBinded("image_bytes")) {
				StdKevoreeMessage msg = new StdKevoreeMessage();
				msg.putValue("bytes", data);
				msg.putValue("height", data);
				msg.putValue("width", data);
				msg.putValue("chroma", data);
				msg.putValue("fps", data);
				*//*int[] newData = new int[data.length + 2];
				newData[0] = width;
				newData[1] = height;
				System.arraycopy(data, 0, newData, 2, data.length);*//*
				getPortByName("image_bytes", MessagePort.class).process(msg);
			}
		}*/

		@Override
		public void display (Memory memory) {
			if (isPortBinded("image_bytes")) {
				memory.read(0, bytes, 0, width * height * 4);
				StdKevoreeMessage msg = new StdKevoreeMessage();
				msg.putValue("bytes", ints);
				msg.putValue("height", height);
				msg.putValue("width", width);
				msg.putValue("chroma", "RV32");
				msg.putValue("fps", "30");
				getPortByName("image_bytes", MessagePort.class).process(msg);
			}
			if (isPortBinded("image")) {
				memory.read(0, ints, 0, width * height * 4);
				image.setRGB(0, 0, width, height, ints, 0, width);
				getPortByName("image", MessagePort.class).process(image);
			}
		}
	}
}
